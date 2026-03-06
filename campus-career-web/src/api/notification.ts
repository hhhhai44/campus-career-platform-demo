import { getJson, postJson } from '@/api/http'

export type Notification = {
  id: number
  /** 通知类型 code，提交/筛选用 */
  type: number
  /** 通知类型可读名称，展示用 */
  typeDesc?: string | null
  typeName?: string | null
  /** 业务类型 code */
  bizType: number
  /** 业务类型可读名称，展示用 */
  bizTypeDesc?: string | null
  bizTypeName?: string | null
  bizId: number
  title: string
  content: string
  fromUserId: number
  fromUserName: string | null
  /** 是否已读 code：0-未读 1-已读 */
  isRead: number
  /** 已读状态可读名称，展示用 */
  isReadDesc?: string | null
  isReadName?: string | null
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
    return getJson<number>('/notification/unread/count')
  },
}

