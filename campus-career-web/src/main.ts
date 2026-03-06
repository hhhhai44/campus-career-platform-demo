/**
 * 应用入口文件：
 * - 创建 Vue 应用实例
 * - 注册全局插件（Pinia 状态管理、Vue Router 路由）
 * - 将应用挂载到 `index.html` 中的 #app 容器
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/assets/styles/variables.css'

import App from './App.vue'
import router from './router'
import { configureHttp } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 使用 Pinia 中的 token 配置全局 HTTP 鉴权
configureHttp({
  getToken: () => {
    const auth = useAuthStore()
    return auth.token
  },
})

app.mount('#app')
