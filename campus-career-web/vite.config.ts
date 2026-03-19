import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * Vite 配置入口：
 * - plugins: Vue 编译插件
 * - resolve.alias: 路径别名（这里把 `@` 指向 `src/`）
 *
 * 文档：https://vite.dev/config/
 */
export default defineConfig({
  plugins: [
    vue(),
  ],
  build: {
    cssCodeSplit: true,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          if (id.includes('element-plus')) return 'vendor-element-plus'
          if (id.includes('markdown-it') || id.includes('dompurify')) return 'vendor-qa'
          if (id.includes('vue-router')) return 'vendor-router'
          if (id.includes('pinia')) return 'vendor-store'
          if (id.includes('/vue/') || id.includes('vue@')) return 'vendor-vue'
          return 'vendor'
        },
      },
    },
  },
  optimizeDeps: {
    include: ['axios', 'pinia', 'vue', 'vue-router'],
  },
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
    // Keep dev server on a fixed address so Nginx can always reach it.
    host: '0.0.0.0',
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
    allowedHosts: [
      '.ngrok-free.app',
      'localhost',
      '127.0.0.1',
    ],
  },
  resolve: {
    alias: {
      // 允许使用 `@/xxx` 引用 `src/xxx`
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
