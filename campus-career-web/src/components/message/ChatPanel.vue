<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import MessageBubble from '@/components/message/MessageBubble.vue'
import type { ChatMessage, ConversationView, MessageAttachment } from '@/types/message'

const props = defineProps<{
  conversation: ConversationView | null
  messages: ChatMessage[]
  draft: string
  attachments: MessageAttachment[]
  sending: boolean
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'update:draft', value: string): void
  (e: 'update:attachments', value: MessageAttachment[]): void
  (e: 'send'): void
  (e: 'copy-message', message: ChatMessage): void
  (e: 'recall-message', message: ChatMessage): void
  (e: 'delete-message', message: ChatMessage): void
  (e: 'toggle-pin'): void
  (e: 'toggle-mute'): void
  (e: 'refresh'): void
}>()

const showEmojiPanel = ref(false)
const messageListRef = ref<HTMLElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const imageInputRef = ref<HTMLInputElement | null>(null)

const emojiList = ['😀', '😄', '😊', '😉', '🥳', '🤝', '🚀', '📚', '💡', '👍', '✨', '🎯']

const pinText = computed(() => (props.conversation?.pinned ? '取消置顶' : '置顶'))
const muteText = computed(() => (props.conversation?.muted ? '取消免打扰' : '免打扰'))

function scrollToBottom() {
  nextTick(() => {
    const el = messageListRef.value
    if (!el) return
    el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
  })
}

function handleComposerKeydown(e: KeyboardEvent) {
  if (e.key !== 'Enter') return
  if (e.ctrlKey || e.metaKey) {
    emit('update:draft', `${props.draft}\n`)
    return
  }
  e.preventDefault()
  if (!props.sending && props.conversation) {
    emit('send')
  }
}

function triggerFilePicker(type: 'image' | 'file') {
  if (type === 'image') {
    imageInputRef.value?.click()
  } else {
    fileInputRef.value?.click()
  }
}

function onFilesPicked(event: Event, type: 'image' | 'file') {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files || [])
  if (!files.length) return

  const nextAttachments = [...props.attachments]
  for (const file of files) {
    const id = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
    const url = URL.createObjectURL(file)
    nextAttachments.push({
      id,
      name: file.name,
      type,
      size: file.size,
      url,
      previewUrl: type === 'image' ? url : undefined,
    })
  }
  emit('update:attachments', nextAttachments)
  input.value = ''
}

function addEmoji(emoji: string) {
  emit('update:draft', `${props.draft}${emoji}`)
}

function removeAttachment(id: string) {
  const target = props.attachments.find((item) => item.id === id)
  if (target?.previewUrl?.startsWith('blob:')) {
    URL.revokeObjectURL(target.previewUrl)
  }
  emit('update:attachments', props.attachments.filter((item) => item.id !== id))
}

function handleQuickAction(action: 'emoji' | 'image' | 'file') {
  if (action === 'emoji') {
    showEmojiPanel.value = !showEmojiPanel.value
    return
  }
  triggerFilePicker(action)
}

watch(
  () => props.messages.length,
  () => scrollToBottom(),
)

watch(
  () => props.conversation?.peerUserId,
  () => {
    showEmojiPanel.value = false
    scrollToBottom()
  },
)

onMounted(() => {
  scrollToBottom()
})

onBeforeUnmount(() => {
  for (const attachment of props.attachments) {
    if (attachment.previewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(attachment.previewUrl)
    }
  }
})
</script>

<template>
  <section class="chat-panel">
    <div v-if="!conversation" class="empty-shell">
      <div class="illus">
        <div class="circle big"></div>
        <div class="circle mid"></div>
        <div class="circle small"></div>
      </div>
      <div class="empty-title">选择一个会话开始聊天</div>
      <div class="empty-desc">在左侧选择同学，或者从帖子详情页直接发起私信。</div>
    </div>

    <template v-else>
      <header class="chat-header">
        <div class="peer-info">
          <el-avatar :size="48" class="avatar">{{ conversation.peerUsername.slice(0, 1).toUpperCase() }}</el-avatar>
          <div class="text-group">
            <div class="name-row">
              <span class="name">{{ conversation.peerUsername }}</span>
            </div>
            <div class="sub-row">
              <span>未读 {{ conversation.unreadCount }}</span>
            </div>
          </div>
        </div>

        <div class="header-actions">
          <el-button text type="primary" @click="emit('toggle-pin')">{{ pinText }}</el-button>
          <el-button text @click="emit('toggle-mute')">{{ muteText }}</el-button>
          <el-button text @click="emit('refresh')">刷新</el-button>
        </div>
      </header>

      <div ref="messageListRef" class="message-area">
        <el-skeleton v-if="loading" animated :rows="8" />
        <template v-else>
          <MessageBubble
            v-for="message in messages"
            :key="message.id"
            :message="message"
            @copy="emit('copy-message', $event)"
            @recall="emit('recall-message', $event)"
            @remove="emit('delete-message', $event)"
          />
        </template>
      </div>

      <footer class="composer">
        <div class="attachment-row" v-if="attachments.length">
          <div v-for="attachment in attachments" :key="attachment.id" class="attachment-pill">
            <span class="pill-name">{{ attachment.name }}</span>
            <el-button text size="small" @click="removeAttachment(attachment.id)">×</el-button>
          </div>
        </div>

        <div class="composer-box">
          <div class="toolbar">
            <el-button text @click="handleQuickAction('emoji')">😊 表情</el-button>
            <el-button text @click="handleQuickAction('image')">图片</el-button>
            <el-button text @click="handleQuickAction('file')">文件</el-button>
            <span class="hint">Enter 发送 / Ctrl+Enter 换行</span>
          </div>

          <div v-if="showEmojiPanel" class="emoji-panel">
            <button v-for="emoji in emojiList" :key="emoji" type="button" class="emoji-btn" @click="addEmoji(emoji)">
              {{ emoji }}
            </button>
          </div>

          <el-input
            :model-value="draft"
            @update:model-value="emit('update:draft', $event)"
            type="textarea"
            :rows="5"
            resize="none"
            placeholder="输入消息，支持表情、图片和文件预览"
            :disabled="loading"
            @keydown="handleComposerKeydown"
          />

          <div class="actions-row">
            <div class="side-note">
              <span>主色 #1890ff</span>
            </div>
            <el-button type="primary" :loading="sending" class="send-btn" @click="emit('send')">
              <span v-if="sending" class="send-text">发送中...</span>
              <span v-else>发送消息</span>
            </el-button>
          </div>
        </div>
      </footer>

      <input ref="fileInputRef" type="file" class="hidden-input" multiple @change="(e) => onFilesPicked(e, 'file')" />
      <input
        ref="imageInputRef"
        type="file"
        class="hidden-input"
        accept="image/*"
        multiple
        @change="(e) => onFilesPicked(e, 'image')"
      />
    </template>
  </section>
</template>

<style scoped>
.chat-panel {
  flex: 1;
  min-width: 0;
  background: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 640px;
}

.empty-shell {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-align: center;
  padding: 28px;
}

.illus {
  position: relative;
  width: 132px;
  height: 108px;
}

.circle {
  position: absolute;
  border-radius: 999px;
  background: rgba(24, 144, 255, 0.16);
}

.big {
  width: 96px;
  height: 96px;
  left: 0;
  bottom: 0;
}

.mid {
  width: 68px;
  height: 68px;
  right: 0;
  top: 6px;
  background: rgba(24, 144, 255, 0.24);
}

.small {
  width: 42px;
  height: 42px;
  left: 46px;
  top: 34px;
  background: rgba(24, 144, 255, 0.34);
}

.empty-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--ccp-text);
}

.empty-desc {
  font-size: 14px;
  color: var(--ccp-text-muted);
  line-height: 1.6;
  max-width: 340px;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #edf1f5;
}

.peer-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.avatar {
  background: linear-gradient(135deg, #1890ff, #69b7ff);
  color: #fff;
  font-weight: 700;
}

.text-group {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name-row,
.sub-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.name {
  font-size: 18px;
  font-weight: 700;
  color: var(--ccp-text);
}

.sub-row {
  font-size: 13px;
  color: var(--ccp-text-muted);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: none;
}

.message-area {
  flex: 1;
  overflow: auto;
  padding: 22px 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: linear-gradient(180deg, #f8f9fa 0%, #f5f7fb 100%);
}

.composer {
  padding: 16px 20px 20px;
  background: #fff;
  border-top: 1px solid #edf1f5;
}

.attachment-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.attachment-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(24, 144, 255, 0.08);
  color: #1677ff;
  max-width: 100%;
}

.pill-name {
  max-width: 220px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 12px;
}

.composer-box {
  position: relative;
  border-radius: 8px;
  border: 1px solid #d9e2f0;
  overflow: hidden;
  background: #fff;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid #edf1f5;
  flex-wrap: wrap;
}

.hint {
  margin-left: auto;
  font-size: 12px;
  color: #94a3b8;
}

.emoji-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid #edf1f5;
  background: #fff;
}

.emoji-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #edf1f5;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: all 0.15s ease;
}

.emoji-btn:hover {
  transform: translateY(-1px);
  border-color: rgba(24, 144, 255, 0.3);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
}

.actions-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  background: #fff;
}

.side-note {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #94a3b8;
  flex-wrap: wrap;
}

.send-btn {
  min-width: 120px;
  box-shadow: 0 10px 22px rgba(24, 144, 255, 0.16);
}

.send-text {
  position: relative;
}

.hidden-input {
  display: none;
}

@media (max-width: 900px) {
  .chat-panel {
    min-height: 560px;
  }

  .chat-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .hint {
    margin-left: 0;
  }

  .actions-row {
    flex-direction: column;
    align-items: stretch;
  }

  .send-btn {
    width: 100%;
  }
}
</style>

