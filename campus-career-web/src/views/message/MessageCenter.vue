<template>
  <div class="message-page">
    <div class="workspace">
      <ConversationSidebar
        :conversations="conversationViews"
        :active-peer-id="activePeerId"
        :loading="loadingConversations"
        :search-text="searchText"
        @update:search-text="searchText = $event"
        @select="openConversation"
        @toggle-pin="handleTogglePin"
        @toggle-mute="handleToggleMute"
      />

      <ChatPanel
        :conversation="currentConversation"
        :messages="displayMessages"
        :draft="draft"
        :attachments="attachments"
        :sending="sending"
        :loading="loadingMessages"
        @update:draft="draft = $event"
        @update:attachments="attachments = $event"
        @send="sendMessage"
        @copy-message="handleCopyMessage"
        @recall-message="handleRecallMessage"
        @delete-message="handleDeleteMessage"
        @toggle-pin="activePeerId && handleTogglePin(activePeerId)"
        @toggle-mute="activePeerId && handleToggleMute(activePeerId)"
        @refresh="refreshCurrent"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ChatPanel from '@/components/message/ChatPanel.vue'
import ConversationSidebar from '@/components/message/ConversationSidebar.vue'
import { messageApi, type Conversation as ApiConversation, type PrivateMessage } from '@/api/message'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import type { ChatMessage, ConversationView, MessageAttachment } from '@/types/message'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const messageStore = useMessageStore()

const loadingConversations = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)
const syncing = ref(false)

const rawConversations = ref<ApiConversation[]>([])
const activePeerId = ref<number | null>(null)
const serverMessages = ref<ChatMessage[]>([])
const pendingMessages = ref<ChatMessage[]>([])
const draft = ref('')
const attachments = ref<MessageAttachment[]>([])
const searchText = ref('')

const conversationViews = computed<ConversationView[]>(() =>
  rawConversations.value.map((item) => toConversationView(item)),
)

const currentConversation = computed<ConversationView | null>(() => {
  if (!activePeerId.value) return null
  return findConversation(activePeerId.value) || buildPlaceholderConversation(activePeerId.value)
})

const displayMessages = computed<ChatMessage[]>(() => {
  const list = [...serverMessages.value, ...pendingMessages.value]
  return list.sort((a, b) => {
    const diff = new Date(a.createTime).getTime() - new Date(b.createTime).getTime()
    if (diff !== 0) return diff
    return String(a.id).localeCompare(String(b.id))
  })
})

function avatarText(name: string) {
  return (name?.trim().slice(0, 1) || 'U').toUpperCase()
}

function resolvePeerFromRoute() {
  const raw = route.query.peer
  const val = Number(Array.isArray(raw) ? raw[0] : raw)
  return Number.isFinite(val) && val > 0 ? val : null
}

function resolvePeerNameFromRoute() {
  const raw = route.query.peerName
  const val = Array.isArray(raw) ? raw[0] : raw
  if (typeof val !== 'string') return null
  const normalized = val.trim()
  if (!normalized || /^用户\d+$/.test(normalized)) return null
  return normalized
}

function toConversationView(item: ApiConversation): ConversationView {
  return {
    peerUserId: item.peerUserId,
    peerUsername: item.peerUsername || `用户${item.peerUserId}`,
    peerAvatar: avatarText(item.peerUsername || `用户${item.peerUserId}`),
    online: false,
    lastSenderId: item.lastSenderId ?? null,
    lastMessage: item.lastMessage ?? null,
    lastTime: item.lastTime ?? null,
    unreadCount: item.unreadCount ?? 0,
    pinned: !!item.pinned,
    muted: !!item.muted,
  }
}

function findConversation(peerUserId: number | null): ConversationView | null {
  if (!peerUserId) return null
  const hit = rawConversations.value.find((item) => item.peerUserId === peerUserId)
  return hit ? toConversationView(hit) : null
}

function buildPlaceholderConversation(peerUserId: number, peerName?: string | null): ConversationView {
  const displayName = peerName || resolvePeerNameFromRoute() || `用户${peerUserId}`
  return {
    peerUserId,
    peerUsername: displayName,
    peerAvatar: avatarText(displayName),
    online: false,
    lastSenderId: null,
    lastMessage: null,
    lastTime: null,
    unreadCount: 0,
    pinned: false,
    muted: false,
  }
}

function ensureConversationVisible(peerUserId: number, peerName?: string | null) {
  if (rawConversations.value.some((item) => item.peerUserId === peerUserId)) return
  const displayName = peerName || `用户${peerUserId}`
  rawConversations.value = [
    {
      peerUserId,
      peerUsername: displayName,
      lastSenderId: null,
      lastMessage: null,
      lastTime: null,
      unreadCount: 0,
      pinned: false,
      muted: false,
    },
    ...rawConversations.value,
  ]
}

async function fetchConversations(options?: { silent?: boolean }) {
  if (!options?.silent) loadingConversations.value = true
  try {
    const page = await messageApi.conversationPage(1, 100)
    rawConversations.value = page.records || []

    const peerFromRoute = resolvePeerFromRoute()
    const peerNameFromRoute = resolvePeerNameFromRoute()
    if (peerFromRoute) {
      if (auth.userId && peerFromRoute === auth.userId) {
        activePeerId.value = null
        await router.replace({ name: 'message-center' })
        return
      }
      ensureConversationVisible(peerFromRoute, peerNameFromRoute)
      activePeerId.value = peerFromRoute
      return
    }

    if (!activePeerId.value && rawConversations.value.length) {
      const firstConversation = rawConversations.value[0]
      if (firstConversation) {
        activePeerId.value = firstConversation.peerUserId
      }
    }
  } catch {
    if (!options?.silent) ElMessage.error('会话加载失败，稍后再试')
  } finally {
    loadingConversations.value = false
  }
}

function normalizeMessage(message: PrivateMessage): ChatMessage {
  return {
    id: message.id,
    fromUserId: message.fromUserId,
    fromUsername: message.fromUsername,
    toUserId: message.toUserId,
    toUsername: message.toUsername,
    content: message.content,
    createTime: message.createTime,
    mine: message.mine,
    isRead: message.isRead,
    status: message.isRead === 1 ? 'read' : 'sent',
    attachments: [],
    recalled: message.recalled ?? false,
    deleted: false,
  }
}

async function loadMessages(peerUserId: number) {
  loadingMessages.value = true
  try {
    const page = await messageApi.sessionPage(peerUserId, 1, 100)
    serverMessages.value = (page.records || []).map((item) => normalizeMessage(item))
    pendingMessages.value = pendingMessages.value.filter((item) => item.toUserId === peerUserId)

    await messageApi.markSessionRead(peerUserId)
    await messageStore.fetchUnreadCount({ force: true })

    rawConversations.value = rawConversations.value.map((item) =>
      item.peerUserId === peerUserId ? { ...item, unreadCount: 0 } : item,
    )
  } catch {
    serverMessages.value = []
    ElMessage.error('消息加载失败，稍后再试')
  } finally {
    loadingMessages.value = false
  }
}

function openConversation(peerUserId: number) {
  if (!peerUserId) return
  if (auth.userId && peerUserId === auth.userId) {
    ElMessage.warning('不能和自己发私信')
    return
  }
  ensureConversationVisible(peerUserId)
  activePeerId.value = peerUserId
  draft.value = ''
  cleanupAttachments()
}

function cleanupAttachments() {
  for (const attachment of attachments.value) {
    if (attachment.previewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(attachment.previewUrl)
    }
  }
  attachments.value = []
}

function formatAttachmentSummary(items: MessageAttachment[]) {
  if (!items.length) return ''
  return items.map((item) => (item.type === 'image' ? `【图片】${item.name}` : `【文件】${item.name}`)).join('，')
}

function buildMessageContent(text: string, items: MessageAttachment[]) {
  const trimmed = text.trim()
  const summary = formatAttachmentSummary(items)
  if (trimmed && summary) return `${trimmed}\n${summary}`
  return trimmed || summary || '（空消息）'
}

function createTempMessage(content: string, files: MessageAttachment[]): ChatMessage {
  const now = new Date().toISOString()
  const peer = currentConversation.value
  return {
    id: `temp-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    fromUserId: auth.userId || 0,
    fromUsername: auth.username || '我',
    toUserId: activePeerId.value || 0,
    toUsername: peer?.peerUsername || '对方',
    content,
    createTime: now,
    mine: true,
    isRead: 0,
    status: 'sending',
    attachments: files,
    recalled: false,
    deleted: false,
  }
}

async function sendMessage() {
  if (!activePeerId.value || sending.value) return
  if (auth.userId && activePeerId.value === auth.userId) {
    ElMessage.warning('不能和自己发私信')
    return
  }

  const text = draft.value.trim()
  if (!text && !attachments.value.length) return

  const content = buildMessageContent(text, attachments.value)
  const tempMessage = createTempMessage(content, attachments.value.map((item) => ({ ...item })))
  pendingMessages.value = [...pendingMessages.value, tempMessage]

  sending.value = true
  try {
    await messageApi.send({ toUserId: activePeerId.value, content })
    pendingMessages.value = pendingMessages.value.filter((item) => item.id !== tempMessage.id)
    draft.value = ''
    cleanupAttachments()
    await Promise.all([fetchConversations({ silent: true }), loadMessages(activePeerId.value)])
  } catch (err: any) {
    pendingMessages.value = pendingMessages.value.map((item) =>
      item.id === tempMessage.id ? { ...item, status: 'failed' } : item,
    )
    ElMessage.error(err?.message || '发送失败，稍后再试')
  } finally {
    sending.value = false
  }
}

async function handleCopyMessage(message: ChatMessage) {
  try {
    await navigator.clipboard.writeText(message.content)
    ElMessage.success('已复制消息内容')
  } catch {
    ElMessage.error('复制失败，浏览器可能不支持剪贴板')
  }
}

async function handleRecallMessage(message: ChatMessage) {
  if (!message.mine || message.deleted || String(message.id).startsWith('temp-')) return
  try {
    await ElMessageBox.confirm('确定要撤回这条消息吗？', '撤回确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await messageApi.recallMessage(message.id)
    if (activePeerId.value) await loadMessages(activePeerId.value)
    ElMessage.success('消息已撤回')
  } catch (err: any) {
    if (err !== 'cancel') ElMessage.error(err?.message || '撤回失败，稍后再试')
  }
}

async function handleDeleteMessage(message: ChatMessage) {
  if (String(message.id).startsWith('temp-')) {
    pendingMessages.value = pendingMessages.value.filter((item) => item.id !== message.id)
    return
  }
  try {
    await ElMessageBox.confirm('删除后消息仅在当前账号隐藏，是否继续？', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await messageApi.deleteMessage(message.id)
    if (activePeerId.value) {
      await Promise.all([loadMessages(activePeerId.value), fetchConversations({ silent: true })])
    }
    ElMessage.success('消息已删除')
  } catch (err: any) {
    if (err !== 'cancel') ElMessage.error(err?.message || '删除失败，稍后再试')
  }
}

async function handleTogglePin(peerUserId: number) {
  const target = findConversation(peerUserId)
  try {
    await messageApi.setSessionPinned(peerUserId, !target?.pinned)
    await fetchConversations({ silent: true })
  } catch (err: any) {
    ElMessage.error(err?.message || '操作失败，稍后再试')
  }
}

async function handleToggleMute(peerUserId: number) {
  const target = findConversation(peerUserId)
  try {
    await messageApi.setSessionMuted(peerUserId, !target?.muted)
    await fetchConversations({ silent: true })
  } catch (err: any) {
    ElMessage.error(err?.message || '操作失败，稍后再试')
  }
}

async function refreshCurrent() {
  await fetchConversations({ silent: true })
  if (activePeerId.value) await loadMessages(activePeerId.value)
}

watch(
  () => activePeerId.value,
  async (peerId) => {
    if (!peerId) {
      serverMessages.value = []
      pendingMessages.value = []
      return
    }

    await loadMessages(peerId)
    if (String(route.query.peer || '') !== String(peerId)) {
      const peerName = currentConversation.value?.peerUsername
      await router.replace({
        name: 'message-center',
        query: {
          peer: String(peerId),
          ...(peerName ? { peerName } : {}),
        },
      })
    }
  },
)

watch(
  () => route.query.peer,
  () => {
    const peer = resolvePeerFromRoute()
    if (!peer) return
    if (auth.userId && peer === auth.userId) return
    if (peer !== activePeerId.value) {
      ensureConversationVisible(peer, resolvePeerNameFromRoute())
      activePeerId.value = peer
    }
  },
)

let syncTimer: number | undefined
onMounted(async () => {
  await Promise.all([fetchConversations(), messageStore.fetchUnreadCount({ force: true })])

  if (!activePeerId.value) {
    const peer = resolvePeerFromRoute()
    if (peer && (!auth.userId || peer !== auth.userId)) {
      ensureConversationVisible(peer, resolvePeerNameFromRoute())
      activePeerId.value = peer
    }
  }

  syncTimer = window.setInterval(async () => {
    if (syncing.value || !auth.isAuthed) return
    syncing.value = true
    try {
      await fetchConversations({ silent: true })
      if (activePeerId.value) {
        await loadMessages(activePeerId.value)
      }
      await messageStore.fetchUnreadCount({ force: true })
    } finally {
      syncing.value = false
    }
  }, 6000)
})

onBeforeUnmount(() => {
  if (syncTimer) window.clearInterval(syncTimer)
  cleanupAttachments()
})
</script>

<style scoped>
.message-page {
  min-height: calc(100vh - 116px);
  padding: 16px;
}

.workspace {
  display: flex;
  gap: 16px;
  align-items: stretch;
}

@media (max-width: 900px) {
  .message-page {
    padding: 12px;
  }

  .workspace {
    flex-direction: column;
  }
}
</style>


