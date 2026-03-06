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
const LS_USERNAME = 'ccp_username'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(LS_TOKEN))
  const expiresAt = ref<number | null>(
    localStorage.getItem(LS_EXPIRES_AT) ? Number(localStorage.getItem(LS_EXPIRES_AT)) : null,
  )
  const username = ref<string | null>(localStorage.getItem(LS_USERNAME))

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

  function setUsername(name: string | null) {
    const normalized = name?.trim() || null
    username.value = normalized
    if (normalized) {
      localStorage.setItem(LS_USERNAME, normalized)
    } else {
      localStorage.removeItem(LS_USERNAME)
    }
  }

  function logout() {
    token.value = null
    expiresAt.value = null
    username.value = null
    localStorage.removeItem(LS_TOKEN)
    localStorage.removeItem(LS_EXPIRES_AT)
    localStorage.removeItem(LS_USERNAME)
  }

  async function login(inputUsername: string, password: string) {
    const normalizedUsername = inputUsername.trim()
    const resp = await authApi.login({ username: normalizedUsername, password })
    setSession(resp.token, resp.expiresIn)
    setUsername(normalizedUsername)
    return resp
  }

  return { token, expiresAt, username, isAuthed, login, logout, setSession, setUsername }
})
