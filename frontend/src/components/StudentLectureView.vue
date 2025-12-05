<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const lectures = ref([]);
const courseName = ref("");
const courseId = Number(route.params.id);

const user = JSON.parse(localStorage.getItem("user"));

async function loadLectures() {
  const res = await fetch(`/api/course/${courseId}/lectures`);
  if (res.ok) {
    lectures.value = await res.json();
  } else {
    lectures.value = [];
  }
}

async function loadCourseName() {
  if (!user || !user.id) return;

  const res = await fetch(`/api/student/${user.id}/courses`);
  if (res.ok) {
    const data = await res.json();
    const course = data.find((c) => c.id === courseId);
    if (course) {
      courseName.value = course.name;
    }
  }
}

onMounted(async () => {
  await Promise.all([loadLectures(), loadCourseName()]);
});

function goToLecture(lectureId, index) {
  router.push({
    path: `/student/course/${courseId}/lecture/${lectureId}`,
    query: {
      courseName: courseName.value,
      lectureNumber: index + 1
    }
  });
}

function goBack() {
  router.push("/student");
}
</script>

<template>
  <div class="student-course-view">
    <button class="back-btn" @click="goBack">
      Zurück
    </button>

    <h1>Lehrveranstaltungen</h1>
    <p>
      Hier siehst du alle Einheiten für den Kurs:
      <strong>{{ courseName }}</strong>.
    </p>

    <ul class="lecture-list">
      <li
          v-for="(lecture, index) in lectures"
          :key="lecture.id"
          class="lecture-item"
          @click="goToLecture(lecture.id, index)"
      >
        🎓 <strong>Vorlesungseinheit {{ index + 1 }}</strong>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.student-course-view {
  max-width: 800px;
  margin: 80px auto;
  font-family: "Inter", Arial, sans-serif;
  padding: 20px;
  color: #1e293b;
}

.back-btn {
  margin-bottom: 20px;
  padding: 8px 14px;
  background: #2563eb;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s ease;
}

.back-btn:hover {
  background: #1e40af;
}

h1 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 12px;
}

p {
  margin-bottom: 18px;
  font-size: 0.95rem;
}

.lecture-list {
  list-style: none;
  padding: 0;
  margin-top: 20px;
}

.lecture-item {
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px 16px;
  margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  transition: transform 0.15s ease, background 0.15s ease;
  cursor: pointer;
}

.lecture-item:hover {
  transform: translateY(-2px);
  background: #e0f2fe;
}
</style>
