<script setup lang="ts">
import type { ChatMessage } from '@/types/message'

const props = defineProps<{
  message: ChatMessage
}>()

const emit = defineEmits<{
  (e: 'copy', message: ChatMessage): void
  (e: 'recall', message: ChatMessage): void
  (e: 'remove', message: ChatMessage): void
}>()

function formatSize(value: number) {
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / (1024 * 1024)).toFixed(1)} MB`
}

function statusText() {
  if (props.message.deleted) return '已删除'
  if (props.message.recalled) return '已撤回'
  if (props.message.status === 'sending') return '发送中'
  if (props.message.status === 'failed') return '发送失败'
  if (props.message.mine) {
    return props.message.isRead === 1 ? '已读' : '已发送'
  }
  return props.message.isRead === 1 ? '对方已读' : '未读'
}

async function copyMessage() {
  emit('copy', props.message)
}
</script>

<template>
  <div class="message-row" :class="{ mine: message.mine }">
    <div class="avatar" :class="{ mine: message.mine }">
      {{ (message.mine ? message.toUsername : message.fromUsername)?.slice(0, 1) || 'U' }}
    </div>

    <div class="content-wrap">
      <div class="author" v-if="!message.mine">{{ message.fromUsername }}</div>
      <div class="bubble" :class="{ mine: message.mine }">
        <div v-if="message.deleted" class="placeholder">消息已删除</div>
        <div v-else-if="message.recalled" class="placeholder">{{ message.content || '消息已撤回' }}</div>
        <template v-else>
          <div class="text">{{ message.content }}</div>
          <div v-if="message.attachments.length" class="attachments">
            <div
              v-for="attachment in message.attachments"
              :key="attachment.id"
              class="attachment"
              :class="attachment.type"
            >
              <img
                v-if="attachment.type === 'image'"
                :src="attachment.previewUrl || attachment.url"
                :alt="attachment.name"
              />
              <div v-else class="file-card">
                <div class="file-name">{{ attachment.name }}</div>
                <div class="file-size">{{ formatSize(attachment.size) }}</div>
              </div>
            </div>
          </div>
        </template>

        <div class="footer">
          <span class="time">{{ new Date(message.createTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}</span>
          <span class="status">{{ statusText() }}</span>
        </div>
      </div>

      <div class="actions">
        <el-button text size="small" @click="copyMessage">复制</el-button>
        <el-button
          v-if="message.mine && message.recallable && !message.recalled && !message.deleted && message.status !== 'sending'"
          text
          size="small"
          @click="emit('recall', message)"
        >
          撤回
        </el-button>
        <el-button text size="small" @click="emit('remove', message)">
          删除
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.message-row.mine {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e9eefc;
  color: var(--ccp-primary);
  font-weight: 700;
  font-size: 14px;
}

.avatar.mine {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
}

.content-wrap {
  max-width: min(72%, 620px);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.message-row.mine .content-wrap {
  align-items: flex-end;
}

.author {
  font-size: 12px;
  color: #6b7280;
}

.bubble {
  border-radius: 16px 16px 16px 6px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 12px 14px;
  color: #111827;
}

.bubble.mine {
  border-radius: 16px 16px 6px 16px;
  background: linear-gradient(135deg, #1890ff 0%, #4aa3ff 100%);
  color: #fff;
}

.text {
  font-size: 14px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.placeholder {
  font-size: 14px;
  color: inherit;
  opacity: 0.78;
}

.attachments {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 8px;
}

.attachment {
  overflow: hidden;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.18);
}

.attachment.image img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}

.file-card {
  padding: 12px;
  background: rgba(255, 255, 255, 0.12);
}

.file-name {
  font-size: 13px;
  font-weight: 600;
  word-break: break-word;
}

.file-size {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.85;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  color: #94a3b8;
}

.bubble.mine .footer {
  color: rgba(255, 255, 255, 0.8);
}

.actions {
  display: flex;
  gap: 6px;
  opacity: 0;
  transform: translateY(-2px);
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.message-row:hover .actions {
  opacity: 1;
  transform: translateY(0);
}

@media (max-width: 768px) {
  .content-wrap {
    max-width: 100%;
  }

  .actions {
    opacity: 1;
  }
}
</style>

