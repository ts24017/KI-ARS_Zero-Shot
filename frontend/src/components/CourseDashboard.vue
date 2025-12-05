<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Pie, Bar } from "vue-chartjs";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale,
  LinearScale,
  BarElement,
} from "chart.js";

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale, LinearScale, BarElement);

const router = useRouter();
const route = useRoute();

const courseId = ref(Number(route.params.id));
const summary = ref(null);
const feedbackList = ref([]);

const expandedTopics = ref({});
const expandedSub = ref({});
const expandedSentiment = ref({});

function logout() {
  localStorage.clear();
  router.push("/");
}

async function loadData() {
  const [summaryRes, feedbackRes] = await Promise.all([
    fetch(`/api/feedback/course/${courseId.value}/summary`),
    fetch(`/api/feedback/course/${courseId.value}`)
  ]);
  summary.value = summaryRes.ok ? await summaryRes.json() : null;
  feedbackList.value = feedbackRes.ok ? await feedbackRes.json() : [];
}

onMounted(loadData);

const lectures = ref([]);

/** Lectures laden und NACH DATUM sortieren (wichtig für 1-basierte Nummerierung) */
async function loadLectures() {
  const res = await fetch(`/api/course/${courseId.value}/lectures`);
  if (res.ok) {
    const data = await res.json();
    data.sort((a, b) => new Date(a.date) - new Date(b.date));
    lectures.value = data;
  }
}

function goToLecture(lectureId) {
  router.push({
    path: `/teacher/lecture/${lectureId}`,
    query: { courseId: courseId.value }
  });
}

onMounted(async () => {
  await loadData();
  await loadLectures();
});

/** Datum robust normalisieren → "YYYY-MM-DD" */
function normalizeDate(d) {
  if (!d) return null;
  const asDate = new Date(d);
  if (!isNaN(asDate)) {
    return asDate.toISOString().slice(0, 10);
  }
  // Fallback: String schon im richtigen Format oder ähnlich
  if (typeof d === "string") return d.slice(0, 10);
  return null;
}

/** Map: Lecture-ID -> laufende Nummer (1-basiert) */
const lectureIndexMap = computed(() => {
  const map = {};
  lectures.value.forEach((lec, idx) => {
    if (lec && lec.id != null) {
      map[lec.id] = idx + 1;
    }
  });
  return map;
});

const lectureDateIndexMap = computed(() => {
  const map = {};
  lectures.value.forEach((lec, idx) => {
    const key = normalizeDate(lec?.date);
    if (key) map[key] = idx + 1;
  });
  return map;
});

function formatDateDE(d) {
  const dt = new Date(d);
  if (isNaN(dt)) return d ?? "";
  return dt.toLocaleDateString("de-DE");
}

function lectureLabel(fb) {

  const lecId = fb?.lectureId ?? fb?.lecture?.id ?? fb?.lecture_id ?? null;
  if (lecId != null) {
    const n = lectureIndexMap.value[lecId];
    if (n) return `Vorlesungseinheit ${n}`;
    // zur Not: Datum der Lecture suchen und anzeigen
    const lec = lectures.value.find(l => l?.id === lecId);
    if (lec?.date) return `Vorlesung vom ${formatDateDE(lec.date)}`;
  }

  const key = normalizeDate(fb?.lectureDate);
  if (key) {
    const nByDate = lectureDateIndexMap.value[key];
    if (nByDate) return `Vorlesungseinheit ${nByDate}`;
    // Wenn keine Nummer, zumindest Datum anzeigen
    return `Vorlesung vom ${formatDateDE(key)}`;
  }

  const directNumber =
      fb?.lectureNumber ??
      fb?.lecture_index ??
      fb?.lectureNo ??
      fb?.lecture?.number ??
      null;
  if (typeof directNumber === "number" && Number.isFinite(directNumber)) {
    return `Vorlesungseinheit ${directNumber}`;
  }

  return `keiner Vorlesungseinheit zugeordnet`;
}

const groupedFeedback = computed(() => {
  const grouped = {};
  feedbackList.value.forEach(fb => {
    const topic = fb.topic || "Allgemein";
    const type = fb.question ? "Fragen" : "Aussagen";
    const sentiment = fb.sentiment || "neutral";

    if (!grouped[topic]) grouped[topic] = { Fragen: {}, Aussagen: {}, __count: 0 };
    if (!grouped[topic][type][sentiment]) grouped[topic][type][sentiment] = [];

    grouped[topic][type][sentiment].push(fb);
    grouped[topic].__count += 1;
  });
  return grouped;
});

function toggleTopic(topic) {
  expandedTopics.value[topic] = !expandedTopics.value[topic];
}
function toggleSub(topic, type) {
  if (!expandedSub.value[topic]) expandedSub.value[topic] = {};
  expandedSub.value[topic][type] = !expandedSub.value[topic][type];
}
function toggleSentiment(topic, type, sentiment) {
  if (!expandedSentiment.value[topic]) expandedSentiment.value[topic] = {};
  if (!expandedSentiment.value[topic][type]) expandedSentiment.value[topic][type] = {};
  expandedSentiment.value[topic][type][sentiment] = !expandedSentiment.value[topic][type][sentiment];
}

function sentimentData(data) {
  const colors = { positiv: "#4ade80", neutral: "#94a3b8", negativ: "#f87171" };
  const labels = Object.keys(data.sentimentCounts || {});
  return {
    labels,
    datasets: [{
      data: labels.map(l => data.sentimentCounts[l]),
      backgroundColor: labels.map(l => colors[l])
    }]
  };
}
function topicData(data) {
  return {
    labels: Object.keys(data.topicCounts || {}),
    datasets: [{
      label: "Topics",
      data: Object.values(data.topicCounts || {}),
      backgroundColor: "#60a5fa"
    }]
  };
}
function urgencyData(data) {
  const colors = { gering: "#4ade80", mittel: "#fbbf24", hoch: "#ef4444" };
  const labels = Object.keys(data.urgencyCounts || {});
  return {
    labels,
    datasets: [{
      label: "Dringlichkeit",
      data: Object.values(data.urgencyCounts || {}),
      backgroundColor: labels.map(l => colors[l])
    }]
  };
}
</script>

<template>
  <header class="headbar">
    <div class="headbar-inner">
      <router-link to="/" class="logo">Audience Response System</router-link>
      <nav>
        <router-link to="/" class="nav-link">Home</router-link>
        <router-link to="/" class="nav-link">Kurse</router-link>
      </nav>
      <div class="spacer"></div>
      <button @click="logout" class="logout">Ausloggen</button>
    </div>
  </header>

  <main class="dashboard">
    <h1>Kurs-Dashboard</h1>

    <div v-if="summary" class="summary">
      <h2>Gesamtauswertung für Kurs:  {{ summary.courseName }}</h2>

      <div class="lecture-list">
        <h3>Vorlesungseinheiten</h3>
        <div class="lecture-buttons">
          <button
              v-for="(lecture, index) in lectures"
              :key="lecture.id"
              class="lecture-btn"
              @click="goToLecture(lecture.id)"
          >
            Vorlesungseinheit {{ index + 1 }}
          </button>
        </div>
      </div>

      <div class="kpis">
        <div class="kpi"><p>Anzahl Feedbacks:</p><strong>{{ summary.totalFeedback }}</strong></div>
        <div class="kpi"><p>Fragen:</p><strong>{{ summary.questionCount }}</strong></div>
        <div class="kpi"><p>Aussagen:</p><strong>{{ summary.statementCount }}</strong></div>
      </div>

      <div class="charts">
        <div class="chart chart--pie">
          <h3>Sentiment</h3>
          <Pie :data="sentimentData(summary)" />
        </div>

        <div class="chart chart--bar">
          <h3>Topics</h3>
          <Bar :data="topicData(summary)" />
        </div>

        <div class="chart chart--bar">
          <h3>Dringlichkeit</h3>
          <Bar :data="urgencyData(summary)" />
        </div>
      </div>
    </div>

    <section v-if="Object.keys(groupedFeedback).length" class="feedback-section">
      <h2>Feedback nach Kategorien</h2>

      <div v-for="(topicBlock, topic) in groupedFeedback" :key="topic" class="topic-block">
        <div class="topic-header" @click="toggleTopic(topic)">
          <h3>{{ topic }}: {{ topicBlock.__count }}</h3>
          <span>{{ expandedTopics[topic] ? "▲" : "▼" }}</span>
        </div>

        <transition name="fade">
          <div v-if="expandedTopics[topic]" class="subsection">
            <div v-for="type in ['Aussagen', 'Fragen']" :key="type" class="sub-block">
              <div class="sub-header" @click="toggleSub(topic, type)">
                <strong>{{ type }}</strong>
                <span>{{ expandedSub[topic]?.[type] ? "▲" : "▼" }}</span>
              </div>

              <transition name="fade">
                <div v-if="expandedSub[topic]?.[type]" class="sentiment-section">
                  <div v-for="(sentimentGroup, sentiment) in topicBlock[type]" :key="sentiment" class="sentiment-block">
                    <div class="sentiment-header" @click="toggleSentiment(topic, type, sentiment)">
                      <em>Sentiment: {{ sentiment }}</em>
                      <span>{{ expandedSentiment[topic]?.[type]?.[sentiment] ? "▲" : "▼" }}</span>
                    </div>

                    <ul v-if="expandedSentiment[topic]?.[type]?.[sentiment]" class="feedback-list">
                      <li v-for="fb in sentimentGroup" :key="fb.id">
                        {{ fb.text }}
                        <span class="lecture-tag">({{ lectureLabel(fb) }})</span>
                      </li>
                    </ul>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </transition>
      </div>
    </section>
  </main>
</template>

<style>
:root {
  --blue1: #2563eb;
  --blue2: #4f46e5;
  --light-bg: #f8fafc;
  --white: #ffffff;
  --border: #e5e7eb;
  --text-dark: #111827;
  --shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

body {
  font-family: "Inter", sans-serif;
  background: var(--light-bg);
  color: var(--text-dark);
  margin: 0;
}

.headbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(90deg, var(--blue1), var(--blue2));
  color: white;
  height: 60px;
  display: flex;
  align-items: center;
  box-shadow: var(--shadow);
  z-index: 100;
}

.headbar-inner {
  width: 100%;
  max-width: 1150px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 24px;
}

.logo {
  font-weight: 600;
  font-size: 1.1rem;
  text-decoration: none;
  color: white;
}

.nav-link {
  color: white;
  margin: 0 10px;
  text-decoration: none;
  font-size: 0.95rem;
  position: relative;
}

.nav-link::after {
  content: "";
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 0;
  height: 2px;
  background: white;
  transition: width 0.3s ease;
}

.nav-link:hover::after {
  width: 100%;
}

.spacer {
  flex: 1;
}

.logout {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border: none;
  color: white;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.25s ease;
}

.logout:hover {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  transform: translateY(-1px);
}

.dashboard {
  max-width: 1150px;
  margin: 90px auto 60px auto;
  padding: 0 24px;
}

h1 {
  font-size: 1.8rem;
  margin-bottom: 8px;
}

h2 {
  font-size: 1.3rem;
  margin-bottom: 22px;
  color: #1e3a8a;
  font-weight: 600;
}

.kpis {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin: 22px 0 35px 0;
}

.kpi {
  flex: 1 1 260px;
  background: var(--white);
  padding: 20px;
  border-radius: 14px;
  box-shadow: var(--shadow);
  text-align: center;
  border: 1px solid #f1f5f9;
  transition: transform 0.2s ease;
}

.kpi:hover {
  transform: translateY(-3px);
}

.kpi p {
  font-size: 0.95rem;
  color: #6b7280;
  margin-bottom: 5px;
}

.kpi strong {
  font-size: 1.6rem;
  color: var(--text-dark);
}

.charts {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 28px;
  margin-top: 40px;
}

.chart {
  flex: 1 1 330px;
  max-width: 350px;
  background: var(--white);
  border-radius: 22px;
  padding: 20px 24px 30px 24px;
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  transition: all 0.25s ease;
}

.chart:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.08);
}

.chart h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1e3a8a;
  margin-bottom: 20px;
}

.chart--pie canvas {
  max-width: 240px !important;
  max-height: 240px !important;
  width: 240px !important;
  height: 240px !important;
}

.chart--bar canvas {
  width: 100% !important;
  height: 220px !important;
}

@media (max-width: 1000px) {
  .chart {
    flex: 1 1 100%;
    max-width: 100%;
  }
}

.lecture-list {
  margin-bottom: 30px;
}

.lecture-list h3 {
  font-size: 1.1rem;
  color: #1e3a8a;
  margin-bottom: 12px;
}

.lecture-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.lecture-btn {
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.lecture-btn:hover {
  background: #1e40af;
  transform: translateY(-2px);
}

.feedback-section {
  margin-top: 60px;
}

.topic-block {
  margin-bottom: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,.06);
  overflow: hidden;
}

.topic-header {
  background: linear-gradient(90deg, var(--blue1), var(--blue2));
  color: white;
  padding: 12px 18px;
  display: flex;
  justify-content: space-between;
  cursor: pointer;
  align-items: center;
  font-weight: 600;
  letter-spacing: 0.3px;
  transition: background 0.2s ease;
}

.topic-header:hover { filter: brightness(1.05); }

.sub-header {
  background: #eef2ff;
  padding: 8px 16px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  font-weight: 500;
  border-top: 1px solid #c7d2fe;
}

.sentiment-header {
  background: #f3f4f6;
  padding: 6px 22px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  color: #374151;
  font-size: 0.9rem;
}

.feedback-list {
  list-style: none;
  margin: 0;
  padding: 10px 26px;
  background: #fff;
}

.feedback-list li {
  border-bottom: 1px solid #e5e7eb;
  padding: 6px 0;
}

.lecture-tag {
  opacity: 0.85;
  font-size: 0.9em;
  margin-left: 6px;
  color: #374151;
}
</style>

