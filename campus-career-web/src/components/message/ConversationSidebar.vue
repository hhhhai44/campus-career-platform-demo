<script setup lang="ts">
import { computed } from 'vue'
import type { ConversationView } from '@/types/message'

const props = defineProps<{
  conversations: ConversationView[]
  activePeerId: number | null
  loading: boolean
  searchText: string
}>()

const emit = defineEmits<{
  (e: 'update:searchText', value: string): void
  (e: 'select', peerUserId: number): void
  (e: 'toggle-pin', peerUserId: number): void
  (e: 'toggle-mute', peerUserId: number): void
}>()

const filteredConversations = computed(() => {
  const keyword = props.searchText.trim().toLowerCase()
  const list = keyword
    ? props.conversations.filter((item) => {
        return (
          item.peerUsername.toLowerCase().includes(keyword)
          || (item.lastMessage || '').toLowerCase().includes(keyword)
          || String(item.peerUserId).includes(keyword)
        )
      })
    : props.conversations

  return [...list].sort((a, b) => {
    const pinDiff = Number(b.pinned) - Number(a.pinned)
    if (pinDiff !== 0) return pinDiff
    const unreadDiff = Number(b.unreadCount > 0) - Number(a.unreadCount > 0)
    if (unreadDiff !== 0) return unreadDiff
    return new Date(b.lastTime || 0).getTime() - new Date(a.lastTime || 0).getTime()
  })
})

function formatTime(value?: string | null) {
  if (!value) return ''
  return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function avatarText(name: string) {
  return (name?.trim().slice(0, 1) || 'U').toUpperCase()
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-head">
      <div>
        <div class="title">私信中心</div>
        <div class="sub">与同学一对一沟通，快速同步问题进展</div>
      </div>
    </div>

    <el-input
      :model-value="searchText"
      @update:model-value="emit('update:searchText', $event)"
      placeholder="搜索同学或私信内容..."
      clearable
    >
      <template #prefix>
        <span class="prefix-icon">⌕</span>
      </template>
    </el-input>

    <div class="tip-row">
      <span>{{ conversations.length }} 个会话</span>
      <span>支持置顶 / 免打扰</span>
    </div>

    <div v-if="loading" class="skeletons">
      <el-skeleton v-for="i in 4" :key="i" animated :rows="2" />
    </div>

    <div v-else-if="!filteredConversations.length" class="empty-state">
      <div class="illus">
        <div class="illus-card card-a"></div>
        <div class="illus-card card-b"></div>
        <div class="illus-card card-c"></div>
      </div>
      <div class="empty-title">还没有会话</div>
      <div class="empty-desc">从帖子、评论、资源中的头像或用户名点击即可发起私信。</div>
    </div>

    <div v-else class="conversation-list">
      <button
        v-for="item in filteredConversations"
        :key="item.peerUserId"
        type="button"
        class="conversation-item"
        :class="{ active: item.peerUserId === activePeerId, pinned: item.pinned, muted: item.muted }"
        @click="emit('select', item.peerUserId)"
      >
        <span v-if="item.peerUserId === activePeerId" class="active-bar"></span>
        <el-avatar :size="44" class="avatar">{{ avatarText(item.peerUsername) }}</el-avatar>
        <div class="meta">
          <div class="row-top">
            <span class="name">{{ item.peerUsername }}</span>
            <span class="time">{{ formatTime(item.lastTime) }}</span>
          </div>
          <div class="row-mid">
            <span class="preview">{{ item.lastMessage || '暂无消息，开始打个招呼吧' }}</span>
            <el-badge :value="item.unreadCount" :hidden="!item.unreadCount" :max="99" />
          </div>
          <div class="tags">
            <el-tag v-if="item.pinned" size="small" type="warning" effect="light">置顶</el-tag>
            <el-tag v-if="item.muted" size="small" type="info" effect="light">免打扰</el-tag>
            <el-tag v-if="item.online" size="small" type="success" effect="light">在线</el-tag>
          </div>
        </div>

        <div class="actions" @click.stop>
          <el-button text size="small" @click="emit('toggle-pin', item.peerUserId)">
            {{ item.pinned ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button text size="small" @click="emit('toggle-mute', item.peerUserId)">
            {{ item.muted ? '取消免打扰' : '免打扰' }}
          </el-button>
        </div>
      </button>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 300px;
  min-width: 300px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: calc(100vh - 136px);
  min-height: 640px;
  overflow: hidden;
}

.sidebar-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: var(--ccp-text);
}

.sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--ccp-text-muted);
  line-height: 1.45;
}

.prefix-icon {
  font-size: 14px;
  color: #94a3b8;
}

.tip-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  color: #94a3b8;
}

.skeletons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 10px;
  padding: 28px 14px;
}

.illus {
  position: relative;
  width: 108px;
  height: 90px;
}

.illus-card {
  position: absolute;
  border-radius: 18px;
  box-shadow: 0 16px 32px rgba(24, 144, 255, 0.15);
}

.card-a {
  width: 72px;
  height: 72px;
  left: 0;
  top: 10px;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.24), rgba(24, 144, 255, 0.08));
}

.card-b {
  width: 54px;
  height: 54px;
  right: 4px;
  top: 0;
  background: rgba(24, 144, 255, 0.18);
}

.card-c {
  width: 42px;
  height: 42px;
  right: 0;
  bottom: 0;
  background: rgba(24, 144, 255, 0.12);
}

.empty-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ccp-text);
}

.empty-desc {
  font-size: 13px;
  color: var(--ccp-text-muted);
  line-height: 1.6;
}

.conversation-list {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-right: 2px;
}

.conversation-item {
  position: relative;
  width: 100%;
  border: 1px solid #eef2f7;
  background: #fff;
  border-radius: 8px;
  padding: 12px 12px 12px 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  text-align: left;
  cursor: pointer;
  transition: all 0.18s ease;
}

.conversation-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  border-color: rgba(24, 144, 255, 0.25);
}

.conversation-item.active {
  background: rgba(24, 144, 255, 0.06);
  border-color: rgba(24, 144, 255, 0.28);
}

.conversation-item.muted {
  opacity: 0.88;
}

.active-bar {
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 4px;
  border-radius: 0 999px 999px 0;
  background: var(--ccp-primary);
}

.avatar {
  flex: none;
  background: linear-gradient(135deg, #1890ff, #4aa3ff);
  color: #fff;
  font-weight: 700;
}

.meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.row-top,
.row-mid {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ccp-text);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.time {
  font-size: 11px;
  color: #9ca3af;
  flex: none;
}

.preview {
  font-size: 12px;
  color: var(--ccp-text-muted);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex: none;
}

@media (max-width: 900px) {
  .sidebar {
    width: 100%;
    min-width: 0;
    height: auto;
    min-height: 0;
  }
}
</style>

