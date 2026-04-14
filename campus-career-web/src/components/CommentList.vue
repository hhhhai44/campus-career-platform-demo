<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { commentApi, type PostComment } from '@/api/comment'

const props = defineProps<{
  comments: PostComment[]
  postId: number
}>()

const emit = defineEmits<{
  refresh: []
  reply: [comment: PostComment]
}>()

const auth = useAuthStore()
const likeLoadingMap = reactive<Record<number, boolean>>({})

async function handleDelete(comment: PostComment) {
  try {
    await ElMessageBox.confirm('确定要删除吗？这个操作无法撤销。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await commentApi.delete(comment.id)
    ElMessage.success('评论已删除')
    emit('refresh')
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err?.message || '删除失败，稍后再试试')
    }
  }
}

function handleReply(comment: PostComment) {
  emit('reply', comment)
}

async function handleLike(comment: PostComment) {
  if (!auth.isAuthed || likeLoadingMap[comment.id]) return
  likeLoadingMap[comment.id] = true
  try {
    const resp = await commentApi.likeToggle(comment.id)
    comment.liked = resp.liked
    comment.likeCount = resp.likeCount
  } catch (err: any) {
    ElMessage.error(err?.message || '操作没成功，稍后再试试')
  } finally {
    likeLoadingMap[comment.id] = false
  }
}
</script>

<template>
  <div class="comment-list">
    <div v-for="item in props.comments" :key="item.id" class="comment ccp-card">
      <div class="comment-header">
        <div class="avatar">
          {{ item.fromUserName?.slice(0, 1)?.toUpperCase() || 'U' }}
        </div>
        <div class="comment-main">
          <div class="line">
            <span class="author">{{ item.fromUserName || '未知用户' }}</span>
            <span v-if="item.toUserName" class="reply-to">
              回复 <span class="author">{{ item.toUserName }}</span>
            </span>
            <span class="time">
              {{ new Date(item.createTime).toLocaleString() }}
            </span>
          </div>
          <div class="content">
            {{ item.content }}
          </div>
          <div class="comment-actions">
            <el-button
              text
              :type="item.liked ? 'primary' : 'default'"
              size="small"
              :loading="!!likeLoadingMap[item.id]"
              @click="handleLike(item)"
            >
              👍 {{ item.liked ? '已赞' : '点赞' }} {{ item.likeCount || 0 }}
            </el-button>
            <el-button text type="primary" size="small" @click="handleReply(item)">
              回复
            </el-button>
            <el-button
              v-if="auth.isAuthed"
              text
              type="danger"
              size="small"
              @click="handleDelete(item)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
      <div v-if="item.children && item.children.length" class="children">
        <CommentList
          :comments="item.children"
          :post-id="postId"
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
  padding: 14px 16px;
  border: 1px solid var(--ccp-card-border);
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(16, 24, 40, 0.04);
}

.comment-header {
  display: flex;
  gap: 10px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--ccp-primary-gradient);
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
  flex-wrap: wrap;
}

.author {
  font-weight: 700;
  color: var(--ccp-text);
}

.reply-to {
  color: var(--ccp-text-muted);
}

.time {
  margin-left: auto;
  color: var(--ccp-text-light);
}

.content {
  margin-top: 6px;
  font-size: 13px;
  color: var(--ccp-text-secondary);
  line-height: 1.6;
}

.comment-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.children {
  margin-top: 12px;
  padding-left: 18px;
  border-left: 2px solid rgba(74, 111, 255, 0.14);
}
</style>

