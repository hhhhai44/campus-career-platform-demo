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
  if (loading.value) return
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

function goRegister() {
  router.push({ name: 'register', query: route.query })
}
</script>

<template>
  <div class="login-shell">
    <div class="login-inner">
      <!-- 左侧品牌与文案 -->
      <div class="login-brand">
        <div class="brand-block">
          <div class="logo">CC</div>
          <div class="brand-text">
            <div class="brand-title">校园职涯社区</div>
            <div class="brand-sub">Campus Career Platform</div>
          </div>
        </div>
        <div class="welcome-title">欢迎回来</div>
        <div class="welcome-desc">
          登录后即可发布帖子、上传资源，与同校同学分享实习经验与学习资料。
        </div>
        <div class="deco-list">
          <div class="deco-item">
            <span class="deco-icon">📝</span>
            <span>发布经验与心得</span>
          </div>
          <div class="deco-item">
            <span class="deco-icon">📚</span>
            <span>浏览与下载资源</span>
          </div>
          <div class="deco-item">
            <span class="deco-icon">💬</span>
            <span>参与评论与互动</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录卡片 -->
      <div class="login-form-wrap">
        <el-card class="login-card" shadow="hover">
          <div class="form-header">
            <div class="form-title">账号登录</div>
            <div class="form-sub">使用用户名与密码登录</div>
          </div>

          <el-form class="login-form" @submit.prevent="onSubmit">
            <el-form-item>
              <el-input
                v-model="username"
                size="large"
                placeholder="请输入用户名"
                autocomplete="username"
                clearable
              >
                <template #prefix>
                  <span class="input-icon">👤</span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="password"
                size="large"
                type="password"
                placeholder="请输入密码"
                autocomplete="current-password"
                show-password
              >
                <template #prefix>
                  <span class="input-icon">🔒</span>
                </template>
              </el-input>
            </el-form-item>

            <el-alert
              v-if="errorMsg"
              :title="errorMsg"
              type="error"
              show-icon
              class="form-alert"
              :closable="false"
            />

            <el-form-item class="form-actions">
              <el-button
                type="primary"
                size="large"
                round
                class="submit-btn"
                :loading="loading"
                native-type="submit"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span class="footer-text">还没有账号？</span>
            <el-button link type="primary" class="footer-link" @click="goRegister">
              立即注册
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 20px;
  background: var(--ccp-page-bg);
}

.login-inner {
  width: 100%;
  max-width: 920px;
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 48px;
  align-items: center;
}

/* 左侧品牌区 */
.login-brand {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: var(--ccp-brand-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 18px;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.35);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--ccp-text);
  letter-spacing: 0.02em;
}

.brand-sub {
  font-size: 12px;
  color: var(--ccp-text-muted);
}

.welcome-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--ccp-text);
  line-height: 1.3;
  letter-spacing: 0.02em;
}

.welcome-desc {
  font-size: 15px;
  color: var(--ccp-text-secondary);
  line-height: 1.6;
  max-width: 320px;
}

.deco-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.deco-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--ccp-text-secondary);
}

.deco-icon {
  font-size: 18px;
  width: 32px;
  text-align: center;
}

/* 右侧表单卡片 */
.login-form-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border-radius: var(--ccp-card-radius);
  padding: 32px 28px;
  box-shadow: var(--ccp-card-shadow);
  border: 1px solid var(--ccp-card-border);
}

.login-card :deep(.el-card__body) {
  padding: 0;
}

.form-header {
  margin-bottom: 28px;
}

.form-title {
  font-size: 20px;
  font-weight: 800;
  color: var(--ccp-text);
  margin-bottom: 4px;
}

.form-sub {
  font-size: 13px;
  color: var(--ccp-text-muted);
}

.login-form {
  margin-bottom: 16px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 14px;
  box-shadow: 0 0 0 1px var(--el-border-color);
}

.login-form :deep(.el-input__wrapper:hover),
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--ccp-primary);
}

.input-icon {
  font-size: 16px;
  margin-right: 4px;
  opacity: 0.8;
}

.form-alert {
  margin-bottom: 16px;
  border-radius: 12px;
}

.form-actions {
  margin-bottom: 0;
  margin-top: 8px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
}

.form-footer {
  padding-top: 16px;
  border-top: 1px solid var(--ccp-card-border);
  text-align: center;
}

.footer-text {
  font-size: 13px;
  color: var(--ccp-text-muted);
  margin-right: 4px;
}

.footer-link {
  font-size: 13px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .login-inner {
    grid-template-columns: 1fr;
    gap: 32px;
    max-width: 420px;
    margin: 0 auto;
  }

  .login-brand {
    text-align: center;
    align-items: center;
  }

  .welcome-desc {
    text-align: center;
  }

  .deco-list {
    align-items: center;
  }
}
</style>
