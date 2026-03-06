<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { ApiError } from '@/api/http'

const router = useRouter()

const username = ref('')
const password = ref('')
const passwordAgain = ref('')
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
  if (password.value !== passwordAgain.value) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  if (password.value.length < 6) {
    errorMsg.value = '密码长度至少 6 位'
    return
  }

  loading.value = true
  try {
    await authApi.register({
      username: username.value.trim(),
      password: password.value,
      confirmPassword: passwordAgain.value,
    })
    router.replace({ name: 'login', query: { from: 'register' } })
  } catch (e) {
    if (e instanceof ApiError) errorMsg.value = e.message
    else errorMsg.value = '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="register-shell">
    <div class="register-inner">
      <!-- 左侧品牌与文案 -->
      <div class="register-brand">
        <div class="brand-block">
          <div class="logo">CC</div>
          <div class="brand-text">
            <div class="brand-title">校园职涯社区</div>
            <div class="brand-sub">Campus Career Platform</div>
          </div>
        </div>
        <div class="welcome-title">加入我们</div>
        <div class="welcome-desc">
          注册账号后即可发布帖子、上传资源，与同校同学一起分享成长与求职经验。
        </div>
        <div class="deco-list">
          <div class="deco-item">
            <span class="deco-icon">✨</span>
            <span>免费使用全部功能</span>
          </div>
          <div class="deco-item">
            <span class="deco-icon">🔐</span>
            <span>账号安全，数据私密</span>
          </div>
        </div>
      </div>

      <!-- 右侧注册卡片 -->
      <div class="register-form-wrap">
        <el-card class="register-card" shadow="hover">
          <div class="form-header">
            <div class="form-title">创建账号</div>
            <div class="form-sub">设置用户名与密码</div>
          </div>

          <el-form class="register-form" @submit.prevent="onSubmit">
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
                placeholder="请输入密码（至少 6 位）"
                autocomplete="new-password"
                show-password
              >
                <template #prefix>
                  <span class="input-icon">🔒</span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="passwordAgain"
                size="large"
                type="password"
                placeholder="请再次输入密码"
                autocomplete="new-password"
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
                {{ loading ? '注册中...' : '注 册' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span class="footer-text">已有账号？</span>
            <el-button link type="primary" class="footer-link" @click="goLogin">
              去登录
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 20px;
  background: var(--ccp-page-bg);
}

.register-inner {
  width: 100%;
  max-width: 920px;
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 48px;
  align-items: center;
}

.register-brand {
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

.brand-text .brand-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--ccp-text);
  letter-spacing: 0.02em;
}

.brand-text .brand-sub {
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

.register-form-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
}

.register-card {
  width: 100%;
  max-width: 400px;
  border-radius: var(--ccp-card-radius);
  padding: 32px 28px;
  box-shadow: var(--ccp-card-shadow);
  border: 1px solid var(--ccp-card-border);
}

.register-card :deep(.el-card__body) {
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

.register-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 14px;
  box-shadow: 0 0 0 1px var(--el-border-color);
}

.register-form :deep(.el-input__wrapper:hover),
.register-form :deep(.el-input__wrapper.is-focus) {
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
  .register-inner {
    grid-template-columns: 1fr;
    gap: 32px;
    max-width: 420px;
    margin: 0 auto;
  }

  .register-brand {
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
