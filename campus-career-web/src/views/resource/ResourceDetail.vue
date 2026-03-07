<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ApiError } from '@/api/http'
import { resourceApi, type ResourceDetail } from '@/api/resource'
import { ratingApi } from '@/api/rating'
import { resourceCommentApi, type ResourceComment } from '@/api/resourceComment'
import ResourceCommentList from '@/components/ResourceCommentList.vue'

const route = useRoute()

const resource = ref<ResourceDetail | null>(null)
const loading = ref(false)

const score = ref(0)
const submitting = ref(false)

const comments = ref<ResourceComment[]>([])
const loadingComments = ref(false)
const submittingComment = ref(false)
const commentContent = ref('')
const replyingTo = ref<ResourceComment | null>(null)

async function fetchDetail(id: number) {
  loading.value = true
  try {
    resource.value = await resourceApi.detail(id)
  } catch {
    ElMessage.error('资源详情加载失败，请稍后重试')
    resource.value = null
  } finally {
    loading.value = false
  }
}

async function fetchComments(resourceId: number) {
  loadingComments.value = true
  try {
    const page = await resourceCommentApi.page(resourceId, 1, 20)
    comments.value = page.records
  } catch {
    comments.value = []
    ElMessage.error('评论加载失败，请稍后重试')
  } finally {
    loadingComments.value = false
  }
}

function onDownload() {
  if (!resource.value?.fileUrl) return
  window.open(resource.value.fileUrl, '_blank', 'noopener,noreferrer')
}

async function submitRating() {
  if (!resource.value || !score.value) return
  submitting.value = true
  try {
    await ratingApi.rate({
      resourceId: resource.value.id,
      score: score.value,
    })
    score.value = 0
    await fetchDetail(resource.value.id)
    ElMessage.success('评分提交成功')
  } catch (e) {
    if (e instanceof ApiError) {
      ElMessage.error(e.message || '评分提交失败，请稍后重试')
    } else {
      ElMessage.error('评分提交失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

async function submitComment() {
  if (!resource.value || !commentContent.value.trim()) return
  submittingComment.value = true
  try {
    await resourceCommentApi.create({
      resourceId: resource.value.id,
      content: commentContent.value.trim(),
      rootId: replyingTo.value?.rootId || replyingTo.value?.id || null,
      parentId: replyingTo.value?.id || null,
      toUserId: replyingTo.value?.fromUserId || null,
    })
    commentContent.value = ''
    replyingTo.value = null
    await fetchComments(resource.value.id)
    ElMessage.success('评论发表成功')
  } catch (e) {
    if (e instanceof ApiError) {
      ElMessage.error(e.message || '评论发表失败，请稍后重试')
    } else {
      ElMessage.error('评论发表失败，请稍后重试')
    }
  } finally {
    submittingComment.value = false
  }
}

function handleReply(comment: ResourceComment) {
  replyingTo.value = comment
  commentContent.value = `@${comment.fromUserName} `
}

function cancelReply() {
  replyingTo.value = null
  commentContent.value = ''
}

async function handleCommentRefresh() {
  if (!resource.value) return
  await fetchComments(resource.value.id)
}

watch(
  () => Number(route.params.id),
  async (id) => {
    if (!id) {
      resource.value = null
      comments.value = []
      return
    }
    score.value = 0
    commentContent.value = ''
    replyingTo.value = null
    await Promise.all([fetchDetail(id), fetchComments(id)])
  },
  { immediate: true },
)
</script>

<template>
  <div class="detail">
    <el-skeleton v-if="loading" animated :rows="6" />
    <template v-else>
      <el-card v-if="resource" class="main-card" shadow="never">
        <div class="header">
          <div>
            <div class="title">{{ resource.title }}</div>
            <div class="meta">
              <span class="meta-text">{{ resource.categoryName }}</span>
              <span class="meta-dot">·</span>
              <span class="meta-text">{{ resource.uploaderName }}</span>
              <span class="meta-dot">·</span>
              <span class="meta-text">
                {{ new Date(resource.createTime).toLocaleString() }}
              </span>
            </div>
          </div>
          <div class="download">
            <el-button type="primary" round @click="onDownload">下载资源</el-button>
          </div>
        </div>

        <div v-if="resource.description" class="desc">
          {{ resource.description }}
        </div>
        <div v-if="resource.tags" class="tags">
          <span v-for="tag in resource.tags.split(',')" :key="tag" class="tag">
            {{ tag }}
          </span>
        </div>

        <div class="rating-summary">
          <div class="score">
            <div class="score-main">
              <span class="score-num">
                {{ resource.scoreAvg?.toFixed?.(1) ?? '-' }}
              </span>
              <span class="score-unit">/ 5</span>
            </div>
            <div class="score-sub">
              {{ resource.scoreCount }} 人评分 · {{ resource.downloadCount }} 次下载
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="rate-card" shadow="never">
        <div class="rate-title">为资源打个分吧</div>
        <el-form @submit.prevent>
          <el-form-item label="评分">
            <el-rate v-model="score" :max="5" allow-half />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submitRating">
              提交评分
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="comment-card" shadow="never">
        <div class="comment-title">资源评论</div>
        <el-form @submit.prevent>
          <el-form-item v-if="replyingTo">
            <div class="reply-hint">
              正在回复 <span class="reply-user">@{{ replyingTo.fromUserName }}</span>
              <el-button text type="primary" size="small" @click="cancelReply">取消</el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              :placeholder="replyingTo ? `回复 ${replyingTo.fromUserName}...` : '写下你对资源的看法... '"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submittingComment" @click="submitComment">
              {{ replyingTo ? '回复' : '发表评论' }}
            </el-button>
            <el-button v-if="replyingTo" @click="cancelReply">取消</el-button>
          </el-form-item>
        </el-form>

        <el-skeleton v-if="loadingComments" animated :rows="5" />
        <template v-else>
          <ResourceCommentList
            v-if="comments.length"
            :comments="comments"
            :resource-id="resource?.id || 0"
            @refresh="handleCommentRefresh"
            @reply="handleReply"
          />
          <div v-else class="empty-text">还没有评论，来发表第一条评论吧～</div>
        </template>
      </el-card>
    </template>
  </div>
</template>

<style scoped>
.detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.main-card {
  border-radius: var(--ccp-card-radius);
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.title {
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 4px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

.meta-text {
  color: var(--ccp-text-muted);
}

.meta-dot {
  color: #9ca3af;
}

.download {
  display: flex;
  align-items: center;
}

.desc {
  margin-top: 10px;
  font-size: 14px;
  color: #111827;
}

.tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #f3f4ff;
  color: #4f46e5;
}

.rating-summary {
  margin-top: 14px;
}

.score-main {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-num {
  font-size: 24px;
  font-weight: 800;
}

.score-unit {
  font-size: 13px;
  color: #6b7280;
}

.score-sub {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

.rate-card,
.comment-card {
  border-radius: var(--ccp-card-radius);
}

.rate-title,
.comment-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 10px;
}

.reply-hint {
  padding: 8px 12px;
  background: #f3f4f6;
  border-radius: 4px;
  font-size: 12px;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.reply-user {
  font-weight: 600;
  color: var(--ccp-primary);
}

.empty-text {
  padding: 18px 0;
  font-size: var(--ccp-sub-size);
  color: var(--ccp-text-light);
  text-align: center;
}

@media (max-width: 640px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

