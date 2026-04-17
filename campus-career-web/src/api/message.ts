import { deleteJson, getJson, postJson } from '@/api/http'

export type PageResult<T> = {
  records: T[]
  total: number
  current: number
  size: number
}

export type Conversation = {
  peerUserId: number
  peerUsername: string
  lastSenderId?: number | null
  lastMessage?: string | null
  lastTime?: string | null
  unreadCount: number
  pinned?: boolean
  muted?: boolean
}

export type PrivateMessage = {
  id: number
  fromUserId: number
  fromUsername: string
  toUserId: number
  toUsername: string
  content: string
  isRead: number
  isReadDesc?: string | null
  createTime: string
  mine: boolean
  recalled?: boolean
}

export const messageApi = {
  conversationPage(page = 1, size = 10) {
    return getJson<PageResult<Conversation>>('/message/conversation/page', { params: { page, size } })
  },

  sessionPage(peerUserId: number, page = 1, size = 20) {
    return getJson<PageResult<PrivateMessage>>(`/message/session/${peerUserId}/page`, {
      params: { page, size },
    })
  },

  send(payload: { toUserId: number; content: string }) {
    return postJson<void, { toUserId: number; content: string }>('/message/send', payload)
  },

  markSessionRead(peerUserId: number) {
    return postJson<void, unknown>(`/message/session/${peerUserId}/read`)
  },

  setSessionPinned(peerUserId: number, pinned: boolean) {
    return postJson<void, unknown>(`/message/session/${peerUserId}/pin`, undefined, {
      params: { pinned },
    })
  },

  setSessionMuted(peerUserId: number, muted: boolean) {
    return postJson<void, unknown>(`/message/session/${peerUserId}/mute`, undefined, {
      params: { muted },
    })
  },

  recallMessage(messageId: number | string) {
    return postJson<void, unknown>(`/message/${messageId}/recall`)
  },

  deleteMessage(messageId: number | string) {
    return deleteJson<void>(`/message/${messageId}`)
  },

  unreadCount() {
    return getJson<number>('/message/unread/count', { cacheTtlMs: 10 * 1000 })
  },
}

