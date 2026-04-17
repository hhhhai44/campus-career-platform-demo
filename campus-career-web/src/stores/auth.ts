import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/auth'

const LS_TOKEN = 'ccp_token'
const LS_USER_ID = 'ccp_user_id'
const LS_EXPIRES_AT = 'ccp_expires_at'
const LS_USERNAME = 'ccp_username'
const LS_ROLE = 'ccp_role'

type JwtPayload = {
  sub?: string
  username?: string
  role?: number | string
}

function decodeBase64Url(input: string) {
  const normalized = input.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
  return atob(padded)
}

function parseTokenPayload(token: string): JwtPayload | null {
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    return JSON.parse(decodeBase64Url(payload)) as JwtPayload
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(LS_TOKEN))
  const expiresAt = ref<number | null>(
    localStorage.getItem(LS_EXPIRES_AT) ? Number(localStorage.getItem(LS_EXPIRES_AT)) : null,
  )
  const username = ref<string | null>(localStorage.getItem(LS_USERNAME))
  const userId = ref<number | null>(
    localStorage.getItem(LS_USER_ID) ? Number(localStorage.getItem(LS_USER_ID)) : null,
  )
  const role = ref<number | null>(
    localStorage.getItem(LS_ROLE) ? Number(localStorage.getItem(LS_ROLE)) : null,
  )

  function setUsername(name: string | null) {
    const normalized = name?.trim() || null
    username.value = normalized
    if (normalized) {
      localStorage.setItem(LS_USERNAME, normalized)
    } else {
      localStorage.removeItem(LS_USERNAME)
    }
  }

  function syncSessionFromToken(newToken: string | null) {
    if (!newToken) {
      setUsername(null)
      userId.value = null
      role.value = null
      localStorage.removeItem(LS_USER_ID)
      localStorage.removeItem(LS_ROLE)
      return
    }

    const payload = parseTokenPayload(newToken)
    if (payload?.username) {
      setUsername(payload.username)
    }

    if (payload?.sub) {
      const normalizedUserId = Number(payload.sub)
      userId.value = Number.isFinite(normalizedUserId) ? normalizedUserId : null
      if (userId.value === null) {
        localStorage.removeItem(LS_USER_ID)
      } else {
        localStorage.setItem(LS_USER_ID, String(userId.value))
      }
    } else {
      userId.value = null
      localStorage.removeItem(LS_USER_ID)
    }

    if (payload?.role !== undefined && payload?.role !== null) {
      const normalizedRole = Number(payload.role)
      role.value = Number.isFinite(normalizedRole) ? normalizedRole : null
      if (role.value === null) {
        localStorage.removeItem(LS_ROLE)
      } else {
        localStorage.setItem(LS_ROLE, String(role.value))
      }
    } else {
      role.value = null
      localStorage.removeItem(LS_ROLE)
    }
  }

  const isAuthed = computed(() => {
    if (!token.value) return false
    if (!expiresAt.value) return true
    return Date.now() < expiresAt.value
  })

  const isAdmin = computed(() => role.value === 2)

  function setSession(newToken: string, expiresInSeconds?: number) {
    token.value = newToken
    localStorage.setItem(LS_TOKEN, newToken)
    syncSessionFromToken(newToken)

    if (typeof expiresInSeconds === 'number' && Number.isFinite(expiresInSeconds)) {
      expiresAt.value = Date.now() + expiresInSeconds * 1000
      localStorage.setItem(LS_EXPIRES_AT, String(expiresAt.value))
    } else {
      expiresAt.value = null
      localStorage.removeItem(LS_EXPIRES_AT)
    }
  }

  function logout() {
    token.value = null
    expiresAt.value = null
    username.value = null
    userId.value = null
    role.value = null
    localStorage.removeItem(LS_TOKEN)
    localStorage.removeItem(LS_EXPIRES_AT)
    localStorage.removeItem(LS_USERNAME)
    localStorage.removeItem(LS_USER_ID)
    localStorage.removeItem(LS_ROLE)
  }

  async function login(inputUsername: string, password: string) {
    const normalizedUsername = inputUsername.trim()
    const resp = await authApi.login({ username: normalizedUsername, password })
    setSession(resp.token, resp.expiresIn)
    if (!username.value) {
      setUsername(normalizedUsername)
    }
    return resp
  }

  if (token.value) {
    syncSessionFromToken(token.value)
  }

  return {
    token,
    expiresAt,
    username,
    userId,
    role,
    isAuthed,
    isAdmin,
    login,
    logout,
    setSession,
    setUsername,
  }
})
