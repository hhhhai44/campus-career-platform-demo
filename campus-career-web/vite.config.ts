import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

/**
 * Vite 配置入口：
 * - plugins: Vue 编译插件、开发调试增强插件等
 * - resolve.alias: 路径别名（这里把 `@` 指向 `src/`）
 *
 * 可选项说明：
 * - `vite-plugin-vue-devtools` 仅提升开发体验，不影响生产功能；
 *   如不需要，可从 `plugins` 中移除 `vueDevTools()` 并卸载依赖。
 *
 * 文档：https://vite.dev/config/
 */
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  /**
   * 本地开发代理：
   * - 前端请求 `/api/**` 会被代理到后端（默认 8081）
   * - 这样可以避免浏览器跨域问题
   *
   * 说明：
   * - 前端请求写成 `POST /api/auth/login`
   * - 实际转发为 `POST http://localhost:8081/auth/login`
   */
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
  resolve: {
    alias: {
      // 允许使用 `@/xxx` 引用 `src/xxx`
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
