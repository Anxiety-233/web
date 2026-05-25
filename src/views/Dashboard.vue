<template>
  <div ref="dashboardRef" class="modern-dashboard">
    <div class="dashboard-header">
      <div class="header-left">
        <h3 class="main-title">📊 实验室设备控制台</h3>
        <p class="sub-title">实时监控与远程管理实验室关键设备</p>
      </div>
      <span class="role-tag" :class="userRole === 'admin' ? 'role-admin' : 'role-user'">
        {{ roleName }}
      </span>
    </div>

    <div class="stats-grid">
      <div class="stat-card online">
        <div class="stat-icon-wrapper">✅</div>
        <div class="stat-info">
          <div class="stat-label">在线设备</div>
          <div class="stat-value">{{ onlineCount }}<span class="stat-total">/ {{ totalCount }}</span></div>
        </div>
      </div>
      <div class="stat-card secure">
        <div class="stat-icon-wrapper">🔒</div>
        <div class="stat-info">
          <div class="stat-label">安全状态</div>
          <div class="stat-value sm"><span class="badge-success"></span> 运行正常</div>
        </div>
      </div>
      <div class="stat-card operations">
        <div class="stat-icon-wrapper">⚡</div>
        <div class="stat-info">
          <div class="stat-label">今日操作</div>
          <div class="stat-value">12<span class="stat-unit">次</span></div>
        </div>
      </div>
    </div>

    <div class="section-header">
      <h5 class="section-title">📱 设备列表</h5>
      <span class="device-count">共 <strong>{{ totalCount }}</strong> 台设备接入系统</span>
    </div>

    <div class="device-grid">
      <div class="device-card">
        <div class="card-header">
          <div class="device-title">
            <div class="device-avatar temp-avatar">🔥</div>
            <div class="title-text">
              <div class="name">温度传感器 T-01</div>
              <div class="id">ID: TEMP-2024-001</div>
            </div>
          </div>
          <span class="status-badge status-online">● 在线</span>
        </div>
        <div class="card-body">
          <div class="temp-display">
            <div ref="tempValueRef" class="temp-value" :style="{ color: tempColor }">
              {{ temperature.toFixed(1) }}<span class="temp-unit">°C</span>
            </div>
          </div>
          <div class="temp-progress-wrapper">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: (temperature / 50 * 100) + '%', background: tempColor }"></div>
            </div>
            <div class="progress-labels">
              <span>0°C</span>
              <span class="current-label" :style="{ color: tempColor }">当前</span>
              <span>50°C</span>
            </div>
          </div>
        </div>
        <div class="card-footer">
          <span class="meta-item">🔄 5秒自动刷新</span>
          <span class="tag tag-green">所有用户可见</span>
        </div>
      </div>
      <div class="device-card">
        <div class="card-header">
          <div class="device-title">
            <div class="device-avatar power-avatar" :class="{ 'is-on': isPowerOn }">⚡</div>
            <div class="title-text">
              <div class="name">核心电源控制器</div>
              <div class="id">ID: PWR-2024-CORE</div>
            </div>
          </div>
          <span class="status-badge" :class="isPowerOn ? 'status-online' : 'status-offline'">
            {{ isPowerOn ? '● 运行中' : '○ 已断电' }}
          </span>
        </div>
        <div class="card-body power-body">
          <div class="power-status-container">
            <div class="power-status-ring" :class="isPowerOn ? 'status-on' : 'status-off'">
              <span ref="powerIconRef" class="power-icon-inner">⏻</span>
            </div>
            <div class="status-text-area">
              <div class="status-label">当前状态</div>
              <div class="status-value" :class="isPowerOn ? 'text-success' : 'text-danger'">
                {{ isPowerOn ? '电源供应正常' : '电源已切断' }}
              </div>
            </div>
          </div>
        </div>
        <div class="card-footer power-footer">
          <div v-if="userRole === 'admin'" class="admin-actions">
            <div class="meta-info">
              <span class="meta-item permission">🔐 操作权限</span>
              <span class="tag tag-red">仅管理员</span>
            </div>
            <button class="action-btn" :class="isPowerOn ? 'btn-danger' : 'btn-success'" @click="togglePower">
              {{ isPowerOn ? '⏻ 紧急切断电源' : '✓ 恢复电源供应' }}
            </button>
          </div>
          <div v-else class="user-lock-notice">
            🔒 访问受限：需要管理员权限操作此设备
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import gsap from 'gsap';
import { getEquipmentList, controlEquipment, getTemperature } from '../api/equipment';

const userRole = ref(localStorage.getItem('userRole') || 'user');
const roleName = computed(() => userRole.value === 'admin' ? '管理员' : '普通用户');

const isPowerOn = ref(true);
const temperature = ref(24.5);
const equipments = ref([]);
const tempSensorId = ref(null);
const powerDeviceId = ref(null);

const dashboardRef = ref(null);
const tempValueRef = ref(null);
const powerIconRef = ref(null);

const totalCount = computed(() => equipments.value.length);
const onlineCount = computed(() => equipments.value.filter(e => e.status === 1).length);

const tempColor = computed(() => {
  if (temperature.value > 35) return '#f53f3f';
  if (temperature.value > 28) return '#ff7d00';
  return '#00b42a';
});

let tempInterval;

const loadEquipments = async () => {
  try {
    const page = await getEquipmentList(1, 50);
    equipments.value = page.records || [];
    const tempDevice = equipments.value.find(e =>
      e.equipmentName?.includes('温度') || e.equipmentCode?.toLowerCase().includes('temp')
    );
    const pwrDevice = equipments.value.find(e =>
      e.equipmentName?.includes('电源') || e.equipmentCode?.toLowerCase().includes('pwr')
    );
    if (tempDevice) tempSensorId.value = tempDevice.id;
    if (pwrDevice) {
      powerDeviceId.value = pwrDevice.id;
      isPowerOn.value = pwrDevice.status === 1;
    } else if (equipments.value.length > 0 && !tempDevice) {
      powerDeviceId.value = equipments.value[0].id;
      isPowerOn.value = equipments.value[0].status === 1;
    } else if (equipments.value.length > 1) {
      const fallback = equipments.value.find(e => e.id !== tempSensorId.value);
      if (fallback) {
        powerDeviceId.value = fallback.id;
        isPowerOn.value = fallback.status === 1;
      }
    }
  } catch (err) {
    console.warn('加载设备列表失败:', err);
  }
};

const pollTemperature = async () => {
  if (!tempSensorId.value) return;
  try {
    const data = await getTemperature(tempSensorId.value);
    temperature.value = data.temperature;
    if (tempValueRef.value) {
      gsap.fromTo(tempValueRef.value, { scale: 1.03 }, { scale: 1, duration: 0.3, ease: 'back.out(2)' });
    }
  } catch (err) {
    console.warn('获取温度失败:', err);
  }
};

onMounted(async () => {
  await loadEquipments();

  nextTick(() => {
    if (!dashboardRef.value) return;
    const q = gsap.utils.selector(dashboardRef.value);
    const tl = gsap.timeline({ defaults: { ease: 'power4.out', duration: 0.45, clearProps: 'all' } });
    tl.fromTo(q('.dashboard-header'), { y: -20, opacity: 0 }, { y: 0, opacity: 1 })
      .fromTo(q('.stat-card'), { y: 15, opacity: 0 }, { y: 0, opacity: 1, stagger: 0.05 }, '-=0.35')
      .fromTo(q('.section-header'), { opacity: 0 }, { opacity: 1 }, '-=0.3')
      .fromTo(q('.device-card'), { y: 20, opacity: 0 }, { y: 0, opacity: 1, stagger: 0.08 }, '-=0.35');
  });

  tempInterval = setInterval(pollTemperature, 5000);
  pollTemperature();
});

onUnmounted(() => {
  if (tempInterval) clearInterval(tempInterval);
});

const togglePower = async () => {
  if (!powerDeviceId.value) {
    alert('未找到电源设备，请确认后端已配置电源设备');
    return;
  }
  const newStatus = isPowerOn.value ? 0 : 1;
  try {
    await controlEquipment(powerDeviceId.value, newStatus);
    isPowerOn.value = !isPowerOn.value;
    await loadEquipments();
    await nextTick();
    if (powerIconRef.value) {
      gsap.fromTo(powerIconRef.value,
        { rotation: isPowerOn.value ? -180 : 180, scale: 0.8 },
        { rotation: 0, scale: 1, duration: 0.35, ease: 'back.out(1.2)' });
    }
  } catch (err) {
    alert('操作失败: ' + (err.message || '权限不足'));
  }
};
</script>

<style scoped>
.modern-dashboard { padding: 8px 0 24px; color: #4e5969; }
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.header-left { display: flex; flex-direction: column; }
.main-title { margin: 0; color: #1d2129; font-size: 20px; }
.sub-title { margin: 4px 0 0; font-size: 14px; color: #86909c; }
.role-tag { font-size: 13px; font-weight: 500; padding: 4px 12px; border-radius: 6px; }
.role-admin { background: #fff0f0; color: #f53f3f; }
.role-user { background: #e8f3ff; color: #165dff; }

.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 32px; }
.stat-card { border-radius: 12px; padding: 24px; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.04); border: 1px solid #f0f0f0; transition: all 0.3s; display: flex; align-items: center; gap: 20px; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 10px 24px rgba(0,0,0,0.08); }
.stat-icon-wrapper { width: 56px; height: 56px; border-radius: 16px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.stat-card.online .stat-icon-wrapper { background: rgba(0,180,42,0.1); }
.stat-card.secure .stat-icon-wrapper { background: rgba(22,93,255,0.1); }
.stat-card.operations .stat-icon-wrapper { background: rgba(255,125,0,0.1); }
.stat-label { font-size: 13px; color: #86909c; margin-bottom: 6px; }
.stat-value { font-size: 32px; font-weight: 700; color: #1d2129; line-height: 1.1; display: flex; align-items: baseline; }
.stat-value.sm { font-size: 16px; font-weight: 500; }
.stat-total, .stat-unit { font-size: 15px; font-weight: 400; color: #86909c; margin-left: 6px; }
.badge-success { display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #00b42a; margin-right: 6px; }

.section-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 16px; }
.section-title { margin: 0; font-size: 16px; color: #1d2129; }
.device-count { font-size: 13px; color: #86909c; }

.device-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }
.device-card { background: #fff; border-radius: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); border: 1px solid #f0f0f0; display: flex; flex-direction: column; transition: all 0.3s; }
.device-card:hover { box-shadow: 0 10px 24px rgba(0,0,0,0.08); }

.card-header { display: flex; justify-content: space-between; align-items: center; padding: 24px 24px 16px; border-bottom: 1px solid #f5f5f5; }
.device-title { display: flex; align-items: center; gap: 16px; }
.device-avatar { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; }
.temp-avatar { background: linear-gradient(135deg, #0fc6c2, #00b42a); }
.power-avatar { background: linear-gradient(135deg, #c0c0c0, #86909c); transition: all 0.3s; }
.power-avatar.is-on { background: linear-gradient(135deg, #f7ba1e, #ff7d00); }
.title-text .name { font-size: 17px; font-weight: 600; color: #1d2129; }
.title-text .id { font-size: 12px; color: #86909c; margin-top: 3px; font-family: monospace; }
.status-badge { font-size: 13px; font-weight: 500; }
.status-online { color: #00b42a; }
.status-offline { color: #f53f3f; }

.card-body { flex: 1; padding: 24px; }
.temp-display { display: flex; justify-content: center; margin: 8px 0 20px; }
.temp-value { font-size: 64px; font-weight: 800; line-height: 1; transition: color 0.3s; }
.temp-unit { font-size: 24px; color: #86909c; margin-left: 4px; font-weight: 300; }
.progress-bar { height: 12px; background: #f2f3f5; border-radius: 6px; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 6px; transition: width 0.5s, background 0.5s; }
.progress-labels { display: flex; justify-content: space-between; margin-top: 8px; font-size: 12px; color: #86909c; }
.current-label { font-weight: 600; }

.power-body { display: flex; justify-content: center; align-items: center; padding: 20px 24px; }
.power-status-container { display: flex; align-items: center; gap: 20px; width: 100%; padding: 16px 20px; }
.power-status-ring { width: 72px; height: 72px; border-radius: 50%; display: flex; justify-content: center; align-items: center; transition: all 0.3s; }
.power-icon-inner { font-size: 32px; display: inline-block; }
.status-on { background: #fff; color: #00b42a; border: 4px solid rgba(0,180,42,0.15); box-shadow: 0 6px 16px rgba(0,0,0,0.06); }
.status-off { background: #fff; color: #f53f3f; border: 4px solid rgba(245,63,63,0.15); }
.status-text-area { flex: 1; }
.status-label { font-size: 13px; color: #86909c; margin-bottom: 2px; }
.status-value { font-size: 18px; font-weight: 600; margin: 0; }
.text-success { color: #00b42a; }
.text-danger { color: #f53f3f; }

.card-footer { padding: 16px 24px; background: #fafafa; border-top: 1px solid #f5f5f5; margin-top: auto; display: flex; justify-content: space-between; align-items: center; }
.meta-item { font-size: 13px; color: #86909c; }
.meta-info { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.permission { color: #f53f3f; }
.tag { font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.tag-green { background: #e8ffea; color: #00b42a; }
.tag-red { background: #fff0f0; color: #f53f3f; }

.power-footer { min-height: 105px; flex-direction: column; justify-content: center; }
.admin-actions { width: 100%; }
.admin-actions .meta-info { margin-bottom: 14px; }
.action-btn { width: 100%; height: 44px; border-radius: 8px; font-weight: 600; font-size: 15px; border: none; cursor: pointer; transition: all 0.2s; }
.btn-danger { background: #f53f3f; color: #fff; }
.btn-danger:hover { background: #d92e2e; }
.btn-success { background: #00b42a; color: #fff; }
.btn-success:hover { background: #009a24; }
.user-lock-notice { display: flex; align-items: center; justify-content: center; gap: 10px; width: 100%; padding: 16px; background: #f5f5f5; border-radius: 8px; color: #86909c; font-size: 14px; }

@media (max-width: 992px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .stats-grid { grid-template-columns: 1fr; } .device-grid { grid-template-columns: 1fr; } }
</style>