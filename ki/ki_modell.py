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
DEVICE = 0 if torch.cuda.is_available() else -1

tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME, use_fast=False)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_NAME)

zsc = pipeline("zero-shot-classification", model=model, tokenizer=tokenizer, device=DEVICE)

# Schwellen
SENT_DELTA = 0.15
QUESTION_TAU = 0.80
TOPIC_TAU = 0.55

# Labels
SENTIMENT_LABELS = ["positiv", "negativ"]
SENTIMENT_TEMPLATE = "Dieser Text drückt eine {} Haltung gegenüber der Lehrveranstaltung aus."

QUESTION_TEMPLATE = "Dieser Text stellt {} an die Dozentin oder den Dozenten."
QUESTION_LABELS = ["eine Frage", "keine Frage"]

TOPIC_TEMPLATE = "Dieser Text bezieht sich auf {}."

QUESTION_TOPICS = [
    "Organisation und Ablauf",
    "Inhalt und Verständnis",
    "Materialien und Ressourcen",
    "Arbeitsbelastung & Lerntempo",
    "Technik und Ausstattung"
]

NONQUESTION_TOPICS = QUESTION_TOPICS.copy()

ORG_KEYWORDS = ["ablauf", "gliederung", "struktur", "zeitplan", "organisation", "termin", "frist"]
CONTENT_KEYWORDS = ["erklärung", "fachbegriff", "thema", "inhalt", "konzept", "theorie", "verständnis"]
MATERIAL_KEYWORDS = ["folien", "skript", "unterlagen", "literatur", "formelsammlung", "pdf", "aufzeichnung"]
WORKLOAD_KEYWORDS = ["tempo", "geschwindigkeit", "umfang", "arbeitsbelastung", "stoffmenge", "aufwand", "dichte"]
TECH_KEYWORDS = ["ton", "kamera", "projektor", "wlan", "abgestürzt", "störung", "ausfall"]

def boost_topic_scores(text: str, topic_scores: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    BOOST_VALUE = 0.20
    lowered = text.lower()
    scores_dict = {item["label"]: item["score"] for item in topic_scores}

    if any(w in lowered for w in ORG_KEYWORDS):
        if "Organisation und Ablauf" in scores_dict:
            scores_dict["Organisation und Ablauf"] += BOOST_VALUE

    if any(w in lowered for w in CONTENT_KEYWORDS):
        if "Inhalt und Verständnis" in scores_dict:
            scores_dict["Inhalt und Verständnis"] += BOOST_VALUE

    if any(w in lowered for w in MATERIAL_KEYWORDS):
        if "Materialien und Ressourcen" in scores_dict:
            scores_dict["Materialien und Ressourcen"] += BOOST_VALUE

    if any(w in lowered for w in WORKLOAD_KEYWORDS):
        workload_key = next((k for k in scores_dict if "Arbeitsbelastung" in k or "Arbeitsaufwand" in k), None)
        if workload_key:
            scores_dict[workload_key] += BOOST_VALUE

    if any(w in lowered for w in TECH_KEYWORDS):
        if "Technik und Ausstattung" in scores_dict:
            scores_dict["Technik und Ausstattung"] += BOOST_VALUE

    boosted_scores = [{"label": k, "score": v} for k, v in scores_dict.items()]
    boosted_scores.sort(key=lambda x: x["score"], reverse=True)
    return boosted_scores

class ClassifyRequest(BaseModel):
    text: str
    question_topics: Optional[List[str]] = None
    nonquestion_topics: Optional[List[str]] = None
    by_sentence: bool = False

class SentimentResult(BaseModel):
    label: str
    score_pos: float
    score_neg: float

class QuestionResult(BaseModel):
    is_question: bool
    score_question: float
    score_statement: float
    heuristic_used: bool
    urgency: Optional[str] = None

class TopicResult(BaseModel):
    applicable_set: str
    labels: List[Dict[str, Any]]

def zero_shot(text: str, candidate_labels: List[str], template: str, multi_label: bool = False):
    return zsc(
        sequences=text,
        candidate_labels=candidate_labels,
        hypothesis_template=template,
        multi_label=multi_label
    )

def decide_sentiment(text: str) -> SentimentResult:
    out = zero_shot(text, SENTIMENT_LABELS, SENTIMENT_TEMPLATE, multi_label=False)
    scores = dict(zip(out["labels"], out["scores"]))
    s_pos = float(scores.get("positiv", 0.0))
    s_neg = float(scores.get("negativ", 0.0))

    lowered = text.lower()
    POS_WORDS = {"super", "gut", "hervorragend", "verständlich", "hilfreich", "strukturiert", "toll"}
    NEG_WORDS = {"schlecht", "unverständlich", "langweilig", "kompliziert", "katastrophal"}

    if any(w in lowered for w in POS_WORDS):
        s_pos += 0.1
    if any(w in lowered for w in NEG_WORDS):
        s_neg += 0.1

    confidence = abs(s_pos - s_neg)
    if confidence < SENT_DELTA:
        label = "neutral"
    else:
        label = "positiv" if s_pos > s_neg else "negativ"

    return SentimentResult(label=label, score_pos=round(s_pos, 4), score_neg=round(s_neg, 4))

def detect_question(text: str) -> QuestionResult:
    lowered = text.strip().lower()
    has_qmark = "?" in lowered

    q_words = {
        "wie", "warum", "wieso", "weshalb", "wo", "wann", "wer", "was",
        "welche", "welcher", "welches",
        "kann", "können", "könnte", "könnten",
        "soll", "sollen", "sollte", "sollten",
        "dürfen", "dürfte", "dürften",
        "würde", "würden"
    }

    first_tokens = lowered.split()[:3]
    starts_with_qword = any(w in q_words for w in first_tokens)

    out = zero_shot(text, QUESTION_LABELS, QUESTION_TEMPLATE, multi_label=False)
    scores = dict(zip(out["labels"], out["scores"]))
    s_q = float(scores.get("eine Frage", 0.0))
    s_s = float(scores.get("keine Frage", 0.0))

    is_q = (s_q >= QUESTION_TAU) and (s_q > s_s) and (has_qmark or starts_with_qword)

    urgency = None
    if is_q:
        if any(w in lowered for w in ["heute", "morgen", "sofort", "dringend", "gleich", "endlich"]):
            urgency = "hoch"
        elif any(w in lowered for w in ["bald", "demnächst", "nächste", "nächsten", "nächster", "kurzfristig", "zeitnah", "die tage", "kommende"]):
            urgency = "mittel"
        else:
            urgency = "gering"

    return QuestionResult(
        is_question=is_q,
        score_question=round(s_q, 4),
        score_statement=round(s_s, 4),
        heuristic_used=(has_qmark or starts_with_qword),
        urgency=urgency
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

def analyze_one(text: str, question_topics: List[str], nonquestion_topics: List[str]) -> Dict[str, Any]:
    sentiment = decide_sentiment(text)
    qres = detect_question(text)

    if qres.is_question:
        max_score = max(sentiment.score_pos, sentiment.score_neg)
        diff_score = abs(sentiment.score_pos - sentiment.score_neg)
        if max_score < 0.65 or diff_score < 0.20:
            sentiment.label = "neutral"
        topics = multi_label_topics(text, question_topics)
        applicable = "question"
    else:
        topics = multi_label_topics(text, nonquestion_topics)
        applicable = "nonquestion"

    if topics:
        topics = boost_topic_scores(text, topics)
        topics = topics[:1]

    return {
        "text": text,
        "sentiment": sentiment.dict(),
        "question": qres.dict(),
        "topics": {"applicable_set": applicable, "labels": topics}
    }

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



