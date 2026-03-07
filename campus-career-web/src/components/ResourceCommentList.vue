<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { resourceCommentApi, type ResourceComment } from '@/api/resourceComment'

const props = defineProps<{
  comments: ResourceComment[]
  resourceId: number
}>()

const emit = defineEmits<{
  refresh: []
  reply: [comment: ResourceComment]
}>()

const auth = useAuthStore()

async function handleDelete(comment: ResourceComment) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await resourceCommentApi.delete(comment.id)
    ElMessage.success('删除成功')
    emit('refresh')
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err?.message || '删除失败')
    }
  }
}

function handleReply(comment: ResourceComment) {
  emit('reply', comment)
}
</script>

<template>
  <div class="comment-list">
    <div v-for="item in props.comments" :key="item.id" class="comment">
      <div class="comment-header">
        <div class="avatar">
          {{ item.fromUserName?.slice(0, 1)?.toUpperCase() || 'U' }}
        </div>
        <div class="comment-main">
          <div class="line">
            <span class="author">{{ item.fromUserName || '未知用户' }}</span>
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
          <div class="comment-actions">
            <el-button text type="primary" size="small" @click="handleReply(item)">回复</el-button>
            <el-button v-if="auth.isAuthed" text type="danger" size="small" @click="handleDelete(item)">
              删除
            </el-button>
          </div>
        </div>
      </div>
      <div v-if="item.children && item.children.length" class="children">
        <ResourceCommentList
          :comments="item.children"
          :resource-id="resourceId"
          @refresh="emit('refresh')"
          @reply="emit('reply', $event)"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment {
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.comment-header {
  display: flex;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #111827;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.comment-main {
  flex: 1;
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
  margin-top: 6px;
  font-size: 13px;
  color: #111827;
  line-height: 1.6;
}

.comment-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.children {
  margin-top: 8px;
  padding-left: 20px;
  border-left: 2px solid #e5e7eb;
}
</style>

