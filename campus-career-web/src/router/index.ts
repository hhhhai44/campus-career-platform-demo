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
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  // 路由表：当前模板为空。后续按业务添加，例如：
  // { path: '/', name: 'home', component: () => import('@/views/HomeView.vue') }
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false },
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta?.requiresAuth && !auth.isAuthed) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && auth.isAuthed) {
    return { name: 'home' }
  }
  return true
})

export default router
