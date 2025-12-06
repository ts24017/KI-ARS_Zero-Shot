<script setup>
import { ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const courseId = route.params.courseId;
const lectureId = route.params.lectureId;

const courseName = ref(route.query.courseName || "");
const lectureNumber = ref(
    route.query.lectureNumber ? Number(route.query.lectureNumber) : null
);

const feedback = ref("");
const message = ref("");

// Dynamischer Titel
const lectureTitle = computed(() => {
  const base = lectureNumber.value
      ? `Vorlesungseinheit ${lectureNumber.value}`
      : "Vorlesungseinheit";

  return courseName.value ? `${base} - ${courseName.value}` : base;
});

function goBack() {
  router.push(`/student/course/${courseId}`);
}

async function submitFeedback() {
  const user = JSON.parse(localStorage.getItem("user"));
  if (!feedback.value.trim()) {
    message.value = "Bitte gib ein Feedback ein.";
    return;
  }

  const res = await fetch(`/api/feedback/lecture/${lectureId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      text: feedback.value,
      studentId: user.id
    }),
  });

  if (res.ok) {
    message.value = "Dein Feedback wurde erfolgreich gesendet. Vielen Dank!";
    feedback.value = "";
  } else {
    message.value = "Fehler beim Senden des Feedbacks.";
  }
}
</script>

<template>
  <div class="lecture-view">

    <button class="back-btn" @click="goBack">Zurück</button>

    <h1>{{ lectureTitle }}</h1>
    <p>Hier kannst du dein Feedback zu dieser Einheit eingeben.</p>

    <textarea
        v-model="feedback"
        placeholder="Schreibe hier dein Feedback..."
        rows="6"
        class="feedback-input"
    ></textarea>

    <button @click="submitFeedback" class="submit-btn">Feedback senden</button>

    <p v-if="message" class="message">{{ message }}</p>
  </div>
</template>

<style scoped>
.lecture-view {
  max-width: 700px;
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

textarea.feedback-input {
  width: 100%;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  padding: 12px;
  font-size: 1rem;
  margin-top: 12px;
  resize: vertical;
}

.submit-btn {
  background: #2563eb;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 1rem;
  margin-top: 16px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.submit-btn:hover {
  background: #1e40af;
}

.message {
  margin-top: 12px;
  color: #334155;
  font-size: 0.95rem;
}
</style>
