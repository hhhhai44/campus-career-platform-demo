<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function onLogout() {
  auth.logout()
  router.replace({ name: 'login' })
}
</script>

<template>
  <div class="page">
    <div class="card">
      <div class="title">已登录</div>
      <div class="kv">
        <div class="k">token</div>
        <div class="v mono">{{ auth.token }}</div>
      </div>
      <div class="kv">
        <div class="k">expiresAt</div>
        <div class="v mono">{{ auth.expiresAt }}</div>
      </div>
      <button class="btn" @click="onLogout">退出登录</button>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}

.card {
  width: min(720px, 92vw);
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 22px 20px;
}

.title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 14px;
}

.kv {
  display: grid;
  grid-template-columns: 90px 1fr;
  gap: 10px;
  padding: 10px 0;
  border-top: 1px dashed #e5e7eb;
}

.k {
  color: #6b7280;
  font-size: 13px;
}

.v {
  font-size: 13px;
  word-break: break-all;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
}

.btn {
  margin-top: 14px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #111827;
  color: white;
  cursor: pointer;
  padding: 0 14px;
}
</style>


