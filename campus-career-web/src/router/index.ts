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
import MainLayout from '@/layout/MainLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
      },
      {
        path: 'forum',
        name: 'forum-list',
        component: () => import('@/views/forum/PostList.vue'),
      },
      {
        path: 'forum/:id',
        name: 'forum-detail',
        component: () => import('@/views/forum/PostDetail.vue'),
      },
      {
        path: 'resource',
        name: 'resource-list',
        component: () => import('@/views/resource/ResourceList.vue'),
      },
      {
        path: 'resource/:id',
        name: 'resource-detail',
        component: () => import('@/views/resource/ResourceDetail.vue'),
      },
      {
        path: 'upload',
        name: 'upload',
        component: () => import('@/views/UploadCenter.vue'),
      },
      {
        path: 'notification',
        name: 'notification',
        component: () => import('@/views/NotificationView.vue'),
      },
      {
        path: 'me',
        name: 'me',
        component: () => import('@/views/MeCenter.vue'),
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { requiresAuth: false },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta?.requiresAuth && !auth.isAuthed) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if ((to.name === 'login' || to.name === 'register') && auth.isAuthed) {
    return { name: 'home' }
  }
  return true
})

export default router
