import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import axios from 'axios'

export type ApiResult<T> = {
  code: number
  message?: string
  data?: T
}

export class ApiError extends Error {
  code?: number
  constructor(message: string, code?: number) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

type HttpConfig = {
  baseUrl?: string
  getToken?: () => string | null
}

export type HttpRequestConfig = AxiosRequestConfig & {
  cacheTtlMs?: number
  cacheKey?: string
  forceRefresh?: boolean
  dedupe?: boolean
}

type CachedPayload = {
  expiresAt: number
  data: unknown
}

const defaultConfig: Required<HttpConfig> = {
  baseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api',
  getToken: () => null,
}

let axiosInstance: AxiosInstance | null = null

const getResponseCache = new Map<string, CachedPayload>()
const inflightGetRequests = new Map<string, Promise<unknown>>()

function toStableString(value: unknown) {
  try {
    return JSON.stringify(value ?? null)
  } catch {
    return String(value)
  }
}

function buildGetRequestKey(path: string, config?: HttpRequestConfig) {
  if (config?.cacheKey) return config.cacheKey
  return `${path}::${toStableString(config?.params)}`
}

function stripHttpEnhancers(config?: HttpRequestConfig): AxiosRequestConfig | undefined {
  if (!config) return undefined
  const { cacheTtlMs, cacheKey, forceRefresh, dedupe, ...axiosConfig } = config
  return axiosConfig
}

function cleanupExpiredCacheEntries() {
  const now = Date.now()
  for (const [key, cacheItem] of getResponseCache.entries()) {
    if (cacheItem.expiresAt <= now) {
      getResponseCache.delete(key)
    }
  }
}

export function clearHttpGetCache() {
  getResponseCache.clear()
}

function getAxios(): AxiosInstance {
  if (axiosInstance) return axiosInstance

  const instance = axios.create({
    baseURL: defaultConfig.baseUrl.replace(/\/+$/, ''),
    timeout: 15000,
    withCredentials: false,
  })

  instance.interceptors.request.use(async (config) => {
    const token = defaultConfig.getToken()
    if (token) {
      config.headers = config.headers ?? {}
      config.headers.Authorization = `Bearer ${token}`
    }

    if (config.data !== undefined) {
      config.data = await encryptPasswordFields(config.data)
    }
    if (config.params !== undefined) {
      config.params = await encryptPasswordFields(config.params)
    }

    return config
  })

  instance.interceptors.response.use(
    (response: AxiosResponse<ApiResult<unknown>>) => {
      const { data } = response
      if (!data) throw new ApiError('响应格式错误')
      if (data.code !== 1) {
        // 优先使用后端返回的 message，只有网络错误或服务器异常时才显示"请求失败"
        const errorMessage = data.message || '请求失败'
        throw new ApiError(errorMessage, data.code)
      }
      return response
    },
    (error) => {
      // 网络错误或服务器异常
      if (error.response) {
        const { status, statusText, data } = error.response
        // 尝试从响应中获取错误信息
        const errorMessage = data?.message || `网络错误：${status} ${statusText}`
        throw new ApiError(errorMessage, status)
      }
      throw new ApiError(error.message || '网络异常，请稍后重试')
    },
  )

  axiosInstance = instance
  return instance
}

export function configureHttp(config: HttpConfig) {
  if (config.baseUrl) {
    defaultConfig.baseUrl = config.baseUrl
  }
  if (config.getToken) {
    defaultConfig.getToken = config.getToken
  }
}

const PASSWORD_KEY_RE = /password/i

async function sha256Hex(text: string): Promise<string> {
  if (!globalThis.crypto?.subtle) {
    throw new ApiError('当前环境不支持密码加密，请升级浏览器')
  }
  const encoded = new TextEncoder().encode(text)
  const digest = await globalThis.crypto.subtle.digest('SHA-256', encoded)
  return Array.from(new Uint8Array(digest), (b) => b.toString(16).padStart(2, '0')).join('')
}

async function encryptPasswordFields<T>(input: T): Promise<T> {
  if (input == null) return input

  if (Array.isArray(input)) {
    const items = await Promise.all(input.map((item) => encryptPasswordFields(item)))
    return items as T
  }

  if (typeof input !== 'object') return input

  if (input instanceof FormData) {
    for (const [key, value] of input.entries()) {
      if (PASSWORD_KEY_RE.test(key) && typeof value === 'string' && value.length > 0) {
        input.set(key, await sha256Hex(value))
      }
    }
    return input
  }

  if (input instanceof URLSearchParams) {
    for (const [key, value] of input.entries()) {
      if (PASSWORD_KEY_RE.test(key) && value.length > 0) {
        input.set(key, await sha256Hex(value))
      }
    }
    return input
  }

  const source = input as Record<string, unknown>
  const target: Record<string, unknown> = {}

  for (const [key, value] of Object.entries(source)) {
    if (PASSWORD_KEY_RE.test(key) && typeof value === 'string' && value.length > 0) {
      target[key] = await sha256Hex(value)
      continue
    }
    target[key] = await encryptPasswordFields(value)
  }

  return target as T
}

export async function getJson<T>(path: string, config?: HttpRequestConfig): Promise<T> {
  const requestKey = buildGetRequestKey(path, config)
  const cacheTtlMs = Math.max(0, config?.cacheTtlMs ?? 0)
  const shouldDedupe = config?.dedupe !== false

  if (config?.forceRefresh) {
    getResponseCache.delete(requestKey)
  }

  if (cacheTtlMs > 0) {
    cleanupExpiredCacheEntries()
    const cached = getResponseCache.get(requestKey)
    if (cached && cached.expiresAt > Date.now()) {
      return cached.data as T
    }
  }

  if (shouldDedupe) {
    const inflight = inflightGetRequests.get(requestKey)
    if (inflight) {
      return inflight as Promise<T>
    }
  }

  const axiosConfig = stripHttpEnhancers(config)
  const request = (async () => {
    const res = await getAxios().get<ApiResult<T>>(path, axiosConfig)
    const data = res.data.data as T
    if (cacheTtlMs > 0) {
      getResponseCache.set(requestKey, {
        data,
        expiresAt: Date.now() + cacheTtlMs,
      })
    }
    return data
  })()

  if (shouldDedupe) {
    inflightGetRequests.set(requestKey, request as Promise<unknown>)
  }

  try {
    return await request
  } finally {
    if (shouldDedupe) {
      inflightGetRequests.delete(requestKey)
    }
  }
}

export async function postJson<TResp, TReq>(path: string, body?: TReq, config?: AxiosRequestConfig) {
  const res = await getAxios().post<ApiResult<TResp>>(path, body, config)
  clearHttpGetCache()
  return res.data.data as TResp
}

export async function putJson<TResp, TReq>(path: string, body?: TReq, config?: AxiosRequestConfig) {
  const res = await getAxios().put<ApiResult<TResp>>(path, body, config)
  clearHttpGetCache()
  return res.data.data as TResp
}

export async function deleteJson<TResp = void>(
  path: string,
  config?: AxiosRequestConfig,
): Promise<TResp> {
  const res = await getAxios().delete<ApiResult<TResp>>(path, config)
  clearHttpGetCache()
  const payload = res.data
  return (payload.data as TResp | undefined) as TResp
}
