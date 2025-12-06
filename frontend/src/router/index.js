import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../components/LoginView.vue";
import TeacherCourseView from "../components/TeacherCourseView.vue";
import StudentCourseView from "../components/StudentCourseView.vue";

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
        meta: { requiresAuth: true, role: "DOZENT" },
    },
    {
        path: "/student",
        name: "StudentDashboard",
        component: StudentCourseView,
        meta: { requiresAuth: true, role: "STUDENT" },
    },
    {
        path: "/teacher/course/:id",
        name: "CourseDashboard",
        component: () => import("../components/CourseDashboard.vue"),
        meta: { requiresAuth: true, role: "DOZENT" },
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

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    const role = localStorage.getItem("userRole");

    if (to.meta.requiresAuth && !role) {
        return next("/");
    }

    if (to.meta.role && role !== to.meta.role) {
        return next("/");
    }

    return next();
});

export default router;



