from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from fastapi import HTTPException
import torch
import re
from transformers import pipeline, AutoTokenizer, AutoModelForSequenceClassification

SENT_SPLIT_RE = re.compile(r'(?<=[\.\?\!])\s+(?=[A-ZÄÖÜ0-9„\"])')

def split_sentences(text: str) -> List[str]:
    parts = [s.strip() for s in SENT_SPLIT_RE.split(text) if s.strip()]
    return [p for p in parts if len(p) >= 2]

app = FastAPI(title="ARS Zero-Shot Service (DE)", version="0.1.0")

MODEL_NAME = "joeddav/xlm-roberta-large-xnli"
DEVICE = 0 if torch.cuda.is_available() else -1  # GPU falls verfügbar, sonst CPU

# Schwellen
SENT_TAU = 0.58      # Mindest-Score für pos/neg
SENT_DELTA = 0.12    # Margin pos vs. neg (Neutral, wenn zu klein)
QUESTION_TAU = 0.60  # Mindest-Score für "Frage"
TOPIC_TAU = 0.55     # Mindest-Score je Kategorie (multi-label)

# Labels & Hypothesen-Templates (Deutsch)
SENTIMENT_LABELS = ["positiv", "negativ"]
SENTIMENT_TEMPLATE = "Dieser Text drückt eine {} Haltung gegenüber der Lehrveranstaltung aus."

QUESTION_LABELS = ["Frage", "keine Frage"]
QUESTION_TEMPLATE = "Dieser Text ist {} an die Dozentin oder den Dozenten."

TOPIC_TEMPLATE = "Dieser Text bezieht sich auf {}."

# Kategorien (anpassbar)
QUESTION_TOPICS = [
    "Organisation und Ablauf",
    "Inhalt und Verständnis",
    "Didaktik und Methoden",
    "Materialien und Ressourcen"
]
NONQUESTION_TOPICS = [
    "Organisation und Ablauf",
    "Inhalt und Verständnis",
    "Didaktik und Methoden",
    "Materialien und Ressourcen"
]

tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME, use_fast=False)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_NAME)

zsc = pipeline(
    "zero-shot-classification",
    model=model,
    tokenizer=tokenizer,
    device=DEVICE
)

# ------- Schemas -------
class ClassifyRequest(BaseModel):
    text: str
    question_topics: Optional[List[str]] = None
    nonquestion_topics: Optional[List[str]] = None
    by_sentence: bool = False   # NEU

class SentimentResult(BaseModel):
    label: str
    score_pos: float
    score_neg: float

class QuestionResult(BaseModel):
    is_question: bool
    score_question: float
    score_statement: float
    heuristic_used: bool

class TopicResult(BaseModel):
    applicable_set: str
    labels: List[Dict[str, Any]]

class ClassifyResponse(BaseModel):
    sentiment: SentimentResult
    question: QuestionResult
    topics: TopicResult

def zero_shot(text: str, candidate_labels: List[str], template: str, multi_label: bool = False):
    return zsc(
        sequences=text,
        candidate_labels=candidate_labels,
        hypothesis_template=template,
        multi_label=multi_label
    )

def decide_sentiment(text: str) -> SentimentResult:
    out = zero_shot(text, SENTIMENT_LABELS, SENTIMENT_TEMPLATE, multi_label=False)
    scores = dict(zip(out["labels"], out["scores"]))  # absteigend sortiert
    s_pos = float(scores.get("positiv", 0.0))
    s_neg = float(scores.get("negativ", 0.0))
    max_score = max(s_pos, s_neg)

    if (max_score < SENT_TAU) or (abs(s_pos - s_neg) < SENT_DELTA):
        label = "neutral"
    else:
        label = "positiv" if s_pos >= s_neg else "negativ"

    return SentimentResult(label=label, score_pos=round(s_pos, 4), score_neg=round(s_neg, 4))

def analyze_one(text: str,
                question_topics: List[str],
                nonquestion_topics: List[str]) -> Dict[str, Any]:
    sentiment = decide_sentiment(text)
    qres = detect_question(text)

    if qres.is_question:
        topics = multi_label_topics(text, question_topics)
        applicable = "question"
    else:
        topics = multi_label_topics(text, nonquestion_topics)
        applicable = "nonquestion"

    return {
        "text": text,
        "sentiment": sentiment.dict(),
        "question": qres.dict(),
        "topics": {"applicable_set": applicable, "labels": topics}
    }




def detect_question(text: str) -> QuestionResult:
    lowered = text.strip().lower()

    has_qmark = "?" in lowered
    first_tokens = lowered.split()[:5]
    q_words = {
        "wie","warum","wieso","weshalb","wo","wann","wer","was",
        "kann","können","könnte","soll","sollte","ist","sind","gibt","dürfen"
    }
    has_qword = any(w in q_words for w in first_tokens)

    heuristic = has_qmark or has_qword  # <-- harte Bedingung

    out = zero_shot(text, QUESTION_LABELS, QUESTION_TEMPLATE, multi_label=False)
    scores = dict(zip(out["labels"], out["scores"]))
    s_q = float(scores.get("Frage", 0.0))
    s_s = float(scores.get("keine Frage", 0.0))


    if heuristic:
        is_q = (s_q >= QUESTION_TAU) and (s_q >= s_s)
    else:
        is_q = False

    return QuestionResult(
        is_question=is_q,
        score_question=round(s_q, 4),
        score_statement=round(s_s, 4),
        heuristic_used=heuristic
    )

def multi_label_topics(text: str, labels: List[str]) -> List[Dict[str, Any]]:
    out = zero_shot(text, labels, TOPIC_TEMPLATE, multi_label=True)
    pairs = list(zip(out["labels"], out["scores"]))
    pairs.sort(key=lambda x: x[1], reverse=True)
    kept = [{"label": l, "score": round(float(s), 4)} for l, s in pairs if s >= TOPIC_TAU]
    if not kept and pairs:
        l, s = pairs[0]
        kept = [{"label": l, "score": round(float(s), 4)}]
    return kept


@app.post("/classify")
def classify(req: ClassifyRequest):
    text = req.text.strip()
    if not text:
        return {
            "mode": "single",
            "sentiment": {"label": "neutral", "score_pos": 0.0, "score_neg": 0.0},
            "question": {"is_question": False, "score_question": 0.0, "score_statement": 1.0, "heuristic_used": False},
            "topics": {"applicable_set": "nonquestion", "labels": []}
        }

    q_topics = req.question_topics or QUESTION_TOPICS
    nq_topics = req.nonquestion_topics or NONQUESTION_TOPICS

    try:
        if req.by_sentence:
            sentences = split_sentences(text)
            results = [analyze_one(s, q_topics, nq_topics) for s in sentences] or \
                      [analyze_one(text, q_topics, nq_topics)]
            return {"mode": "by_sentence", "sentences": results}
        else:
            one = analyze_one(text, q_topics, nq_topics)
            return {"mode": "single", **one}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"classify_failed: {type(e).__name__}: {e}")

@app.get("/health")
def health():
    return {"status": "ok", "device": "cuda" if DEVICE == 0 else "cpu", "model": MODEL_NAME}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("ki.ki_modell:app", host="0.0.0.0", port=8000, reload=True)


