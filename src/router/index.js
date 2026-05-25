import { createRouter, createWebHashHistory } from 'vue-router';

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: () => import('../views/Login.vue') },
    {
        path: '/dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/logs',
        component: () => import('../views/Logs.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/dashboard'
    }
];

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    const isAuthenticated = !!localStorage.getItem('accessToken');
    const userRole = localStorage.getItem('userRole');

    if (to.path === '/login' && isAuthenticated) {
        next('/dashboard');
        return;
    }

    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login');
        return;
    }

    if (to.meta.requiresAdmin && userRole !== 'admin') {
        alert('权限不足，禁止越权访问！');
        next('/dashboard');
        return;
    }

    next();
});

export default router;