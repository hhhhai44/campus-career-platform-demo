<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ApiError } from '@/api/http'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref<string | null>(null)

async function onSubmit() {
  errorMsg.value = null
  if (!username.value.trim()) {
    errorMsg.value = '请输入用户名'
    return
  }
  if (!password.value) {
    errorMsg.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    await auth.login(username.value.trim(), password.value)
    const redirect = (route.query.redirect as string | undefined) ?? '/'
    await router.replace(redirect)
  } catch (e) {
    if (e instanceof ApiError) errorMsg.value = e.message
    else errorMsg.value = '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="card">
      <div class="title">校园职业平台 - 登录</div>
      <div class="sub">请输入用户名与密码</div>

      <form class="form" @submit.prevent="onSubmit">
        <label class="field">
          <div class="label">用户名</div>
          <input v-model="username" class="input" autocomplete="username" placeholder="请输入用户名" />
        </label>

        <label class="field">
          <div class="label">密码</div>
          <input
            v-model="password"
            class="input"
            type="password"
            autocomplete="current-password"
            placeholder="请输入密码"
          />
        </label>

        <div v-if="errorMsg" class="error">{{ errorMsg }}</div>

        <button class="btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
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
  width: min(420px, 92vw);
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 22px 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
}

.title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 6px;
}

.sub {
  color: #6b7280;
  font-size: 13px;
  margin-bottom: 16px;
}

.form {
  display: grid;
  gap: 12px;
}

.field {
  display: grid;
  gap: 6px;
}

.label {
  font-size: 12px;
  color: #374151;
}

.input {
  height: 40px;
  padding: 0 12px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  outline: none;
}

.input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.15);
}

.error {
  padding: 10px 12px;
  border-radius: 10px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #991b1b;
  font-size: 13px;
}

.btn {
  height: 42px;
  border-radius: 10px;
  border: 0;
  background: #4f46e5;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>


