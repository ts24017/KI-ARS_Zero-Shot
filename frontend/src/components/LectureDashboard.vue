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

// === ROUTE PARAMS UND QUERIES ===
const route = useRoute();
const courseId = ref(Number(route.query.courseId)); // Query: ?courseId=1
const lectureId = ref(Number(route.params.id));     // Route: /teacher/lecture/:id

// === REAKTIVES NEULADEN BEI URL-WECHSEL ===
watch(
    () => route.query.courseId,
    (newVal) => {
      courseId.value = Number(newVal);
    }
);

// === API-DATEN ===
const lecture = ref(null);
const feedbacks = ref([]);
const loading = ref(true);
const error = ref(null);

// === API CALLS ===
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

// === LADEN BEIM START UND BEI URL-WECHSEL ===
watch(
    () => route.params.id,
    (newId) => {
      lectureId.value = Number(newId);
      fetchLectureSummary(lectureId.value);
      fetchLectureFeedback(lectureId.value);
    },
    { immediate: true }
);

// === CHART-DATEN ===
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

// === CHART-INTERAKTION ===
const selectedSentiment = ref(null);
const selectedTopic = ref(null);
const selectedUrgency = ref(null);

function chartOptions(type) {
  return {
    onClick: (evt, elements, chart) => {
      if (elements.length > 0) {
        const index = elements[0].index;
        const label = chart.data.labels[index];

        if (type === "sentiment") {
          selectedSentiment.value = label;
        } else if (type === "topic") {
          selectedTopic.value = label;
        } else if (type === "urgency") {
          selectedUrgency.value = label;
        }
      }
    },
  };
}

// === GEFILTERTE FEEDBACKS ===
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
  <div class="p-6">
    <router-link :to="`/teacher/course/${courseId}`" class="text-blue-600 underline mb-4 inline-block">
      Zurück zur Kursübersicht
    </router-link>


    <div v-if="loading">⏳ Lade Daten…</div>
    <div v-else-if="error" class="text-red-600">⚠️ {{ error }}</div>

    <div v-else-if="lecture">
      <h2 class="text-xl font-semibold mb-2">
        {{ lecture.courseName }} – Vorlesung am {{ lecture.lectureDate }}
      </h2>

      <!-- KPIs -->
      <div class="flex gap-6 mb-6">
        <div class="bg-gray-100 p-4 rounded-lg text-center flex-1">
          <div class="text-gray-500 text-sm">Feedback gesamt</div>
          <div class="text-2xl font-bold">{{ lecture.totalFeedback }}</div>
        </div>
        <div class="bg-gray-100 p-4 rounded-lg text-center flex-1">
          <div class="text-gray-500 text-sm">Fragen</div>
          <div class="text-2xl font-bold">{{ lecture.questionCount }}</div>
        </div>
        <div class="bg-gray-100 p-4 rounded-lg text-center flex-1">
          <div class="text-gray-500 text-sm">Aussagen</div>
          <div class="text-2xl font-bold">{{ lecture.statementCount }}</div>
        </div>
      </div>

      <!-- Charts -->
      <div class="charts-grid">
        <!-- Sentiment -->
        <div class="chart-box">
          <h3 class="text-sm font-medium mb-2">Sentiment</h3>
          <Pie
              v-if="lecture.sentimentCounts"
              :data="sentimentData(lecture)"
              :options="{ ...chartOptions('sentiment'), maintainAspectRatio: false }"
          />
          <div v-if="filteredSentiment.length > 0" class="mt-2 feedback-list">
            <h4 class="font-semibold text-sm mb-1">{{ selectedSentiment }} Feedbacks</h4>
            <ul>
              <li v-for="fb in filteredSentiment" :key="fb.id">{{ fb.text }}</li>
            </ul>
          </div>
        </div>

        <!-- Topics -->
        <div class="chart-box">
          <h3 class="text-sm font-medium mb-2">Topics</h3>
          <Bar
              v-if="lecture.topicCounts"
              :data="topicData(lecture)"
              :options="{ ...chartOptions('topic'), maintainAspectRatio: false }"
          />
          <div v-if="filteredTopic.length > 0" class="mt-2 feedback-list">
            <h4 class="font-semibold text-sm mb-1">{{ selectedTopic }} Feedbacks</h4>
            <ul>
              <li v-for="fb in filteredTopic" :key="fb.id">{{ fb.text }}</li>
            </ul>
          </div>
        </div>

        <!-- Urgency -->
        <div class="chart-box">
          <h3 class="text-sm font-medium mb-2">Dringlichkeit (nur Fragen)</h3>
          <Bar
              v-if="lecture.urgencyCounts"
              :data="urgencyData(lecture)"
              :options="{ ...chartOptions('urgency'), maintainAspectRatio: false }"
          />
          <div v-if="filteredUrgency.length > 0" class="mt-2 feedback-list">
            <h4 class="font-semibold text-sm mb-1">{{ selectedUrgency }} Fragen</h4>
            <ul>
              <li v-for="fb in filteredUrgency" :key="fb.id">{{ fb.text }}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.chart-box {
  background: #fafafa;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  height: 300px; /* feste Höhe */
  display: flex;
  flex-direction: column;
}

.chart-box canvas {
  flex: 1; /* Chart passt sich Höhe an */
  max-height: 220px; /* Chart selbst kleiner */
}

.feedback-list {
  max-height: 100px;
  overflow-y: auto;
  font-size: 0.85rem;
}
</style>
