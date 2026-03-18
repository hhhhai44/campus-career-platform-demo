<script setup lang="ts">
/**
 * 根组件（应用入口 UI）：
 * - 通常用于放全局布局（Header/Sidebar/Footer）、全局弹窗、全局样式等
 * - 如果启用路由，常见做法是在这里放 `<router-view />` 作为页面容器
 *
 * 当前模板仅展示静态内容，便于快速确认项目已跑通。
 */
</script>

<template>
  <router-view v-slot="{ Component, route }">
    <transition name="route-fade" mode="out-in">
      <keep-alive v-if="route.meta?.keepAlive">
        <component
          :is="Component"
          :key="String(route.name ?? route.path)"
        />
      </keep-alive>
      <component
        :is="Component"
        v-else
        :key="route.fullPath"
      />
    </transition>
  </router-view>
</template>

<style>
/* 全站默认排版，与 variables.css 设计变量一致 */
body {
  margin: 0;
  font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial, "PingFang SC",
    "Microsoft YaHei", sans-serif;
  background: var(--ccp-page-bg);
  color: var(--ccp-text);
  -webkit-font-smoothing: antialiased;
}

html {
  scroll-behavior: smooth;
}

* {
  box-sizing: border-box;
}

button,
a,
input,
textarea,
select {
  touch-action: manipulation;
}

button:focus-visible,
a:focus-visible,
input:focus-visible,
textarea:focus-visible,
select:focus-visible {
  outline: 2px solid var(--ccp-primary);
  outline-offset: 2px;
}

.route-fade-enter-active,
.route-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.route-fade-enter-from,
.route-fade-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

@media (prefers-reduced-motion: reduce) {
  html {
    scroll-behavior: auto;
  }

  .route-fade-enter-active,
  .route-fade-leave-active {
    transition: none;
  }
}
</style>
