<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { notificationApi } from '@/api/notification'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const unreadCount = ref(0)
const loadingUnread = ref(false)

const navItems = [
  { label: '首页', path: '/', name: 'home' },
  { label: '论坛', path: '/forum', name: 'forum-list' },
  { label: '资源库', path: '/resource', name: 'resource-list' },
  { label: '上传', path: '/upload', name: 'upload' },
  { label: '我的', path: '/me', name: 'me' },
]

const activePath = computed(() => {
  if (route.path.startsWith('/forum')) return '/forum'
  if (route.path.startsWith('/resource')) return '/resource'
  if (route.path.startsWith('/upload')) return '/upload'
  if (route.path.startsWith('/me')) return '/me'
  return '/'
})

async function fetchUnread() {
  try {
    loadingUnread.value = true
    unreadCount.value = await notificationApi.unreadCount()
  } finally {
    loadingUnread.value = false
  }
}

function goto(path: string) {
  if (path === route.path) return
  router.push(path)
}

function gotoNotifications() {
  router.push('/me')
}

function onLogout() {
  auth.logout()
  router.replace({ name: 'login' })
}

watch(
  () => auth.isAuthed,
  (authed) => {
    if (!authed) {
      unreadCount.value = 0
      return
    }
    fetchUnread()
  },
)

onMounted(() => {
  if (auth.isAuthed) {
    fetchUnread()
  }
})

const avatarText = computed(() => (auth.username?.slice(0, 1) || 'U').toUpperCase())
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <div class="topbar-inner">
        <div class="brand" @click="goto('/')">
          <div class="logo">CC</div>
          <div class="brand-text">
            <div class="title">校园职涯社区</div>
            <div class="sub">Campus Career Platform</div>
          </div>
        </div>

        <nav class="nav">
          <button
            v-for="item in navItems"
            :key="item.path"
            class="nav-item"
            :class="{ active: activePath === item.path }"
            type="button"
            @click="goto(item.path)"
          >
            {{ item.label }}
          </button>
        </nav>

        <div class="right-area">
          <button class="icon-btn" type="button" @click="gotoNotifications">
            <el-badge
              :value="unreadCount"
              :hidden="!unreadCount"
              :max="99"
              class="badge"
            >
              <span class="bell">🔔</span>
            </el-badge>
          </button>

          <div class="user" v-if="auth.isAuthed">
            <div class="avatar">
              <span>{{ avatarText }}</span>
            </div>
            <button class="logout" type="button" @click="onLogout">退出</button>
          </div>
        </div>
      </div>
    </header>

    <main class="main">
      <div class="main-inner">
        <router-view />
      </div>
    </main>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--ccp-page-bg);
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(14px);
  background: rgba(248, 250, 252, 0.8);
  border-bottom: 1px solid rgba(226, 232, 240, 0.7);
}

.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: var(--ccp-brand-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 14px;
}

.brand-text .title {
  font-size: 16px;
  font-weight: 700;
}

.brand-text .sub {
  font-size: 11px;
  color: var(--ccp-text-muted);
}

.nav {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.nav-item {
  border: 0;
  background: transparent;
  padding: 7px 14px;
  border-radius: 999px;
  font-size: 14px;
  color: var(--ccp-text-secondary);
  cursor: pointer;
  transition:
    background 0.15s ease,
    color 0.15s ease,
    transform 0.1s ease;
}

.nav-item:hover {
  background: rgba(148, 163, 184, 0.1);
  color: var(--ccp-text);
  transform: translateY(-1px);
}

.nav-item.active {
  background: var(--ccp-text);
  color: #f9fafb;
}

.right-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  border: 0;
  background: transparent;
  cursor: pointer;
  padding: 4px;
}

.bell {
  font-size: 18px;
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  background: var(--ccp-text);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.logout {
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid var(--ccp-card-border);
  background: var(--ccp-card-bg);
  font-size: 12px;
  color: var(--ccp-text-secondary);
  cursor: pointer;
}

.main {
  flex: 1;
  padding: 18px 16px 32px;
}

.main-inner {
  max-width: 1200px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .topbar-inner {
    gap: 8px;
    padding: 0 10px;
  }

  .brand-text .sub {
    display: none;
  }

  .nav {
    display: flex;
    gap: 6px;
    overflow-x: auto;
    flex: 1;
    min-width: 0;
    scrollbar-width: none;
  }

  .nav::-webkit-scrollbar {
    display: none;
  }

  .nav-item {
    white-space: nowrap;
    padding: 6px 10px;
    font-size: 12px;
    flex: none;
  }
}
</style>

