/**
 * 路由配置入口：
 * - 负责创建 router 实例并导出给 `src/main.ts` 注册
 * - 你可以在 routes 中添加页面路由（推荐后续拆分为 `src/router/routes.ts` 维护）
 *
 * 可删除条件：
 * - 如果项目确定不需要路由（单页单视图），可移除此文件与 `vue-router` 依赖；
 *   同时在 `src/main.ts` 中删除 `app.use(router)`。
 */
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

type ViewLoader = () => Promise<unknown>

const loadMainLayout: ViewLoader = () => import('@/layout/MainLayout.vue')
const loadHomeView: ViewLoader = () => import('@/views/HomeView.vue')
const loadPostListView: ViewLoader = () => import('@/views/forum/PostList.vue')
const loadPostDetailView: ViewLoader = () => import('@/views/forum/PostDetail.vue')
const loadResourceListView: ViewLoader = () => import('@/views/resource/ResourceList.vue')
const loadResourceDetailView: ViewLoader = () => import('@/views/resource/ResourceDetail.vue')
const loadQwenChatView: ViewLoader = () => import('@/views/QwenChat.vue')
const loadUploadView: ViewLoader = () => import('@/views/UploadCenter.vue')
const loadNotificationView: ViewLoader = () => import('@/views/NotificationView.vue')
const loadMessageCenterView: ViewLoader = () => import('@/views/message/MessageCenter.vue')
const loadMeCenterView: ViewLoader = () => import('@/views/MeCenter.vue')
const loadLoginView: ViewLoader = () => import('@/views/LoginView.vue')
const loadRegisterView: ViewLoader = () => import('@/views/RegisterView.vue')
const loadAdminPostModerationView: ViewLoader = () => import('@/views/admin/PostModerationView.vue')
const loadAdminReportManagementView: ViewLoader = () => import('@/views/admin/ReportManagementView.vue')
const loadAdminUserManagementView: ViewLoader = () => import('@/views/admin/UserManagementView.vue')

const loadersByRouteName: Record<string, ViewLoader> = {
  home: loadHomeView,
  'forum-list': loadPostListView,
  'forum-detail': loadPostDetailView,
  'resource-list': loadResourceListView,
  'resource-detail': loadResourceDetailView,
  'smart-qa': loadQwenChatView,
  upload: loadUploadView,
  notification: loadNotificationView,
  'message-center': loadMessageCenterView,
  me: loadMeCenterView,
  'admin-post-review': loadAdminPostModerationView,
  'admin-report-review': loadAdminReportManagementView,
  'admin-user-review': loadAdminUserManagementView,
  login: loadLoginView,
  register: loadRegisterView,
}

const prefetchPlan: Record<string, string[]> = {
  home: ['forum-list', 'resource-list', 'smart-qa'],
  'forum-list': ['forum-detail', 'resource-list'],
  'resource-list': ['resource-detail', 'forum-list'],
  'message-center': ['forum-list', 'me'],
  'smart-qa': ['resource-list', 'forum-list'],
  'admin-report-review': ['admin-post-review', 'admin-user-review'],
  'admin-user-review': ['admin-post-review', 'admin-report-review'],
}

const prefetchedRoutes = new Set<string>()

function runWhenIdle(task: () => void) {
  if (typeof window === 'undefined') return
  const w = window as Window & {
    requestIdleCallback?: (callback: () => void, options?: { timeout: number }) => number
  }
  if (typeof w.requestIdleCallback === 'function') {
    w.requestIdleCallback(() => task(), { timeout: 1000 })
    return
  }
  window.setTimeout(task, 180)
}

function prefetchRoutes(routeNames: string[]) {
  for (const routeName of routeNames) {
    if (prefetchedRoutes.has(routeName)) continue
    const loader = loadersByRouteName[routeName]
    if (!loader) continue

    prefetchedRoutes.add(routeName)
    runWhenIdle(() => {
      void loader().catch(() => {
        prefetchedRoutes.delete(routeName)
      })
    })
  }
}

function supportsSmoothScroll() {
  if (typeof document === 'undefined') return false
  return 'scrollBehavior' in document.documentElement.style
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: loadMainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'home',
        component: loadHomeView,
        meta: { keepAlive: true },
      },
      {
        path: 'forum',
        name: 'forum-list',
        component: loadPostListView,
        meta: { keepAlive: true },
      },
      {
        path: 'forum/:id',
        name: 'forum-detail',
        component: loadPostDetailView,
      },
      {
        path: 'resource',
        name: 'resource-list',
        component: loadResourceListView,
        meta: { keepAlive: true },
      },
      {
        path: 'resource/:id',
        name: 'resource-detail',
        component: loadResourceDetailView,
      },
      {
        path: 'qa',
        name: 'smart-qa',
        component: loadQwenChatView,
        meta: { keepAlive: true },
      },
      {
        path: 'qwen',
        name: 'qwen-chat',
        redirect: { name: 'smart-qa' },
      },
      {
        path: 'upload',
        name: 'upload',
        component: loadUploadView,
      },
      {
        path: 'notification',
        name: 'notification',
        component: loadNotificationView,
      },
      {
        path: 'message',
        name: 'message-center',
        component: loadMessageCenterView,
      },
      {
        path: 'me',
        name: 'me',
        component: loadMeCenterView,
      },
      {
        path: 'admin/posts',
        name: 'admin-post-review',
        component: loadAdminPostModerationView,
        meta: { requiresAdmin: true },
      },
      {
        path: 'admin/reports',
        name: 'admin-report-review',
        component: loadAdminReportManagementView,
        meta: { requiresAdmin: true },
      },
      {
        path: 'admin/users',
        name: 'admin-user-review',
        component: loadAdminUserManagementView,
        meta: { requiresAdmin: true },
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: loadLoginView,
    meta: { requiresAuth: false },
  },
  {
    path: '/register',
    name: 'register',
    component: loadRegisterView,
    meta: { requiresAuth: false },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    return {
      left: 0,
      top: 0,
      behavior: supportsSmoothScroll() ? 'smooth' : 'auto',
    }
  },
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta?.requiresAuth && !auth.isAuthed) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta?.requiresAdmin && !auth.isAdmin) {
    return { name: 'home' }
  }

  return true
})

router.afterEach((to) => {
  const currentName = typeof to.name === 'string' ? to.name : null
  if (!currentName) return
  const plan = prefetchPlan[currentName]
  if (!plan?.length) return
  prefetchRoutes(plan)
})

export default router
