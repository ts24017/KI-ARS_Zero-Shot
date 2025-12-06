<script setup>
import { ref, onMounted, watch, computed } from "vue";
import { useRoute } from "vue-router";
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

const route = useRoute();
const courseId = ref(Number(route.query.courseId));
const lectureId = ref(Number(route.params.id));

watch(
    () => route.query.courseId,
    (newVal) => {
      courseId.value = Number(newVal);
    }
);

const lecture = ref(null);
const feedbacks = ref([]);
const loading = ref(true);
const error = ref(null);

async function fetchLectureSummary(id) {
  try {
    loading.value = true;
    error.value = null;
    const res = await fetch(`/api/feedback/lecture/${id}/summary`);
    if (!res.ok) throw new Error("API Error " + res.status);
    lecture.value = await res.json();
  } catch (e) {
    error.value = e.message;
    lecture.value = null;
  } finally {
    loading.value = false;
  }
}

async function fetchLectureFeedback(id) {
  try {
    const res = await fetch(`/api/feedback/lecture/${id}`);
    if (!res.ok) throw new Error("API Error " + res.status);
    feedbacks.value = await res.json();
  } catch (e) {
    feedbacks.value = [];
  }
}

watch(
    () => route.params.id,
    (newId) => {
      lectureId.value = Number(newId);
      fetchLectureSummary(lectureId.value);
      fetchLectureFeedback(lectureId.value);
    },
    { immediate: true }
);

function sentimentData(summary) {
  const order = ["positiv", "neutral", "negativ"];
  const colors = {
    positiv: "#4ade80",
    neutral: "#94a3b8",
    negativ: "#f87171",
  };
  const labels = order.filter((k) => summary.sentimentCounts?.[k] !== undefined);
  return {
    labels,
    datasets: [
      {
        data: labels.map((k) => summary.sentimentCounts[k]),
        backgroundColor: labels.map((k) => colors[k]),
      },
    ],
  };
}

function topicData(summary) {
  return {
    labels: Object.keys(summary.topicCounts || {}),
    datasets: [
      {
        label: "Topics",
        data: Object.values(summary.topicCounts || {}),
        backgroundColor: "#60a5fa",
      },
    ],
  };
}

function urgencyData(summary) {
  const order = ["gering", "mittel", "hoch"];
  const colors = {
    gering: "#4ade80",
    mittel: "#fbbf24",
    hoch: "#ef4444",
  };
  const labels = order.filter((k) => summary.urgencyCounts?.[k] !== undefined);
  return {
    labels,
    datasets: [
      {
        label: "Dringende Fragen",
        data: labels.map((k) => summary.urgencyCounts[k]),
        backgroundColor: labels.map((k) => colors[k]),
      },
    ],
  };
}

const selectedSentiment = ref(null);
const selectedTopic = ref(null);
const selectedUrgency = ref(null);

function chartOptions(type) {
  return {
    responsive: true,
    maintainAspectRatio: false,
    onClick: (evt, elements, chart) => {
      if (!elements.length) return;
      const index = elements[0].index;
      const label = chart.data.labels[index];

      if (type === "sentiment") {
        selectedSentiment.value =
            selectedSentiment.value === label ? null : label;
      } else if (type === "topic") {
        selectedTopic.value = selectedTopic.value === label ? null : label;
      } else if (type === "urgency") {
        selectedUrgency.value =
            selectedUrgency.value === label ? null : label;
      }
    },
    plugins: {
      legend: {
        position: "bottom",
      },
    },
  };
}

const filteredSentiment = computed(() =>
    selectedSentiment.value
        ? feedbacks.value.filter((f) => f.sentiment === selectedSentiment.value)
        : []
);

const filteredTopic = computed(() =>
    selectedTopic.value
        ? feedbacks.value.filter((f) => f.topic === selectedTopic.value)
        : []
);

const filteredUrgency = computed(() =>
    selectedUrgency.value
        ? feedbacks.value.filter((f) => f.urgency === selectedUrgency.value)
        : []
);
</script>

<template>
  <div class="lecture-dashboard">
    <router-link
        :to="`/teacher/course/${courseId}`"
        class="back-button"
    >
      ← Zurück zur Kursübersicht
    </router-link>

    <div v-if="loading">⏳ Lade Daten…</div>
    <div v-else-if="error" class="error-text">⚠️ {{ error }}</div>

    <div v-else-if="lecture">
      <h2 class="lecture-title">
        {{ lecture.courseName }} – Vorlesung am {{ lecture.lectureDate }}
      </h2>

      <div class="kpis">
        <div class="kpi">
          <p>Feedback gesamt</p>
          <strong>{{ lecture.totalFeedback }}</strong>
        </div>
        <div class="kpi">
          <p>Fragen</p>
          <strong>{{ lecture.questionCount }}</strong>
        </div>
        <div class="kpi">
          <p>Aussagen</p>
          <strong>{{ lecture.statementCount }}</strong>
        </div>
      </div>

      <div class="charts-grid">
        <div class="chart-box">
          <h3 class="chart-title">Sentiment</h3>
          <Pie
              v-if="lecture.sentimentCounts"
              :data="sentimentData(lecture)"
              :options="chartOptions('sentiment')"
          />
          <div v-if="filteredSentiment.length" class="feedback-list">
            <h4 class="feedback-list-title">
              {{ selectedSentiment }} Feedbacks
            </h4>
            <ul>
              <li v-for="fb in filteredSentiment" :key="fb.id">
                {{ fb.text }}
              </li>
            </ul>
          </div>
        </div>

        <div class="chart-box">
          <h3 class="chart-title">Topics</h3>
          <Bar
              v-if="lecture.topicCounts"
              :data="topicData(lecture)"
              :options="chartOptions('topic')"
          />
          <div v-if="filteredTopic.length" class="feedback-list">
            <h4 class="feedback-list-title">
              {{ selectedTopic }} Feedbacks
            </h4>
            <ul>
              <li v-for="fb in filteredTopic" :key="fb.id">
                {{ fb.text }}
              </li>
            </ul>
          </div>
        </div>

        <div class="chart-box">
          <h3 class="chart-title">Dringlichkeit (nur Fragen)</h3>
          <Bar
              v-if="lecture.urgencyCounts"
              :data="urgencyData(lecture)"
              :options="chartOptions('urgency')"
          />
          <div v-if="filteredUrgency.length" class="feedback-list">
            <h4 class="feedback-list-title">
              {{ selectedUrgency }} Fragen
            </h4>
            <ul>
              <li v-for="fb in filteredUrgency" :key="fb.id">
                {{ fb.text }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.lecture-dashboard {
  max-width: 1150px;
  margin: 80px auto 40px auto;
  padding: 0 24px 40px 24px;
  font-family: "Inter", sans-serif;
}

.back-button {
  display: inline-block;
  margin-bottom: 18px;
  padding: 8px 14px;
  background: #2563eb;
  color: #ffffff;
  border-radius: 8px;
  text-decoration: none;
  font-size: 0.9rem;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.35);
  transition: all 0.2s ease;
}

.back-button:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.error-text {
  color: #b91c1c;
  font-weight: 500;
}

.lecture-title {
  font-size: 1.5rem;
  margin-bottom: 18px;
  color: #1e3a8a;
}


.kpis {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin: 22px 0 30px 0;
}

.kpi {
  flex: 1 1 260px;
  background: #ffffff;
  padding: 20px;
  border-radius: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
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
  color: #111827;
}


.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.chart-box {
  background: #fafafa;
  padding: 14px 16px 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  height: 320px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
}

.chart-title {
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1e3a8a;
}

.chart-box canvas {
  flex: 1;
  max-height: 210px;
}

.feedback-list {
  max-height: 110px;
  overflow-y: auto;
  font-size: 0.85rem;
  margin-top: 6px;
  padding-top: 4px;
  border-top: 1px solid #e5e7eb;
}

.feedback-list-title {
  font-weight: 600;
  font-size: 0.85rem;
  margin-bottom: 4px;
}

.feedback-list ul {
  list-style: none;
  padding-left: 0;
  margin: 0;
}

.feedback-list li {
  padding: 3px 0;
  border-bottom: 1px solid #f3f4f6;
}

.feedback-list li:last-child {
  border-bottom: none;
}
</style>
