/**
 * 应用入口文件：
 * - 创建 Vue 应用实例
 * - 注册全局插件（Pinia 状态管理、Vue Router 路由）
 * - 将应用挂载到 `index.html` 中的 #app 容器
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 全局状态管理（Pinia）。如果你不使用 Pinia，可删除这行并移除依赖。
app.use(createPinia())
// 路由（Vue Router）。如果你不使用路由，可删除这行并移除 `src/router/` 与依赖。
app.use(router)

// 挂载到 DOM：对应 `index.html` 里的 <div id="app"></div>
app.mount('#app')
