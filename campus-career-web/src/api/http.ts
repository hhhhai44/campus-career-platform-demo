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

function joinUrl(base: string, path: string) {
  const b = base.replace(/\/+$/, '')
  const p = path.startsWith('/') ? path : `/${path}`
  return `${b}${p}`
}

/**
 * 最小 HTTP 封装（基于 fetch）：
 * - 兼容后端统一返回 Result<T>：{ code, errorMsg, data }
 * - code === 1 视为成功（与后端 `Result.success()` 一致）
 */
export async function postJson<TResp, TReq>(
  path: string,
  body: TReq,
  opts?: { baseUrl?: string; token?: string | null },
): Promise<TResp> {
  const baseUrl = opts?.baseUrl ?? import.meta.env.VITE_API_BASE_URL ?? '/api'
  const url = joinUrl(baseUrl, path)

  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (opts?.token) headers.Authorization = `Bearer ${opts.token}`

  const res = await fetch(url, { method: 'POST', headers, body: JSON.stringify(body) })
  if (!res.ok) throw new ApiError(`网络错误：${res.status} ${res.statusText}`)

  const json = (await res.json()) as ApiResult<TResp>
  if (json.code !== 1) throw new ApiError(json.errorMsg || '登录失败', json.code)
  if (json.data === undefined) throw new ApiError('响应缺少 data 字段')
  return json.data
}


