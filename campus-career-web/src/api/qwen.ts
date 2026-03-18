import { ApiError, postJson } from './http'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/+$/, '')
const LS_TOKEN = 'ccp_token'

export type QwenAskReq = {
  question: string
  // TODO(Agent): 后续可扩展 sessionId/history/knowledgeBaseId 等字段
}

export type QwenAskResp = {
  answer: string
  model: string
  // TODO(Agent): 后续可加入 usage、引用来源、工具调用轨迹等
}

export type QwenAskStreamOptions = {
  signal?: AbortSignal
  onDelta?: (content: string) => void
  onDone?: (payload?: unknown) => void
  onError?: (message: string) => void
}

export async function qwenAsk(question: string) {
  const body: QwenAskReq = { question }
  return await postJson<QwenAskResp, QwenAskReq>('/qwen/ask', body)
}

export async function qwenAskStream(question: string, opts: QwenAskStreamOptions = {}) {
  const token = localStorage.getItem(LS_TOKEN)
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    Accept: 'text/event-stream',
  }
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(`${API_BASE_URL}/qwen/ask/stream`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ question }),
    signal: opts.signal,
  })

  if (!res.ok) {
    throw new ApiError(`请求失败：HTTP ${res.status}`, res.status)
  }

  if (!res.body) {
    throw new ApiError('浏览器不支持流式响应')
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const blocks = buffer.split(/\r?\n\r?\n/)
    buffer = blocks.pop() ?? ''

    for (const block of blocks) {
      handleSseBlock(block, opts)
    }
  }

  if (buffer.trim()) {
    handleSseBlock(buffer, opts)
  }
}

function handleSseBlock(block: string, opts: QwenAskStreamOptions) {
  const lines = block.split(/\r?\n/)
  let eventName = 'message'
  const dataLines: string[] = []

  for (const line of lines) {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim() || 'message'
      continue
    }
    if (line.startsWith('data:')) {
      // SSE 规范中 data: 后首个空格应忽略，其余字符需原样保留
      const value = line.slice(5)
      dataLines.push(value.startsWith(' ') ? value.slice(1) : value)
    }
  }

  if (!dataLines.length) return
  const payload = dataLines.join('\n')

  if (eventName === 'delta') {
    opts.onDelta?.(decodeMaybeJsonString(payload))
    return
  }

  if (eventName === 'done') {
    try {
      opts.onDone?.(JSON.parse(payload))
    } catch {
      opts.onDone?.(payload)
    }
    return
  }

  if (eventName === 'error') {
    const msg = decodeMaybeJsonString(payload)
    opts.onError?.(msg)
    throw new ApiError(msg || '流式请求失败')
  }

  // 兼容无事件名场景：尽量按 OpenAI chunk 格式读取 delta
  if (payload.trim() === '[DONE]') {
    opts.onDone?.()
    return
  }

  try {
    const json = JSON.parse(payload)
    const delta = json?.choices?.[0]?.delta?.content
    if (typeof delta === 'string' && delta.length > 0) {
      opts.onDelta?.(delta)
    }
  } catch {
    opts.onDelta?.(decodeMaybeJsonString(payload))
  }
}

function decodeMaybeJsonString(input: string) {
  const text = input
  if (!text) return text
  const first = text[0]
  const last = text[text.length - 1]
  if ((first === '"' && last === '"') || (first === "'" && last === "'")) {
    try {
      const parsed = JSON.parse(text)
      if (typeof parsed === 'string') return parsed
    } catch {
      // ignore and fallback to raw text
    }
  }
  return text
}
