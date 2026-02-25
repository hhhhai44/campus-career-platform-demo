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
  <!-- 根容器：路由页面渲染出口 -->
  <router-view />
</template>

<style scoped>
/* 给全站一个舒服的默认排版 */
:global(body) {
  margin: 0;
  font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial, "PingFang SC",
    "Microsoft YaHei", sans-serif;
  background: #f6f7fb;
  color: #111827;
}
</style>
