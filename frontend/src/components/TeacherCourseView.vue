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

    <button class="logout-btn" @click="logout">Abmelden</button>

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
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 700px;
  margin: 60px auto;
  padding: 20px;
  font-family: "Inter", sans-serif;
  position: relative;
}

.logout-btn {
  position: absolute;
  top: 10px;
  right: 10px;

  background: #ef4444;
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.25s ease;
}

.logout-btn:hover {
  background: #dc2626;
}

.error {
  color: red;
}

ul {
  list-style: none;
  padding: 0;
}

.course-item {
  padding: 12px 10px;
  border-bottom: 1px solid #dbe3ec;
  cursor: pointer;
  transition: background-color 0.2s;
  border-radius: 6px;
}

.course-item:hover {
  background-color: #e0f2fe;
}
</style>

