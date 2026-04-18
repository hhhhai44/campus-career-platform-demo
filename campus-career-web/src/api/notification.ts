import { getJson, postJson } from '@/api/http'

export type Notification = {
  id: number
  /** 通知类型可读名称，展示用 */
  typeDesc?: string | null
  title: string
  content: string
  /** 是否已读 code：0-未读 1-已读 */
  isRead: number
  createTime: string
}

export type PageResult<T> = {
  records: T[]
  total: number
  current: number
  size: number
}

export const notificationApi = {
  // GET /notification/page
  page(params: { page?: number; size?: number; type?: number | null; isRead?: number | null }) {
    return getJson<PageResult<Notification>>('/notification/page', { params })
  },

  // POST /notification/{id}/read
  markRead(id: number) {
    return postJson<void, unknown>(`/notification/${id}/read`)
  },

  // POST /notification/read/all
  markAllRead() {
    return postJson<void, unknown>('/notification/read/all')
  },

  // GET /notification/unread/count
  unreadCount() {
    return getJson<number>('/notification/unread/count', { cacheTtlMs: 10 * 1000 })
  },
}
