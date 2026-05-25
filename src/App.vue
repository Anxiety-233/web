<template>
  <div id="app" class="app-layout">
    <header v-show="isLoggedIn" class="nav-header">
      <div class="nav-content">
        <div class="nav-brand">
          <span class="brand-icon">🔒</span>
          <span class="brand-text">安全实验室设备系统</span>
        </div>
        <div class="nav-links">
          <router-link to="/dashboard" class="nav-link-btn" active-class="nav-active">
            📊 设备面板
          </router-link>
          <router-link v-if="userRole === 'admin'" to="/logs" class="nav-link-btn" active-class="nav-active">
            📄 操作日志
          </router-link>
          <span class="nav-divider"></span>
          <span class="role-tag">{{ userRole === 'admin' ? '管理员' : '用户' }}</span>
          <button class="logout-btn" @click="logout">退出登录</button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>

    <footer v-show="isLoggedIn" class="app-footer">
      <span>Lab Security System v1.0 — 安全实验室设备管理系统</span>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { logout as logoutApi } from './api/auth';

const router = useRouter();

const accessToken = ref(localStorage.getItem('accessToken'));
const userRoleValue = ref(localStorage.getItem('userRole'));

const isLoggedIn = computed(() => !!accessToken.value);
const userRole = computed(() => userRoleValue.value);

router.afterEach(() => {
  accessToken.value = localStorage.getItem('accessToken');
  userRoleValue.value = localStorage.getItem('userRole');
});

const logout = async () => {
  try { await logoutApi(); } catch {}
  localStorage.clear();
  accessToken.value = null;
  userRoleValue.value = null;
  router.push('/login');
};
</script>

<style>
body {
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  margin: 0;
  background-color: #f2f3f5;
}

.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.nav-header {
  background: linear-gradient(135deg, #165dff 0%, #0e42d2 100%);
  padding: 0 32px;
  height: 56px;
  box-shadow: 0 2px 12px rgba(22, 93, 255, 0.25);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  height: 56px;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-icon { font-size: 20px; }
.brand-text { font-size: 16px; font-weight: 600; color: #fff; letter-spacing: 0.5px; }

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link-btn {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  text-decoration: none;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.2s;
}

.nav-link-btn:hover { color: #fff; background: rgba(255, 255, 255, 0.15); }
.nav-active { color: #fff !important; background: rgba(255, 255, 255, 0.2) !important; }

.nav-divider {
  width: 1px;
  height: 20px;
  background: rgba(255, 255, 255, 0.3);
  margin: 0 4px;
}

.role-tag {
  font-size: 12px;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  padding: 3px 10px;
  border-radius: 4px;
}

.logout-btn {
  background: #f53f3f;
  color: #fff;
  border: none;
  padding: 5px 12px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}
.logout-btn:hover { background: #d92e2e; }

.main-content {
  padding: 28px 32px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
  flex: 1;
}

.app-footer {
  text-align: center;
  padding: 16px;
  color: #86909c;
  font-size: 13px;
}

.page-fade-enter-active, .page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.page-fade-enter-from { opacity: 0; transform: translateY(8px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
