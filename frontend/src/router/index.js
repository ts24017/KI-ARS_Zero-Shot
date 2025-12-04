import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../components/LoginView.vue";
import TeacherCourseView from "../components/TeacherCourseView.vue";
import StudentCourseView from "../components/StudentCourseView.vue";

// 🔹 Prüfen, ob jemand eingeloggt ist
const isAuthenticated = () => !!localStorage.getItem("userRole");

// 🔹 Routen-Definition
const routes = [
    {
        path: "/",
        name: "Login",
        component: LoginView,
    },
    {
        path: "/teacher",
        name: "TeacherDashboard",
        component: TeacherCourseView,
        meta: { requiresAuth: true, role: "DOZENT" }, // 👈 Großbuchstaben wie im Backend
    },
    {
        path: "/student",
        name: "StudentDashboard",
        component: StudentCourseView,
        meta: { requiresAuth: true, role: "STUDENT" }, // 👈 dito
    },
    {
        path: "/teacher/course/:id",
        name: "CourseDashboard",
        component: () => import("../components/CourseDashboard.vue"),
        meta: { requiresAuth: true, role: "DOZENT" }, // 👈 dito
    },
    {
        path: "/student/course/:id",
        name: "StudentCourseView",
        component: () => import("../components/StudentLectureView.vue"),
        meta: { requiresAuth: true, role: "STUDENT" },
    },
    {
        path: "/student/course/:courseId/lecture/:lectureId",
        name: "StudentLectureView",
        component: () => import("../components/StudentFeedbackView.vue"),
        meta: { requiresAuth: true, role: "STUDENT" },
    },
    {
        path: "/teacher/lecture/:id",
        name: "LectureDashboard",
        component: () => import("../components/LectureDashboard.vue"),
        meta: { requiresAuth: true, role: "DOZENT" },
    },
    {
        path: "/:catchAll(.*)",
        redirect: "/",
    },
];

// 🔹 Router erstellen
const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 🔹 Navigation Guards (Zugriffsschutz)
router.beforeEach((to, from, next) => {
    const role = localStorage.getItem("userRole");

    // Wenn Route Auth braucht, aber niemand eingeloggt ist
    if (to.meta.requiresAuth && !role) {
        next("/");
    }
    // Wenn falsche Rolle versucht, falsche Seite zu öffnen
    else if (to.meta.role && role !== to.meta.role) {
        next("/");
    }
    // Alles korrekt
    else {
        next();
    }
});

export default router;


