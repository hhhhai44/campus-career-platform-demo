export type MessageAttachmentType = 'image' | 'file'

export type MessageAttachment = {
  id: string
  name: string
  type: MessageAttachmentType
  size: number
  url: string
  previewUrl?: string
}

export type MessageStatus = 'sending' | 'sent' | 'read' | 'failed'

export type ConversationView = {
  peerUserId: number
  peerUsername: string
  peerAvatar: string
  online: boolean
  lastSenderId?: number | null
  lastMessage?: string | null
  lastTime?: string | null
  unreadCount: number
  pinned: boolean
  muted: boolean
}

export type ChatMessage = {
  id: number | string
  fromUserId: number
  fromUsername: string
  toUserId: number
  toUsername: string
  content: string
  createTime: string
  mine: boolean
  isRead: number
  status: MessageStatus
  attachments: MessageAttachment[]
  recalled?: boolean
  recallable?: boolean
  deleted?: boolean
}
