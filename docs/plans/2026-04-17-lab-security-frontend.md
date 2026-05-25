# Lab Security Frontend Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a Vue SPA frontend for the lab device security system with login, RBAC-aware device management UI, audit logs, mock API boundaries, and demoable auth/security states ready for later backend integration.

**Architecture:** Extend the current Vue + Vite starter into a routed SPA with a global application shell, composable state modules, and a mock service layer that simulates JWT lifecycle, RBAC checks, replay metadata, and audit export behavior. Keep backend assumptions isolated in `src/services` so later integration only replaces service implementations, not view logic.

**Tech Stack:** Vue, Vue Router, Vitest, Vue Test Utils, plain CSS, mock service modules in JavaScript

---

### Task 1: Establish auth and demo session state

**Files:**
- Create: `src/lib/demoData.js`
- Create: `src/services/securityService.js`
- Create: `src/services/authService.js`
- Create: `src/composables/useSession.js`
- Test: `src/__tests__/session.spec.js`

**Step 1: Write the failing test**

```js
import { describe, expect, it } from 'vitest'
import { createSessionStore } from '../composables/useSession'

describe('session store', () => {
  it('logs in an admin user and exposes token metadata', async () => {
    const session = createSessionStore()

    await session.login({ username: 'admin', password: 'admin123' })

    expect(session.user.value.role).toBe('admin')
    expect(session.accessToken.value).toContain('mock-access')
    expect(session.tokenExpiresAt.value).toBeTypeOf('number')
  })
})
```

**Step 2: Run test to verify it fails**

Run: `npm run test:unit -- src/__tests__/session.spec.js`

Expected: FAIL because `createSessionStore` does not exist yet.

**Step 3: Write minimal implementation**

```js
export function createSessionStore() {
  const user = ref(null)
  const accessToken = ref('')
  const tokenExpiresAt = ref(0)

  async function login(credentials) {
    const result = await authService.login(credentials)
    user.value = result.user
    accessToken.value = result.accessToken
    tokenExpiresAt.value = result.tokenExpiresAt
  }

  return { user, accessToken, tokenExpiresAt, login }
}
```

**Step 4: Run test to verify it passes**

Run: `npm run test:unit -- src/__tests__/session.spec.js`

Expected: PASS

**Step 5: Commit**

```bash
git add src/lib/demoData.js src/services/securityService.js src/services/authService.js src/composables/useSession.js src/__tests__/session.spec.js
git commit -m "feat: add mock auth session state"
```

### Task 2: Add router structure and route guards

**Files:**
- Modify: `src/router/index.js`
- Create: `src/views/LoginView.vue`
- Create: `src/views/ForbiddenView.vue`
- Test: `src/__tests__/router.spec.js`

**Step 1: Write the failing test**

```js
import { describe, expect, it } from 'vitest'
import { createAppRouter } from '../router'
import { createSessionStore } from '../composables/useSession'

describe('router guards', () => {
  it('redirects ordinary users away from logs', async () => {
    const session = createSessionStore()
    await session.login({ username: 'viewer', password: 'viewer123' })
    const router = createAppRouter(session)

    await router.push('/logs')

    expect(router.currentRoute.value.fullPath).toBe('/forbidden')
  })
})
```

**Step 2: Run test to verify it fails**

Run: `npm run test:unit -- src/__tests__/router.spec.js`

Expected: FAIL because the router factory and route guards do not exist.

**Step 3: Write minimal implementation**

```js
const routes = [
  { path: '/login', component: LoginView, meta: { guestOnly: true } },
  { path: '/dashboard', component: DashboardView, meta: { requiresAuth: true } },
  { path: '/logs', component: LogsView, meta: { requiresAuth: true, role: 'admin' } },
  { path: '/forbidden', component: ForbiddenView },
]

router.beforeEach((to) => {
  if (to.meta.role && session.user.value?.role !== to.meta.role) {
    return '/forbidden'
  }
})
```

**Step 4: Run test to verify it passes**

Run: `npm run test:unit -- src/__tests__/router.spec.js`

Expected: PASS

**Step 5: Commit**

```bash
git add src/router/index.js src/views/LoginView.vue src/views/ForbiddenView.vue src/__tests__/router.spec.js
git commit -m "feat: add auth-aware router guards"
```

### Task 3: Build the global app shell and login experience

**Files:**
- Modify: `src/App.vue`
- Modify: `src/main.js`
- Create: `src/styles/theme.css`
- Create: `src/components/AppShell.vue`
- Create: `src/components/SecurityStatusBar.vue`
- Test: `src/__tests__/app-shell.spec.js`

**Step 1: Write the failing test**

```js
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '../App.vue'

describe('app shell', () => {
  it('shows the security status bar when authenticated', () => {
    const wrapper = mount(App, {
      global: {
        provide: {
          session: {
            isAuthenticated: { value: true },
            user: { value: { name: 'Admin', role: 'admin' } },
            tokenMinutesLeft: { value: 13 },
          },
        },
      },
    })

    expect(wrapper.text()).toContain('Access Token')
    expect(wrapper.text()).toContain('admin')
  })
})
```

**Step 2: Run test to verify it fails**

Run: `npm run test:unit -- src/__tests__/app-shell.spec.js`

Expected: FAIL because `App.vue` still renders the starter content.

**Step 3: Write minimal implementation**

```vue
<template>
  <AppShell>
    <RouterView />
  </AppShell>
</template>
```

**Step 4: Run test to verify it passes**

Run: `npm run test:unit -- src/__tests__/app-shell.spec.js`

Expected: PASS

**Step 5: Commit**

```bash
git add src/App.vue src/main.js src/styles/theme.css src/components/AppShell.vue src/components/SecurityStatusBar.vue src/__tests__/app-shell.spec.js
git commit -m "feat: add application shell and login layout"
```

### Task 4: Build the dashboard with role-sensitive device cards

**Files:**
- Create: `src/services/deviceService.js`
- Create: `src/components/DeviceCard.vue`
- Create: `src/views/DashboardView.vue`
- Test: `src/__tests__/dashboard.spec.js`

**Step 1: Write the failing test**

```js
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardView from '../views/DashboardView.vue'

describe('dashboard role UI', () => {
  it('disables control actions for ordinary users', async () => {
    const wrapper = mount(DashboardView, {
      global: {
        provide: {
          session: {
            user: { value: { role: 'viewer' } },
          },
        },
      },
    })

    expect(wrapper.text()).toContain('只读')
    expect(wrapper.find('[data-testid="device-action"]').attributes('disabled')).toBeDefined()
  })
})
```

**Step 2: Run test to verify it fails**

Run: `npm run test:unit -- src/__tests__/dashboard.spec.js`

Expected: FAIL because the dashboard and device cards do not exist.

**Step 3: Write minimal implementation**

```vue
<button data-testid="device-action" :disabled="!isAdmin">
  {{ isAdmin ? '控制设备' : '只读访问' }}
</button>
```

**Step 4: Run test to verify it passes**

Run: `npm run test:unit -- src/__tests__/dashboard.spec.js`

Expected: PASS

**Step 5: Commit**

```bash
git add src/services/deviceService.js src/components/DeviceCard.vue src/views/DashboardView.vue src/__tests__/dashboard.spec.js
git commit -m "feat: add dashboard device overview"
```

### Task 5: Build device detail view with control panel states

**Files:**
- Create: `src/views/DeviceDetailView.vue`
- Modify: `src/router/index.js`
- Test: `src/__tests__/device-detail.spec.js`

**Step 1: Write the failing test**

```js
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import DeviceDetailView from '../views/DeviceDetailView.vue'

describe('device detail permissions', () => {
  it('shows an RBAC explanation when viewer cannot control the device', () => {
    const wrapper = mount(DeviceDetailView, {
      props: { id: 'switch-01' },
      global: {
        provide: {
          session: {
            user: { value: { role: 'viewer' } },
          },
        },
      },
    })

    expect(wrapper.text()).toContain('RBAC')
    expect(wrapper.text()).toContain('当前角色仅允许查看')
  })
})
```

**Step 2: Run test to verify it fails**

Run: `npm run test:unit -- src/__tests__/device-detail.spec.js`

Expected: FAIL because the device detail page does not exist.

**Step 3: Write minimal implementation**

```vue
<section v-if="!isAdmin">
  <p>RBAC: 当前角色仅允许查看，不能执行控制操作。</p>
</section>
```

**Step 4: Run test to verify it passes**

Run: `npm run test:unit -- src/__tests__/device-detail.spec.js`

Expected: PASS

**Step 5: Commit**

```bash
git add src/views/DeviceDetailView.vue src/router/index.js src/__tests__/device-detail.spec.js
git commit -m "feat: add device detail permission states"
```

### Task 6: Build the audit log view and CSV export

**Files:**
- Create: `src/services/auditService.js`
- Create: `src/views/LogsView.vue`
- Test: `src/__tests__/logs.spec.js`

**Step 1: Write the failing test**

```js
import { describe, expect, it } from 'vitest'
import { exportLogsCsv } from '../services/auditService'

describe('audit export', () => {
  it('returns CSV content with audit headers', async () => {
    const csv = await exportLogsCsv()

    expect(csv).toContain('user,time,action,ip,userAgent,result,nonce,timestampStatus')
  })
})
```

**Step 2: Run test to verify it fails**

Run: `npm run test:unit -- src/__tests__/logs.spec.js`

Expected: FAIL because the audit service does not exist.

**Step 3: Write minimal implementation**

```js
export async function exportLogsCsv() {
  return 'user,time,action,ip,userAgent,result,nonce,timestampStatus'
}
```

**Step 4: Run test to verify it passes**

Run: `npm run test:unit -- src/__tests__/logs.spec.js`

Expected: PASS

**Step 5: Commit**

```bash
git add src/services/auditService.js src/views/LogsView.vue src/__tests__/logs.spec.js
git commit -m "feat: add audit log view and csv export"
```

### Task 7: Add demo mode switching and security state simulations

**Files:**
- Create: `src/composables/useDemoMode.js`
- Modify: `src/composables/useSession.js`
- Modify: `src/services/authService.js`
- Modify: `src/services/deviceService.js`
- Modify: `src/services/auditService.js`
- Modify: `src/components/SecurityStatusBar.vue`
- Test: `src/__tests__/demo-mode.spec.js`

**Step 1: Write the failing test**

```js
import { describe, expect, it } from 'vitest'
import { createDemoModeStore } from '../composables/useDemoMode'
import { createSessionStore } from '../composables/useSession'

describe('demo security modes', () => {
  it('forces refresh failure when token expired mode is enabled', async () => {
    const demoMode = createDemoModeStore()
    demoMode.setMode('token-expired')
    const session = createSessionStore({ demoMode })

    await expect(session.tryRefresh()).rejects.toThrow('SESSION_EXPIRED')
  })
})
```

**Step 2: Run test to verify it fails**

Run: `npm run test:unit -- src/__tests__/demo-mode.spec.js`

Expected: FAIL because demo mode state and refresh branching do not exist.

**Step 3: Write minimal implementation**

```js
if (demoMode.mode.value === 'token-expired') {
  throw new Error('SESSION_EXPIRED')
}
```

**Step 4: Run test to verify it passes**

Run: `npm run test:unit -- src/__tests__/demo-mode.spec.js`

Expected: PASS

**Step 5: Commit**

```bash
git add src/composables/useDemoMode.js src/composables/useSession.js src/services/authService.js src/services/deviceService.js src/services/auditService.js src/components/SecurityStatusBar.vue src/__tests__/demo-mode.spec.js
git commit -m "feat: add demo security state switching"
```

### Task 8: Refine styling, accessibility, and integration tests

**Files:**
- Modify: `src/styles/theme.css`
- Modify: `src/views/LoginView.vue`
- Modify: `src/views/DashboardView.vue`
- Modify: `src/views/DeviceDetailView.vue`
- Modify: `src/views/LogsView.vue`
- Modify: `src/__tests__/App.spec.js`

**Step 1: Write the failing test**

```js
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '../App.vue'

describe('application integration', () => {
  it('renders the lab console shell instead of starter text', () => {
    const wrapper = mount(App)

    expect(wrapper.text()).not.toContain('You did it!')
    expect(wrapper.text()).toContain('Laboratory Device Security Console')
  })
})
```

**Step 2: Run test to verify it fails**

Run: `npm run test:unit -- src/__tests__/App.spec.js`

Expected: FAIL because the final shell title and view composition are not complete yet.

**Step 3: Write minimal implementation**

```vue
<h1>Laboratory Device Security Console</h1>
```

**Step 4: Run test to verify it passes**

Run: `npm run test:unit -- src/__tests__/App.spec.js`

Expected: PASS

**Step 5: Run the full suite and build**

Run: `npm run test:unit`
Expected: PASS

Run: `npm run build`
Expected: PASS with Vite production output

**Step 6: Commit**

```bash
git add src/styles/theme.css src/views/LoginView.vue src/views/DashboardView.vue src/views/DeviceDetailView.vue src/views/LogsView.vue src/__tests__/App.spec.js
git commit -m "feat: finalize lab security frontend"
```
