<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const user = JSON.parse(localStorage.getItem("user"));
const courses = ref([]);
const error = ref("");

onMounted(async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/teacher/${user.id}/courses`);
    if (!res.ok) throw new Error("Fehler beim Laden der Lehrveranstaltungen");
    courses.value = await res.json();
  } catch (e) {
    console.error(e);
    error.value = "Konnte Lehrveranstaltungen nicht laden.";
  }
});

function openCourse(courseId) {
  router.push(`/teacher/course/${courseId}`);
}

function logout() {
  localStorage.removeItem("user");
  localStorage.removeItem("userRole");
  router.push("/");
}
</script>

<template>
  <div class="dashboard">
    <h1>Willkommen, {{ user.username }}</h1>
    <h2>Deine Lehrveranstaltungen</h2>

    <p v-if="error" class="error">{{ error }}</p>

    <ul v-if="courses.length">
      <li
          v-for="course in courses"
          :key="course.id"
          @click="openCourse(course.id)"
          class="course-item"
      >
        <strong>{{ course.name }}</strong>
        <span v-if="course.semester"> – {{ course.semester }}</span>
      </li>
    </ul>

    <p v-else>Keine Lehrveranstaltungen gefunden.</p>

    <button @click="logout">Logout</button>
  </div>
</template>

<style>
.dashboard {
  max-width: 700px;
  margin: 60px auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}
.error {
  color: red;
}
ul {
  list-style: none;
  padding: 0;
}
.course-item {
  padding: 10px;
  border-bottom: 1px solid #ccc;
  cursor: pointer;
  transition: background-color 0.2s;
}
.course-item:hover {
  background-color: #f1f5f9;
}
button {
  margin-top: 20px;
  padding: 10px 15px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
button:hover {
  background-color: #2563eb;
}
</style>

