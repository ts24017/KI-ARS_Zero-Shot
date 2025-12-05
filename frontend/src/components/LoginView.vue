<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const username = ref("");
const password = ref("");
const error = ref("");

async function handleLogin() {
  error.value = "";

  try {
    const res = await fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username: username.value,
        password: password.value,
      }),
    });

    if (res.status === 401) {
      error.value = "Benutzername oder Passwort ist falsch.";
      return;
    }

    if (!res.ok) {
      error.value = "Serverfehler. Bitte später erneut versuchen.";
      return;
    }

    const data = await res.json();
    console.log("Login erfolgreich:", data);

    localStorage.setItem("user", JSON.stringify(data));
    localStorage.setItem("userRole", data.role?.toUpperCase());

    if (data.role?.toUpperCase() === "DOZENT") {
      router.push("/teacher");
    } else if (data.role?.toUpperCase() === "STUDENT") {
      router.push("/student");
    } else {
      error.value = "Unbekannte Rolle – Zugriff verweigert.";
    }

  } catch (e) {
    console.error("Fehler beim Login:", e);
    error.value = "Verbindung zum Server fehlgeschlagen.";
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <h2>Login zum KI-ARS: Zero-Shot Feedback System</h2>

      <form @submit.prevent="handleLogin">
        <input v-model="username" placeholder="Username" required />
        <input v-model="password" type="password" placeholder="Passwort" required />
        <button type="submit">Anmelden</button>
      </form>

      <p v-if="error" class="error">{{ error }}</p>

      <div class="login-info">
        <h3>Demo-Accounts</h3>

        <div class="login-roles">
          <div class="login-role-block">
            <h4>Dozent</h4>
            <div class="line">
              <span class="label">Benutzername:</span>
              <span class="value">Dr. Herrmann</span>
            </div>
            <div class="line">
              <span class="label">Passwort:</span>
              <span class="value">1234</span>
            </div>
          </div>

          <div class="login-role-block">
            <h4>Studierende</h4>
            <div class="line">
              <span class="label">Max</span>
              <span class="value">Passwort: 1234</span>
            </div>
            <div class="line">
              <span class="label">Laura</span>
              <span class="value">Passwort: 1234</span>
            </div>
            <div class="line">
              <span class="label">Tobias</span>
              <span class="value">Passwort: 1234</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #eef2f7, #f8fafc);
  font-family: "Segoe UI", Arial, sans-serif;
}

.login-container {
  width: 380px;
  background: white;
  border-radius: 12px;
  padding: 36px 40px 28px 40px;
  text-align: center;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.login-container h2 {
  color: #1e3a8a;
  font-weight: 600;
  margin-bottom: 24px;
}

.login-container input {
  display: block;
  width: 100%;
  box-sizing: border-box;
  padding: 11px 12px;
  margin-bottom: 14px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  font-size: 15px;
  transition: all 0.2s ease;
}

.login-container input:focus {
  border-color: #3b82f6;
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
}

.login-container button {
  width: 100%;
  padding: 11px;
  background-color: #2563eb;
  border: none;
  color: white;
  border-radius: 6px;
  font-weight: 500;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.1s ease;
}

.login-container button:hover {
  background-color: #1d4ed8;
  transform: translateY(-1px);
}

.error {
  color: #dc2626;
  font-size: 14px;
  margin-top: 10px;
}

.login-info {
  margin-top: 22px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f1f5f9;
  text-align: left;
  font-size: 13px;
  color: #0f172a;
  border: 1px solid #e2e8f0;
}

.login-info h3 {
  margin: 0 0 6px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  text-align: left;
}

.login-info p {
  margin: 0 0 10px 0;
}

.login-roles {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.login-role-block {
  padding-top: 4px;
  border-top: 1px dashed #cbd5e1;
}

.login-role-block:first-of-type {
  border-top: none;
}

.login-role-block h4 {
  font-size: 13px;
  margin: 4px 0 4px 0;
  font-weight: 600;
  color: #111827;
}

.line {
  display: flex;
  justify-content: space-between;
  font-size: 12.5px;
  margin: 2px 0;
}

.label {
  font-weight: 500;
  color: #111827;
}

.value {
  color: #111827;
}
</style>



