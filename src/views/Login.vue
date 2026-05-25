<template>
  <div class="login-container">
    <div class="bg-decoration">
      <div v-for="n in 6" :key="n" class="floating-orb" :class="`orb-${n}`" />
    </div>

    <div ref="loginCardRef" class="login-wrapper">
      <div class="modern-login-card">
        <div class="login-header">
          <div ref="iconRef" class="login-icon-wrapper">🔒</div>
          <h3 class="login-title">安全实验室设备系统</h3>
          <p class="login-subtitle">Lab Device Security System — 内部联调版</p>
        </div>

        <div class="custom-divider">
          <span class="divider-text">请输入账号登录</span>
        </div>

        <div class="login-form">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input v-model="username" class="form-input" placeholder="请输入用户名" @keyup.enter="handleLogin" />
          </div>
          <div class="form-group">
            <label class="form-label">密码</label>
            <input v-model="password" type="password" class="form-input" placeholder="请输入密码" @keyup.enter="handleLogin" />
          </div>
          <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
          <button class="login-btn user-btn" :disabled="isLoggingIn" @click="handleLogin">
            {{ isLoggingIn ? '登录中...' : '🔐 登录系统' }}
          </button>
        </div>

        <div class="login-footer">
          <div class="security-badge">🔐 JWT Token + 防重放机制保护</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import gsap from 'gsap';
import { login } from '../api/auth';
import { getRoleFromToken } from '../utils/jwt';

const router = useRouter();
const loginCardRef = ref(null);
const iconRef = ref(null);
const isLoggingIn = ref(false);
const username = ref('');
const password = ref('');
const errorMsg = ref('');

onMounted(() => {
  const tl = gsap.timeline({ defaults: { ease: 'power4.out' } });
  tl.from(loginCardRef.value, { duration: 1, y: 40, opacity: 0, scale: 0.98, clearProps: 'all' })
    .from(iconRef.value, { duration: 0.8, scale: 0.5, opacity: 0, rotation: -90, ease: 'back.out(2)', clearProps: 'all' }, '-=0.6')
    .from('.login-btn', { duration: 0.6, y: 15, opacity: 0, stagger: 0.1, clearProps: 'all' }, '-=0.5');
});

const handleLogin = async () => {
  if (isLoggingIn.value) return;
  if (!username.value || !password.value) {
    errorMsg.value = '请输入用户名和密码';
    return;
  }
  isLoggingIn.value = true;
  errorMsg.value = '';

  try {
    const data = await login(username.value, password.value);
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    const role = getRoleFromToken(data.accessToken);
    localStorage.setItem('userRole', role);
    localStorage.setItem('username', username.value);
    router.push('/dashboard');
  } catch (err) {
    errorMsg.value = err.message || '登录失败，请检查用户名和密码';
    isLoggingIn.value = false;
  }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f4f7fc;
  position: relative;
  overflow: hidden;
}

.bg-decoration { position: absolute; inset: 0; pointer-events: none; overflow: hidden; }
.floating-orb { position: absolute; border-radius: 50%; filter: blur(60px); opacity: 0.5; animation: float 10s ease-in-out infinite alternate; }
.orb-1 { width: 400px; height: 400px; background: rgba(22, 93, 255, 0.4); top: -10%; left: -10%; }
.orb-2 { width: 350px; height: 350px; background: rgba(114, 46, 209, 0.3); bottom: -10%; right: -5%; animation-delay: -2s; }
.orb-3 { width: 250px; height: 250px; background: rgba(15, 198, 194, 0.3); top: 40%; right: 20%; animation-delay: -4s; }
.orb-4 { width: 300px; height: 300px; background: rgba(247, 186, 30, 0.2); bottom: 20%; left: 15%; animation-delay: -6s; }
.orb-5 { width: 200px; height: 200px; background: rgba(22, 93, 255, 0.2); top: 10%; right: 30%; animation-delay: -8s; }
.orb-6 { width: 150px; height: 150px; background: rgba(245, 63, 63, 0.15); top: 50%; left: 40%; animation-delay: -10s; }
@keyframes float { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(30px, -50px) scale(1.1); } }

.login-wrapper { z-index: 1; width: 440px; padding: 0 20px; }

.modern-login-card {
  border-radius: 20px;
  box-shadow: 0 24px 48px rgba(0,0,0,0.04), 0 8px 16px rgba(0,0,0,0.04);
  padding: 40px 32px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px) saturate(150%);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.login-header { text-align: center; margin-bottom: 8px; }
.login-icon-wrapper {
  display: inline-flex; justify-content: center; align-items: center;
  width: 88px; height: 88px; border-radius: 24px;
  background: linear-gradient(135deg, #ffffff, #f0f5ff);
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.15);
  margin-bottom: 20px; font-size: 36px;
}
.login-title { margin: 0 0 6px; font-weight: 700; color: #1d2129; font-size: 22px; }
.login-subtitle { margin: 0; font-size: 14px; color: #86909c; }

.custom-divider {
  text-align: center; margin: 28px 0; position: relative;
  border-top: 1px solid rgba(229, 230, 235, 0.6);
  padding-top: 0;
}
.divider-text {
  position: relative; top: -10px;
  background: rgba(255, 255, 255, 0.85);
  padding: 0 12px; font-size: 13px; color: #86909c;
}

.login-actions { display: flex; flex-direction: column; gap: 14px; margin-top: 8px; }

.login-form { display: flex; flex-direction: column; gap: 16px; margin-top: 8px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 13px; font-weight: 500; color: #4e5969; }
.form-input {
  height: 44px; padding: 0 14px; border: 1px solid #e5e6eb; border-radius: 8px;
  font-size: 14px; outline: none; transition: border-color 0.2s;
}
.form-input:focus { border-color: #165dff; box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1); }
.error-msg { margin: 0; font-size: 13px; color: #f53f3f; }

.login-btn {
  height: 48px; border-radius: 10px; font-size: 15px; font-weight: 600;
  border: none; cursor: pointer; width: 100%;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.user-btn {
  background: #165dff; color: #fff;
  box-shadow: 0 6px 16px rgba(22, 93, 255, 0.2);
}
.user-btn:hover:not(:disabled) { box-shadow: 0 8px 24px rgba(22, 93, 255, 0.3); transform: translateY(-1px); }

.admin-btn {
  background: linear-gradient(135deg, #f7ba1e, #f7931e); color: #fff;
  box-shadow: 0 6px 16px rgba(247, 186, 30, 0.2);
}
.admin-btn:hover:not(:disabled) { box-shadow: 0 8px 24px rgba(247, 186, 30, 0.3); transform: translateY(-1px); }

.login-footer { display: flex; justify-content: center; margin-top: 32px; }
.security-badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px; background: rgba(242, 243, 245, 0.6);
  border-radius: 20px; font-size: 12px; color: #86909c;
}
</style>
