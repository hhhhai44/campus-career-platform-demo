import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import axios from 'axios'

export type ApiResult<T> = {
  code: number
  errorMsg?: string
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

const defaultConfig: Required<HttpConfig> = {
  baseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api',
  getToken: () => null,
}

let axiosInstance: AxiosInstance | null = null

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
      if (data.code !== 1) throw new ApiError(data.errorMsg || '请求失败', data.code)
      return response
    },
    (error) => {
      if (error.response) {
        const { status, statusText } = error.response
        throw new ApiError(`网络错误：${status} ${statusText}`, status)
      }
      throw new ApiError(error.message || '网络异常')
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

export async function getJson<T>(path: string, config?: AxiosRequestConfig) {
  const res = await getAxios().get<ApiResult<T>>(path, config)
  return res.data.data as T
}

export async function postJson<TResp, TReq>(path: string, body?: TReq, config?: AxiosRequestConfig) {
  const res = await getAxios().post<ApiResult<TResp>>(path, body, config)
  return res.data.data as TResp
}

export async function putJson<TResp, TReq>(path: string, body?: TReq, config?: AxiosRequestConfig) {
  const res = await getAxios().put<ApiResult<TResp>>(path, body, config)
  return res.data.data as TResp
}

export async function deleteJson<TResp = void>(
  path: string,
  config?: AxiosRequestConfig,
): Promise<TResp> {
  const res = await getAxios().delete<ApiResult<TResp>>(path, config)
  const payload = res.data
  return (payload.data as TResp | undefined) as TResp
}
