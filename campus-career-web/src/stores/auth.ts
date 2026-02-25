import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/auth'

/**
 * 认证 Store（最简版）：
 * - 保存登录态（token + 过期时间）
 * - localStorage 持久化，刷新页面不丢登录态
 */
const LS_TOKEN = 'ccp_token'
const LS_EXPIRES_AT = 'ccp_expires_at'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(LS_TOKEN))
  const expiresAt = ref<number | null>(
    localStorage.getItem(LS_EXPIRES_AT) ? Number(localStorage.getItem(LS_EXPIRES_AT)) : null,
  )

  const isAuthed = computed(() => {
    if (!token.value) return false
    if (!expiresAt.value) return true
    return Date.now() < expiresAt.value
  })

  function setSession(newToken: string, expiresInSeconds?: number) {
    token.value = newToken
    localStorage.setItem(LS_TOKEN, newToken)

    if (typeof expiresInSeconds === 'number' && Number.isFinite(expiresInSeconds)) {
      const at = Date.now() + Math.max(0, expiresInSeconds) * 1000
      expiresAt.value = at
      localStorage.setItem(LS_EXPIRES_AT, String(at))
    } else {
      expiresAt.value = null
      localStorage.removeItem(LS_EXPIRES_AT)
    }
  }

  function logout() {
    token.value = null
    expiresAt.value = null
    localStorage.removeItem(LS_TOKEN)
    localStorage.removeItem(LS_EXPIRES_AT)
  }

  async function login(username: string, password: string) {
    const resp = await authApi.login({ username, password })
    setSession(resp.token, resp.expiresIn)
    return resp
  }

  return { token, expiresAt, isAuthed, login, logout, setSession }
})


