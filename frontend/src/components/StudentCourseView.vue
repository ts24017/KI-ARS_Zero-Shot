<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const user = JSON.parse(localStorage.getItem("user"));
const courses = ref([]);
const router = useRouter();

onMounted(async () => {
  const res = await fetch(`/api/student/${user.id}/courses`);
  if (res.ok) courses.value = await res.json();
});

function goToCourse(courseId) {
  router.push(`/student/course/${courseId}`);
}
</script>

<template>
  <div class="dashboard">
    <h1>Hallo, {{ user.username }}</h1>
    <h2>Deine Kurse</h2>

    <ul class="course-list">
      <li
          v-for="course in courses"
          :key="course.id"
          class="course-item"
          @click="goToCourse(course.id)"
      >
        🎓 {{ course.name }} <br />
        <small>Dozent: {{ course.teacherName }}</small>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 800px;
  margin: 80px auto;
  font-family: "Inter", Arial, sans-serif;
  padding: 20px;
}

h1 {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 10px;
}

h2 {
  font-size: 20px;
  font-weight: 600;
  margin-top: 20px;
  margin-bottom: 16px;
}

.course-list {
  list-style: none;
  padding: 0;
}

.course-item {
  background: #f1f5f9;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: transform 0.15s ease, background 0.15s ease;
}

.course-item:hover {
  transform: translateY(-2px);
  background: #e0f2fe;
}
</style>
