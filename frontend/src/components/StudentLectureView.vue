<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const lectures = ref([]);
const courseId = route.params.id;

onMounted(async () => {
  const res = await fetch(`/api/course/${courseId}/lectures`);
  if (res.ok) lectures.value = await res.json();
});

function goToLecture(lectureId) {
  router.push(`/student/course/${courseId}/lecture/${lectureId}`);
}
</script>

<template>
  <div class="student-course-view">
    <h1>Lehrveranstaltungen</h1>
    <p>Hier siehst du alle Einheiten für diesen Kurs.</p>

    <ul class="lecture-list">
      <li
          v-for="(lecture, index) in lectures"
          :key="lecture.id"
          class="lecture-item"
          @click="goToLecture(lecture.id)"
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

h1 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 12px;
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
