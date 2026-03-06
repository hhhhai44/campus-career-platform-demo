<script setup lang="ts">
import type { PostComment } from '@/api/comment'

const props = defineProps<{
  comments: PostComment[]
}>()
</script>

<template>
  <div class="comment-list">
    <div v-for="item in props.comments" :key="item.id" class="comment">
      <div class="line">
        <span class="author">{{ item.fromUserName }}</span>
        <span v-if="item.toUserName" class="reply-to">
          回复
          <span class="author">{{ item.toUserName }}</span>
        </span>
        <span class="time">
          {{ new Date(item.createTime).toLocaleString() }}
        </span>
      </div>
      <div class="content">
        {{ item.content }}
      </div>
      <div v-if="item.children && item.children.length" class="children">
        <CommentList :comments="item.children" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment {
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
}

.line {
  display: flex;
  gap: 8px;
  align-items: baseline;
  font-size: 12px;
}

.author {
  font-weight: 600;
  color: #374151;
}

.reply-to {
  color: #6b7280;
}

.time {
  margin-left: auto;
  color: #9ca3af;
}

.content {
  margin-top: 4px;
  font-size: 13px;
  color: #111827;
}

.children {
  margin-top: 6px;
  padding-left: 10px;
  border-left: 1px dashed #e5e7eb;
}
</style>

