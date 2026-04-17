<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import { useMessageStore } from '@/stores/message'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const notification = useNotificationStore()
const message = useMessageStore()

const unreadCount = computed(() => notification.unreadCount)

const navItems = computed(() => [
  { label: '首页', path: '/', name: 'home' },
  { label: '论坛', path: '/forum', name: 'forum-list' },
  { label: '资源库', path: '/resource', name: 'resource-list' },
  { label: '学涯助手', path: '/qa', name: 'smart-qa' },
  { label: '私信', path: '/message', name: 'message-center' },
  { label: '上传', path: '/upload', name: 'upload' },
  ...(auth.isAdmin
    ? [
        { label: '帖子审核', path: '/admin/posts', name: 'admin-post-review' },
        { label: '举报管理', path: '/admin/reports', name: 'admin-report-review' },
        { label: '用户管理', path: '/admin/users', name: 'admin-user-review' },
      ]
    : []),
])

const activePath = computed(() => {
  if (route.path.startsWith('/forum')) return '/forum'
  if (route.path.startsWith('/resource')) return '/resource'
  if (route.path.startsWith('/qa') || route.path.startsWith('/qwen')) return '/qa'
  if (route.path.startsWith('/message')) return '/message'
  if (route.path.startsWith('/upload')) return '/upload'
  if (route.path.startsWith('/admin/posts')) return '/admin/posts'
  if (route.path.startsWith('/admin/reports')) return '/admin/reports'
  if (route.path.startsWith('/admin/users')) return '/admin/users'
  return '/'
})

function goto(path: string) {
  if (path === route.path) return
  router.push(path)
}

function gotoNotifications() {
  router.push({ name: 'notification' })
}

function onLogout() {
  auth.logout()
  router.replace({ name: 'login' })
}

function gotoMe() {
  router.push({ name: 'me' })
}

watch(
  () => auth.isAuthed,
  (authed) => {
    if (!authed) {
      notification.resetUnread()
      message.resetUnread()
      return
    }
    notification.fetchUnreadCount()
    message.fetchUnreadCount()
  },
)

onMounted(() => {
  if (auth.isAuthed) {
    notification.fetchUnreadCount()
    message.fetchUnreadCount()
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

          <el-dropdown v-if="auth.isAuthed" trigger="click" placement="bottom-end">
            <button type="button" class="user-btn">
              <div class="avatar"><span>{{ avatarText }}</span></div>
              <span class="username">{{ auth.username || '我的' }}</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="gotoMe">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="onLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="main">
      <div class="main-inner">
        <RouterView v-slot="{ Component, route: childRoute }">
          <transition name="page-slide" mode="out-in">
            <keep-alive v-if="childRoute.meta?.keepAlive">
              <component
                :is="Component"
                :key="String(childRoute.name ?? childRoute.path)"
              />
            </keep-alive>
            <component
              :is="Component"
              v-else
              :key="childRoute.fullPath"
            />
          </transition>
        </RouterView>
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
  backdrop-filter: blur(10px);
  background: rgba(248, 249, 250, 0.82);
  border-bottom: 1px solid var(--ccp-card-border);
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
  background: var(--ccp-primary-gradient);
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
  transition: background var(--ccp-fast), color var(--ccp-fast), transform var(--ccp-fast);
  will-change: transform;
}

.nav-item:hover {
  background: var(--ccp-primary-soft);
  color: var(--ccp-text);
  transform: translateY(-1px);
}

.nav-item.active {
  background: var(--ccp-primary-gradient);
  color: #fff;
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
  border-radius: 8px;
  transition: background 0.2s ease;
}

.icon-btn:hover {
  background: var(--ccp-primary-soft);
}

.bell {
  font-size: 18px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  background: var(--ccp-primary-gradient);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
}

.user-btn {
  height: 34px;
  border: 1px solid var(--ccp-card-border);
  background: #fff;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 8px 0 2px;
  cursor: pointer;
}

.user-btn:hover {
  border-color: #d7def0;
  box-shadow: 0 4px 12px rgba(42, 92, 255, 0.12);
}

.username {
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  color: var(--ccp-text-secondary);
}

.main {
  flex: 1;
  padding: 18px 16px 32px;
}

.main-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.page-slide-enter-active,
.page-slide-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.page-slide-enter-from,
.page-slide-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

@media (prefers-reduced-motion: reduce) {
  .page-slide-enter-active,
  .page-slide-leave-active {
    transition: none;
  }
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

