<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, defineAsyncComponent, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ApiError } from '@/api/http'
import { resourceApi, type ResourceDetail } from '@/api/resource'
import { ratingApi } from '@/api/rating'
import { resourceCommentApi, type ResourceComment } from '@/api/resourceComment'
import { reportApi, type ReportBizType } from '@/api/report'
import { useAuthStore } from '@/stores/auth'

const ResourceCommentList = defineAsyncComponent(() => import('@/components/ResourceCommentList.vue'))

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const resource = ref<ResourceDetail | null>(null)
const loading = ref(false)

const score = ref(0)
const submitting = ref(false)

const comments = ref<ResourceComment[]>([])
const loadingComments = ref(false)
const submittingComment = ref(false)
const commentContent = ref('')
const replyingTo = ref<ResourceComment | null>(null)

const likeLoading = ref(false)
const favoriteLoading = ref(false)
const reportVisible = ref(false)
const reportSubmitting = ref(false)
const reportTitle = ref('')
const reportForm = reactive({
  bizType: 'RESOURCE' as ReportBizType,
  bizId: 0,
  reason: '',
  detail: '',
})
let detailRequestSeq = 0
let commentRequestSeq = 0

async function fetchDetail(id: number) {
  const requestSeq = ++detailRequestSeq
  loading.value = true
  try {
    const detail = await resourceApi.detail(id)
    if (requestSeq !== detailRequestSeq) return
    resource.value = {
      ...detail,
      liked: !!detail.liked,
      favorited: !!detail.favorited,
    }
  } catch {
    if (requestSeq !== detailRequestSeq) return
    ElMessage.error('资源详情加载失败，稍后再试试')
    resource.value = null
  } finally {
    if (requestSeq === detailRequestSeq) {
      loading.value = false
    }
  }
}

async function fetchComments(resourceId: number) {
  const requestSeq = ++commentRequestSeq
  loadingComments.value = true
  try {
    const page = await resourceCommentApi.page(resourceId, 1, 20)
    if (requestSeq !== commentRequestSeq) return
    comments.value = page.records
  } catch {
    if (requestSeq !== commentRequestSeq) return
    comments.value = []
    ElMessage.error('评论加载失败，稍后再试试')
  } finally {
    if (requestSeq === commentRequestSeq) {
      loadingComments.value = false
    }
  }
}

async function onDownload() {
  if (!resource.value) return
  try {
    const url = await resourceApi.download(resource.value.id)
    resource.value.downloadCount = (resource.value.downloadCount || 0) + 1
    window.open(url, '_blank', 'noopener,noreferrer')
  } catch {
    ElMessage.error('下载失败，稍后再试试')
  }
}

async function onLike() {
  if (!resource.value || likeLoading.value) return
  likeLoading.value = true
  try {
    const resp = await resourceApi.likeToggle(resource.value.id)
    resource.value.liked = resp.liked
    resource.value.likeCount = resp.likeCount
  } catch {
    ElMessage.error('操作没成功，稍后再试试')
  } finally {
    likeLoading.value = false
  }
}

async function onFavorite() {
  if (!resource.value || favoriteLoading.value) return
  favoriteLoading.value = true
  try {
    const resp = await resourceApi.favoriteToggle(resource.value.id)
    resource.value.favorited = resp.favorited
    resource.value.favoriteCount = Math.max(
      0,
      (resource.value.favoriteCount || 0) + (resp.favorited ? 1 : -1),
    )
  } catch {
    ElMessage.error('操作没成功，稍后再试试')
  } finally {
    favoriteLoading.value = false
  }
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
    ElMessage.success('评分已提交，感谢你的反馈')
  } catch (e) {
    if (e instanceof ApiError) {
      ElMessage.error(e.message || '评分提交失败，稍后再试试')
    } else {
      ElMessage.error('评分提交失败，稍后再试试')
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
    ElMessage.success('评论已发布')
  } catch (e) {
    if (e instanceof ApiError) {
      ElMessage.error(e.message || '评论发布失败，稍后再试试')
    } else {
      ElMessage.error('评论发布失败，稍后再试试')
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

function gotoUserMessage(userId?: number | null) {
  if (!userId || (auth.userId && auth.userId === userId)) return
  router.push({
    name: 'message-center',
    query: { peer: String(userId), peerName: resource.value?.uploaderName },
  })
}

function openReportDialog(bizType: ReportBizType, bizId: number, title: string) {
  reportForm.bizType = bizType
  reportForm.bizId = bizId
  reportForm.reason = ''
  reportForm.detail = ''
  reportTitle.value = title
  reportVisible.value = true
}

function handleReportResource() {
  if (!resource.value) return
  if (resource.value.owner) {
    ElMessage.warning('不能举报自己上传的资源')
    return
  }
  openReportDialog('RESOURCE', resource.value.id, resource.value.title)
}

function handleReportComment(comment: ResourceComment) {
  if (auth.userId && comment.fromUserId === auth.userId) {
    ElMessage.warning('不能举报自己的评论')
    return
  }
  openReportDialog('RESOURCE_COMMENT', comment.id, comment.content.slice(0, 24) || `评论#${comment.id}`)
}

async function submitReport() {
  if (!reportForm.reason.trim() || reportSubmitting.value) return
  reportSubmitting.value = true
  try {
    await reportApi.create({
      bizType: reportForm.bizType,
      bizId: reportForm.bizId,
      reason: reportForm.reason.trim(),
      detail: reportForm.detail.trim() || null,
    })
    ElMessage.success('举报已提交')
    reportVisible.value = false
  } catch (error) {
    ElMessage.error('举报提交失败，请稍后重试')
    console.error(error)
  } finally {
    reportSubmitting.value = false
  }
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

const showRatingCard = computed(() => {
  if (!resource.value) return false
  return !resource.value.owner && !resource.value.hasRated
})
</script>

<template>
  <div class="detail">
    <div class="header ccp-page-header">
      <div>
        <div class="title-page">资源详情</div>
        <div class="sub-page">看看资源内容、评分和评论，再决定要不要下载</div>
      </div>
      <div class="header-badge">
        <span class="badge-dot"></span>
        <span>评分、收藏、下载都能即时更新</span>
      </div>
    </div>

    <el-skeleton v-if="loading" animated :rows="6" />
    <template v-else>
      <el-card v-if="resource" class="main-card" shadow="never">
        <div class="header">
          <div>
            <div class="title">{{ resource.title }}</div>
            <div class="meta">
              <span class="meta-text">{{ resource.categoryName }}</span>
              <span class="meta-dot">·</span>
              <button
                v-if="!resource.owner"
                type="button"
                class="uploader-chip"
                @click="gotoUserMessage(resource.uploaderId)"
              >
                <span class="uploader-avatar">{{ resource.uploaderName?.slice(0, 1)?.toUpperCase() || 'U' }}</span>
                <span class="meta-text uploader-link">{{ resource.uploaderName }}</span>
              </button>
              <span v-else class="uploader-chip uploader-chip-static">
                <span class="uploader-avatar">{{ resource.uploaderName?.slice(0, 1)?.toUpperCase() || 'U' }}</span>
                <span class="meta-text">{{ resource.uploaderName }}</span>
              </span>
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

        <div class="actions">
          <el-button
            :type="resource.liked ? 'primary' : 'default'"
            round
            size="small"
            :loading="likeLoading"
            @click="onLike"
          >
            👍 {{ resource.liked ? '已赞' : '点赞' }} {{ resource.likeCount || 0 }}
          </el-button>
          <el-button
            :type="resource.favorited ? 'warning' : 'default'"
            round
            size="small"
            :loading="favoriteLoading"
            @click="onFavorite"
          >
            ⭐ {{ resource.favorited ? '已收藏' : '收藏' }} {{ resource.favoriteCount || 0 }}
          </el-button>
          <el-button v-if="auth.isAuthed && !resource.owner" text type="danger" size="small" @click="handleReportResource">举报</el-button>
        </div>
      </el-card>

      <el-card v-if="showRatingCard" class="rate-card" shadow="never">
        <div class="rate-title">觉得这个资源怎么样？打个分吧</div>
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
        <div class="comment-title">资源评论区</div>
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
              :placeholder="replyingTo ? `回复 ${replyingTo.fromUserName}...` : '写下你的使用感受，帮到更多同学'"
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
            @report="handleReportComment"
          />
          <div v-else class="empty-text">评论区还空着，来发表第一条评论吧！</div>
        </template>
      </el-card>

      <el-dialog v-model="reportVisible" title="举报内容" width="520px" destroy-on-close>
        <el-form label-width="90px">
          <el-form-item label="举报对象">
            <div class="report-target">{{ reportTitle }}</div>
          </el-form-item>
          <el-form-item label="举报原因">
            <el-input v-model="reportForm.reason" placeholder="例如：广告、辱骂、违规内容" />
          </el-form-item>
          <el-form-item label="补充说明">
            <el-input v-model="reportForm.detail" type="textarea" :rows="4" placeholder="可补充说明具体问题" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reportVisible = false">取消</el-button>
          <el-button type="primary" :loading="reportSubmitting" @click="submitReport">提交举报</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<style scoped>
.detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.header {
  margin-bottom: 4px;
}

.title-page {
  font-size: 24px;
  font-weight: 700;
  color: var(--ccp-text);
}

.sub-page {
  margin-top: 4px;
  color: var(--ccp-sub-color);
  font-size: var(--ccp-sub-size);
  line-height: 1.6;
}

.header-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(74, 111, 255, 0.08);
  color: var(--ccp-primary);
  font-size: 12px;
  white-space: nowrap;
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--ccp-primary);
  box-shadow: 0 0 0 4px rgba(74, 111, 255, 0.12);
}

.main-card {
  border-radius: var(--ccp-card-radius);
  border: 1px solid var(--ccp-card-border);
  box-shadow: var(--ccp-card-shadow);
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

.uploader-chip {
  border: 0;
  background: transparent;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  cursor: pointer;
}

.uploader-chip-static {
  cursor: default;
}

.uploader-avatar {
  width: 22px;
  height: 22px;
  border-radius: 999px;
  background: var(--ccp-primary-gradient);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
}

.uploader-link {
  color: var(--ccp-primary);
}

.uploader-chip:hover .uploader-link {
  text-decoration: underline;
}

.uploader-chip-static:hover .meta-text {
  text-decoration: none;
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
  border: 1px solid var(--ccp-card-border);
  box-shadow: var(--ccp-card-shadow);
}

.rate-title,
.comment-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 10px;
}

.actions {
  margin-top: 10px;
  display: flex;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.2s;
}

.action-item:hover {
  background: #f5f7ff;
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

.report-target {
  color: #1f2937;
  font-size: 14px;
  line-height: 1.6;
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

