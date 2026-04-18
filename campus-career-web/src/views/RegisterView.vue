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
    errorMsg.value = '先想一个好记的校园账号吧'
    return
  }
  if (!password.value) {
    errorMsg.value = '请输入登录密码'
    return
  }
  if (password.value !== passwordAgain.value) {
    errorMsg.value = '两次密码不一致，再确认一下'
    return
  }
  if (password.value.length < 6) {
    errorMsg.value = '密码至少 6 位，安全一些更好哦'
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
    else errorMsg.value = '注册没成功，请稍后再试'
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
    <div class="shell-orbit shell-orbit-a"></div>
    <div class="shell-orbit shell-orbit-b"></div>
    <div class="register-inner">
      <section class="register-brand">
        <div class="brand-hero">
          <div class="eyebrow">新同学欢迎加入</div>
          <div class="brand-block">
            <div class="logo">CC</div>
            <div class="brand-text">
              <div class="brand-title">Campus Career Platform</div>
              <div class="brand-sub">把校园里的经验、资料和交流，串成一个更有用的社区</div>
            </div>
          </div>
          <div class="welcome-title">30 秒，创建你的专属主页</div>
          <div class="welcome-desc">
            发布经验、沉淀资料、获取建议，你的每一步都会被看见。
          </div>
        </div>

        <div class="feature-grid">
          <div class="feature-card feature-blue">
            <span class="feature-icon">✨</span>
            <div>
              <div class="feature-title">免费使用全部核心功能</div>
              <div class="feature-sub">发帖、评论、收藏、资源阅读都能用</div>
            </div>
          </div>
          <div class="feature-card feature-green">
            <span class="feature-icon"></span>
            <div>
              <div class="feature-title">账号安全，内容可控</div>
              <div class="feature-sub">内容管理更安心，体验更顺滑</div>
            </div>
          </div>
        </div>

        <div class="brand-note">
          注册后就能马上发帖、看资源、问学涯助手，让社区陪你一起往前走。
        </div>
      </section>

      <div class="register-form-wrap">
        <el-card class="register-card auth-card" shadow="never">
          <div class="form-header">
            <div class="form-kicker">欢迎加入</div>
            <div class="form-title">创建账号</div>
            <div class="form-sub">填好信息后，就能立刻加入讨论和分享。</div>
          </div>

          <el-form class="register-form" @submit.prevent="onSubmit">
            <el-form-item>
              <el-input
                v-model="username"
                size="large"
                placeholder="学号 / 邮箱"
                autocomplete="username"
                clearable
              >
                <template #prefix>
                  <span class="input-icon"></span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="password"
                size="large"
                type="password"
                placeholder="密码（至少 6 位）"
                autocomplete="new-password"
                show-password
              >
                <template #prefix>
                  <span class="input-icon"></span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="passwordAgain"
                size="large"
                type="password"
                placeholder="再次输入密码"
                autocomplete="new-password"
                show-password
              >
                <template #prefix>
                  <span class="input-icon"></span>
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
                {{ loading ? '正在创建...' : '立即加入' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span class="footer-text">已经有账号了？</span>
            <el-button class="footer-link footer-link-button" @click="goLogin">
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
  padding: 28px 24px;
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(74, 111, 255, 0.16), transparent 34%),
    radial-gradient(circle at 12% 80%, rgba(34, 197, 94, 0.12), transparent 30%),
    var(--ccp-page-bg-soft);
}

.register-inner {
  width: 100%;
  max-width: 1120px;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(360px, 0.95fr);
  gap: 42px;
  align-items: center;
  position: relative;
  z-index: 1;
}

.register-brand {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 580px;
}

.brand-hero {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.eyebrow {
  align-self: flex-start;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(42, 92, 255, 0.1);
  color: var(--ccp-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  background: var(--ccp-primary-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 18px;
  box-shadow: 0 16px 36px rgba(42, 92, 255, 0.28);
}

.brand-text .brand-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--ccp-text);
}

.brand-text .brand-sub {
  font-size: 13px;
  line-height: 1.6;
  color: var(--ccp-text-muted);
}

.welcome-title {
  font-size: 36px;
  font-weight: 800;
  color: var(--ccp-text);
  line-height: 1.15;
  letter-spacing: -0.03em;
}

.welcome-desc {
  font-size: 16px;
  color: var(--ccp-text-secondary);
  line-height: 1.6;
  max-width: 520px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.feature-card {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 16px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(233, 238, 245, 0.92);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
  backdrop-filter: blur(12px);
}

.feature-icon {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  flex: none;
  font-size: 18px;
}

.feature-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--ccp-text);
  line-height: 1.35;
}

.feature-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--ccp-text-muted);
  line-height: 1.45;
}

.feature-blue .feature-icon {
  background: rgba(74, 111, 255, 0.12);
  color: var(--ccp-primary);
}

.feature-green .feature-icon {
  background: rgba(34, 197, 94, 0.12);
  color: var(--ccp-success);
}

.brand-note {
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px dashed rgba(74, 111, 255, 0.2);
  color: var(--ccp-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.register-form-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
}

.register-card {
  width: 100%;
  max-width: 430px;
  border-radius: 18px;
  padding: 34px 30px;
  box-shadow: 0 24px 60px rgba(16, 24, 40, 0.12);
  border: 1px solid rgba(233, 238, 245, 0.95);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px);
  animation: rise-in 520ms cubic-bezier(0.2, 0.8, 0.2, 1) both;
}

.register-card :deep(.el-card__body) {
  padding: 0;
}

.form-header {
  margin-bottom: 24px;
}

.form-kicker {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(42, 92, 255, 0.08);
  color: var(--ccp-primary);
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 10px;
}

.form-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--ccp-text);
  margin-bottom: 6px;
}

.form-sub {
  font-size: 14px;
  color: var(--ccp-text-muted);
  line-height: 1.6;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 14px;
  min-height: 48px;
  padding: 6px 14px;
  box-shadow: 0 0 0 1px rgba(226, 232, 240, 0.95);
  transition: box-shadow var(--ccp-normal), transform var(--ccp-normal), border-color var(--ccp-normal);
}

.register-form :deep(.el-input__wrapper:hover),
.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px rgba(74, 111, 255, 0.8),
    0 10px 24px rgba(74, 111, 255, 0.08);
  transform: translateY(-1px);
}

.register-form :deep(.el-input__prefix),
.register-form :deep(.el-input__suffix) {
  color: var(--ccp-text-light);
  transition: color var(--ccp-normal);
}

.register-form :deep(.el-input__wrapper.is-focus .el-input__prefix),
.register-form :deep(.el-input__wrapper.is-focus .el-input__suffix) {
  color: var(--ccp-primary);
}

.input-icon {
  font-size: 15px;
  margin-right: 4px;
}

.form-alert {
  margin-bottom: 14px;
  border-radius: 14px;
}

.form-actions {
  margin-bottom: 0;
  margin-top: 8px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  border: 0;
  background: var(--ccp-primary-gradient);
  box-shadow: 0 14px 26px rgba(42, 92, 255, 0.24);
  transition: transform var(--ccp-normal), box-shadow var(--ccp-normal), filter var(--ccp-normal);
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 32px rgba(42, 92, 255, 0.28);
  filter: brightness(1.03);
}

.submit-btn:active {
  transform: translateY(1px);
  box-shadow: 0 10px 18px rgba(42, 92, 255, 0.18);
}

.form-footer {
  padding-top: 18px;
  border-top: 1px solid rgba(233, 238, 245, 0.95);
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

.footer-link-button {
  margin-left: 2px;
  padding: 0 10px;
  height: 30px;
  border-radius: 999px;
  border: 1px solid rgba(74, 111, 255, 0.16);
  background: rgba(74, 111, 255, 0.06);
  color: var(--ccp-primary);
  transition: background var(--ccp-normal), transform var(--ccp-normal), box-shadow var(--ccp-normal);
}

.footer-link-button:hover {
  background: rgba(74, 111, 255, 0.12);
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(74, 111, 255, 0.12);
}

.shell-orbit {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(8px);
  animation: float 9s ease-in-out infinite;
}

.shell-orbit-a {
  width: 300px;
  height: 300px;
  left: -70px;
  top: 86px;
  background: radial-gradient(circle, rgba(74, 111, 255, 0.14), transparent 68%);
}

.shell-orbit-b {
  width: 220px;
  height: 220px;
  right: 14%;
  bottom: 48px;
  background: radial-gradient(circle, rgba(34, 197, 94, 0.12), transparent 68%);
  animation-delay: -3s;
}

@keyframes rise-in {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.985);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes float {
  0%,
  100% {
    transform: translate3d(0, 0, 0);
  }

  50% {
    transform: translate3d(0, -10px, 0);
  }
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

  .eyebrow {
    align-self: center;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .feature-card {
    width: 100%;
  }

  .welcome-desc {
    text-align: center;
    max-width: 100%;
  }

  .brand-note {
    text-align: left;
  }
}
</style>
