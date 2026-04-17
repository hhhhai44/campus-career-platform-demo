<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { resourceCommentApi, type ResourceComment } from '@/api/resourceComment'

const props = defineProps<{
  comments: ResourceComment[]
  resourceId: number
}>()

const emit = defineEmits<{
  refresh: []
  reply: [comment: ResourceComment]
  report: [comment: ResourceComment]
}>()

const auth = useAuthStore()
const router = useRouter()
const likeLoadingMap = reactive<Record<number, boolean>>({})

function goMessage(userId: number | null | undefined, userName?: string | null) {
  if (!userId || (auth.userId && auth.userId === userId)) return
  router.push({
    name: 'message-center',
    query: {
      peer: String(userId),
      ...(userName ? { peerName: userName } : {}),
    },
  })
}

async function handleDelete(comment: ResourceComment) {
  try {
    await ElMessageBox.confirm('确定要删除吗？这个操作无法撤销。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await resourceCommentApi.delete(comment.id)
    ElMessage.success('评论已删除')
    emit('refresh')
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err?.message || '删除失败，稍后再试试')
    }
  }
}

function handleReply(comment: ResourceComment) {
  emit('reply', comment)
}

function handleReport(comment: ResourceComment) {
  emit('report', comment)
}

async function handleLike(comment: ResourceComment) {
  if (!auth.isAuthed || likeLoadingMap[comment.id]) return
  likeLoadingMap[comment.id] = true
  try {
    const resp = await resourceCommentApi.likeToggle(comment.id)
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
        <button
          v-if="!auth.userId || auth.userId !== item.fromUserId"
          class="avatar"
          type="button"
          @click="goMessage(item.fromUserId, item.fromUserName)"
        >
          {{ item.fromUserName?.slice(0, 1)?.toUpperCase() || 'U' }}
        </button>
        <span v-else class="avatar avatar-static">{{ item.fromUserName?.slice(0, 1)?.toUpperCase() || 'U' }}</span>
        <div class="comment-main">
          <div class="line">
            <button
              v-if="!auth.userId || auth.userId !== item.fromUserId"
              type="button"
              class="author user-link"
              @click="goMessage(item.fromUserId, item.fromUserName)"
            >
              {{ item.fromUserName || '未知用户' }}
            </button>
            <span v-else class="author">{{ item.fromUserName || '未知用户' }}</span>
            <span v-if="item.toUserName" class="reply-to">
              回复
              <button
                v-if="item.toUserId && (!auth.userId || auth.userId !== item.toUserId)"
                type="button"
                class="author user-link"
                @click="goMessage(item.toUserId, item.toUserName)"
              >
                {{ item.toUserName }}
              </button>
              <span v-else class="author">{{ item.toUserName }}</span>
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
            <el-button text type="primary" size="small" @click="handleReply(item)">回复</el-button>
            <el-button
              v-if="auth.isAuthed && auth.userId !== item.fromUserId"
              text
              type="danger"
              size="small"
              @click="handleReport(item)"
            >
              举报
            </el-button>
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
          @report="emit('report', $event)"
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
  border: 0;
  cursor: pointer;
}

.avatar-static {
  cursor: default;
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

.user-link {
  border: 0;
  background: transparent;
  padding: 0;
  cursor: pointer;
  color: var(--ccp-primary);
}

.user-link:hover {
  text-decoration: underline;
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

