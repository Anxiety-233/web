<template>
  <div class="logs-container">
    <div ref="headerRef" class="logs-header">
      <div>
        <h4 class="logs-title">🔒 系统安全审计日志</h4>
        <p class="logs-subtitle">记录所有设备操作与安全事件，仅限管理员查看</p>
      </div>
      <div class="header-actions">
        <input v-model="searchQuery" class="search-input" placeholder="搜索日志..." />
        <button class="export-btn" @click="exportCSV">📥 导出 CSV</button>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card stat-card-blue">
        <div class="stat-card-icon">📄</div>
        <div class="stat-card-info">
          <div class="stat-card-label">总日志条数</div>
          <div class="stat-card-value">{{ stats.total }}</div>
        </div>
      </div>
      <div class="stat-card stat-card-green">
        <div class="stat-card-icon">✅</div>
        <div class="stat-card-info">
          <div class="stat-card-label">今日操作</div>
          <div class="stat-card-value" style="color:#00b42a">{{ stats.todayCount }}</div>
        </div>
      </div>
      <div class="stat-card stat-card-purple">
        <div class="stat-card-icon">👥</div>
        <div class="stat-card-info">
          <div class="stat-card-label">活跃用户</div>
          <div class="stat-card-value">{{ stats.activeUsers }}</div>
        </div>
      </div>
      <div class="stat-card stat-card-amber">
        <div class="stat-card-icon">🛡️</div>
        <div class="stat-card-info">
          <div class="stat-card-label">安全等级</div>
          <div class="stat-card-value stat-card-value-sm"><span class="badge-dot"></span> 安全</div>
        </div>
      </div>
    </div>

    <div ref="tableCardRef" class="table-card">
      <table class="logs-table">
        <thead>
          <tr>
            <th>ID</th><th>时间戳</th><th>操作用户</th><th>操作行为</th><th>来源 IP</th><th>浏览器</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in paginatedLogs" :key="log.id">
            <td><span class="id-tag">{{ log.id }}</span></td>
            <td>🕐 {{ log.operationTime }}</td>
            <td><span class="user-avatar" :class="log.username === 'admin' ? 'avatar-admin' : 'avatar-user'">{{ log.username.charAt(0).toUpperCase() }}</span> {{ log.username }}</td>
            <td><span class="action-tag" :class="getActionClass(log.operation)">{{ log.operation }}</span></td>
            <td><code>{{ log.ip }}</code></td>
            <td class="ua-cell">{{ log.browser }}</td>
          </tr>
        </tbody>
      </table>
      <div class="pagination">
        <button :disabled="currentPage <= 1" @click="currentPage--">上一页</button>
        <span>第 {{ currentPage }} / {{ totalPages }} 页，共 {{ totalLogs }} 条</span>
        <button :disabled="currentPage >= totalPages" @click="currentPage++">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import gsap from 'gsap';
import { getLogList, getLogStats, exportLogCSV } from '../api/logs';

const headerRef = ref(null);
const tableCardRef = ref(null);
const searchQuery = ref('');
const currentPage = ref(1);
const pageSize = 10;

const logs = ref([]);
const totalLogs = ref(0);
const stats = ref({ total: 0, todayCount: 0, activeUsers: 0 });

const paginatedLogs = computed(() => logs.value);
const totalPages = computed(() => Math.max(1, Math.ceil(totalLogs.value / pageSize)));

const getActionClass = (action) => {
  if (action.includes('电源') || action.includes('关闭')) return 'action-danger';
  if (action.includes('登录')) return 'action-success';
  if (action.includes('导出') || action.includes('查看')) return 'action-info';
  if (action.includes('修改') || action.includes('配置')) return 'action-purple';
  return '';
};

const loadLogs = async () => {
  try {
    const result = await getLogList(currentPage.value, pageSize, searchQuery.value);
    logs.value = result.records;
    totalLogs.value = result.total;
  } catch (e) {
    console.error('Failed to load logs:', e);
  }
};

const loadStats = async () => {
  try {
    const result = await getLogStats();
    stats.value = result;
  } catch (e) {
    console.error('Failed to load stats:', e);
  }
};

let searchTimer = null;
watch(searchQuery, () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    currentPage.value = 1;
    loadLogs();
  }, 300);
});

watch(currentPage, () => {
  loadLogs();
});

onMounted(async () => {
  await Promise.all([loadStats(), loadLogs()]);

  const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });
  tl.from(headerRef.value, { duration: 0.5, y: -20, opacity: 0 })
    .from('.stat-card', { duration: 0.4, y: 20, opacity: 0, stagger: 0.06 }, '-=0.2')
    .from('.table-card', { duration: 0.6, y: 30, opacity: 0 }, '-=0.2');
});

const exportCSV = () => {
  exportLogCSV();
};
</script>

<style scoped>
.logs-title { margin: 0; font-size: 18px; color: #1d2129; }
.logs-subtitle { margin: 4px 0 0; font-size: 14px; color: #86909c; }
.logs-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.search-input { padding: 8px 14px; border: 1px solid #e5e6eb; border-radius: 6px; font-size: 14px; width: 220px; outline: none; transition: border-color 0.2s; }
.search-input:focus { border-color: #165dff; }
.export-btn { background: #165dff; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; font-size: 14px; cursor: pointer; transition: background 0.2s; }
.export-btn:hover { background: #0e42d2; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
.stat-card { border-radius: 14px; padding: 20px 22px; display: flex; align-items: center; gap: 16px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); transition: transform 0.25s, box-shadow 0.25s; }
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
.stat-card-icon { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 22px; }
.stat-card-blue .stat-card-icon { background: #e8f3ff; }
.stat-card-green .stat-card-icon { background: #e8ffea; }
.stat-card-purple .stat-card-icon { background: #f5e8ff; }
.stat-card-amber .stat-card-icon { background: #fff7e8; }
.stat-card-label { font-size: 13px; color: #86909c; margin-bottom: 4px; }
.stat-card-value { font-size: 28px; font-weight: 700; color: #1d2129; line-height: 1.2; }
.stat-card-value-sm { font-size: 16px; font-weight: 500; display: flex; align-items: center; gap: 6px; }
.badge-dot { width: 8px; height: 8px; border-radius: 50%; background: #00b42a; display: inline-block; }
.table-card { background: #fff; border-radius: 16px; padding: 4px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); overflow: hidden; }
.logs-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.logs-table th { text-align: left; padding: 12px 16px; background: #fafafa; color: #86909c; font-weight: 500; border-bottom: 1px solid #f0f0f0; }
.logs-table td { padding: 12px 16px; border-bottom: 1px solid #f5f5f5; color: #4e5969; }
.logs-table tr:hover td { background: #f7f8fa; }
.ua-cell { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.id-tag { background: #e8f3ff; color: #165dff; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.user-avatar { display: inline-flex; align-items: center; justify-content: center; width: 24px; height: 24px; border-radius: 50%; font-size: 12px; color: #fff; margin-right: 6px; vertical-align: middle; }
.avatar-admin { background: #ff7d00; }
.avatar-user { background: #165dff; }
.action-tag { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.action-danger { background: #fff0f0; color: #f53f3f; }
.action-success { background: #e8ffea; color: #00b42a; }
.action-info { background: #e8f3ff; color: #165dff; }
.action-purple { background: #f5e8ff; color: #722ed1; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; padding: 16px; font-size: 14px; color: #4e5969; }
.pagination button { padding: 6px 14px; border: 1px solid #e5e6eb; border-radius: 4px; background: #fff; cursor: pointer; font-size: 13px; }
.pagination button:hover:not(:disabled) { border-color: #165dff; color: #165dff; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
