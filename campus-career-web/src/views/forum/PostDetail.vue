<script setup lang="ts">
import { computed, defineAsyncComponent, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { postApi, type PostDetail } from '@/api/post'
import { commentApi, type PostComment } from '@/api/comment'
import { interactionApi } from '@/api/interaction'
import { reportApi, type ReportBizType } from '@/api/report'

const CommentList = defineAsyncComponent(() => import('@/components/CommentList.vue'))

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const isMyPost = computed(() => {
  if (!post.value || !auth.isAuthed) {
    return false
  }
  return post.value.currentUserId != null
    && post.value.authorId === post.value.currentUserId
})

const post = ref<PostDetail | null>(null)
const comments = ref<PostComment[]>([])
const loadingPost = ref(false)
const loadingComments = ref(false)
const submittingComment = ref(false)
const commentContent = ref('')
const likeLoading = ref(false)
const favoriteLoading = ref(false)
const replyingTo = ref<PostComment | null>(null)
const deleteLoading = ref(false)
const reportVisible = ref(false)
const reportSubmitting = ref(false)
const reportTitle = ref('')
const reportForm = reactive({
  bizType: 'POST' as ReportBizType,
  bizId: 0,
  reason: '',
  detail: '',
})
let postRequestSeq = 0
let commentRequestSeq = 0

async function fetchPost(id: number) {
  const requestSeq = ++postRequestSeq
  loadingPost.value = true
  try {
    const detail = await postApi.detail(id)
    if (requestSeq !== postRequestSeq) return
    post.value = {
      ...detail,
      liked: !!detail.liked,
      favorited: !!detail.favorited,
    }
  } catch {
    if (requestSeq !== postRequestSeq) return
    ElMessage.error('帖子加载失败，稍后再试试')
    post.value = null
  } finally {
    if (requestSeq === postRequestSeq) {
      loadingPost.value = false
    }
  }
}

async function fetchComments(id: number) {
  const requestSeq = ++commentRequestSeq
  loadingComments.value = true
  try {
    const page = await commentApi.page(id, 1, 20)
    if (requestSeq !== commentRequestSeq) return
    comments.value = page.records
  } catch {
    if (requestSeq !== commentRequestSeq) return
    ElMessage.error('评论加载失败，稍后再试试')
    comments.value = []
  } finally {
    if (requestSeq === commentRequestSeq) {
      loadingComments.value = false
    }
  }
}

/** 点赞/取消点赞 Toggle，根据返回更新数量和状态 */
async function onLike() {
  if (!post.value || likeLoading.value) return
  likeLoading.value = true
  try {
    const res = await interactionApi.likeToggle(post.value.id)
    post.value.liked = res.liked
    post.value.likeCount = res.likeCount
  } catch {
    ElMessage.error('操作没成功，稍后再试试')
  } finally {
    likeLoading.value = false
  }
}

/** 收藏/取消收藏 Toggle */
async function onFavorite() {
  if (!post.value || favoriteLoading.value) return
  favoriteLoading.value = true
  try {
    const res = await interactionApi.favoriteToggle(post.value.id)
    post.value.favorited = res.favorited
    ElMessage.success(res.favorited ? '收藏成功' : '已取消收藏')
  } catch {
    ElMessage.error('操作没成功，稍后再试试')
  } finally {
    favoriteLoading.value = false
  }
}

async function submitComment() {
  if (!post.value || !commentContent.value.trim()) return
  submittingComment.value = true
  try {
    await commentApi.create({
      postId: post.value.id,
      content: commentContent.value.trim(),
      rootId: replyingTo.value?.rootId || replyingTo.value?.id || null,
      parentId: replyingTo.value?.id || null,
      toUserId: replyingTo.value?.fromUserId || null,
    })
    commentContent.value = ''
    replyingTo.value = null
    await Promise.all([fetchPost(post.value.id), fetchComments(post.value.id)])
  } catch {
    ElMessage.error('评论发送失败，稍后再试试')
  } finally {
    submittingComment.value = false
  }
}

function handleReply(comment: PostComment) {
  replyingTo.value = comment
  commentContent.value = `@${comment.fromUserName} `
}

function cancelReply() {
  replyingTo.value = null
  commentContent.value = ''
}

async function handleDeletePost() {
  if (!post.value) return
  try {
    await ElMessageBox.confirm('确定要删除吗？这个操作无法撤销。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    deleteLoading.value = true
    await postApi.delete(post.value.id)
    ElMessage.success('帖子已删除')
    // 返回上一页或首页
    window.history.back()
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err?.message || '删除失败')
    }
  } finally {
    deleteLoading.value = false
  }
}

async function handleCommentRefresh() {
  if (post.value) {
    await Promise.all([fetchPost(post.value.id), fetchComments(post.value.id)])
  }
}

function goMessageAuthor() {
  if (!post.value || !post.value.authorId || isMyPost.value) {
    return
  }
  router.push({
    name: 'message-center',
    query: { peer: String(post.value.authorId), peerName: post.value.authorName },
  })
}

function gotoUserMessage(userId?: number | null) {
  if (!userId) return
  if (post.value?.currentUserId && userId === post.value.currentUserId) return
  router.push({
    name: 'message-center',
    query: { peer: String(userId), peerName: post.value?.authorName },
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

function handleReportPost() {
  if (!post.value || isMyPost.value) {
    ElMessage.warning('不能举报自己发布的帖子')
    return
  }
  openReportDialog('POST', post.value.id, post.value.title)
}

function handleReportComment(comment: PostComment) {
  if (auth.userId && comment.fromUserId === auth.userId) {
    ElMessage.warning('不能举报自己的评论')
    return
  }
  openReportDialog('POST_COMMENT', comment.id, comment.content.slice(0, 24) || `评论#${comment.id}`)
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
      post.value = null
      comments.value = []
      return
    }
    commentContent.value = ''
    await Promise.all([fetchPost(id), fetchComments(id)])
  },
  { immediate: true },
)
</script>

<template>
  <div class="detail">
    <div class="header ccp-page-header">
      <div>
        <div class="title-page">帖子详情</div>
        <div class="sub-page">把经验、观点和回复都串在一起看，更像一次完整讨论</div>
      </div>
      <div class="header-badge">
        <span class="badge-dot"></span>
        <span>点赞和收藏都有即时反馈</span>
      </div>
    </div>

    <el-skeleton v-if="loadingPost" animated :rows="6" />
    <template v-else>
      <el-card v-if="post" class="post-card" shadow="never">
        <div class="title">{{ post.title }}</div>
        <div class="meta">
          <span class="tag">{{ post.categoryName }}</span>
          <button
            v-if="!post.currentUserId || post.currentUserId !== post.authorId"
            type="button"
            class="author-chip"
            @click="gotoUserMessage(post.authorId)"
          >
            <span class="author-avatar">{{ post.authorName?.slice(0, 1)?.toUpperCase() || 'U' }}</span>
            <span class="meta-text author-link">{{ post.authorName }}</span>
          </button>
          <span v-else class="author-chip author-chip-static">
            <span class="author-avatar">{{ post.authorName?.slice(0, 1)?.toUpperCase() || 'U' }}</span>
            <span class="meta-text">{{ post.authorName }}</span>
          </span>
          <span class="meta-dot">·</span>
          <span class="meta-text">
            {{ new Date(post.createTime).toLocaleString() }}
          </span>
        </div>
        <div class="content">
          <p style="white-space: pre-wrap">
            {{ post.content }}
          </p>
        </div>
        <div class="post-actions">
          <el-button
            :type="post.liked ? 'primary' : 'default'"
            round
            size="small"
            :loading="likeLoading"
            @click="onLike"
          >
            👍 {{ post.liked ? '已赞' : '点赞' }} {{ post.likeCount }}
          </el-button>
          <el-button
            :type="post.favorited ? 'warning' : 'default'"
            round
            size="small"
            :loading="favoriteLoading"
            @click="onFavorite"
          >
            ⭐ {{ post.favorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button v-if="auth.isAuthed && !isMyPost" text type="danger" size="small" @click="handleReportPost">举报</el-button>
          <span class="meta-text">浏览 {{ post.viewCount }} · 评论 {{ post.commentCount }}</span>
          <el-button
            v-if="isMyPost"
            text
            type="danger"
            size="small"
            :loading="deleteLoading"
            @click="handleDeletePost"
          >
            删除
          </el-button>
          <el-button
            v-else-if="post.authorId"
            text
            type="primary"
            size="small"
            @click="goMessageAuthor"
          >
            私信作者
          </el-button>
        </div>
      </el-card>
    </template>

    <el-card class="comment-card" shadow="never">
      <div class="comment-title">评论</div>
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
            :placeholder="replyingTo ? `回复 ${replyingTo.fromUserName}...` : '写点你的看法，让讨论更完整'"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submittingComment" @click="submitComment">
            {{ replyingTo ? '回复评论' : '发表评论' }}
          </el-button>
          <el-button v-if="replyingTo" @click="cancelReply">取消</el-button>
        </el-form-item>
      </el-form>

      <el-skeleton v-if="loadingComments" animated :rows="5" />
      <template v-else>
        <CommentList
          v-if="comments.length"
          :comments="comments"
          :post-id="post?.id || 0"
          @refresh="handleCommentRefresh"
          @reply="handleReply"
          @report="handleReportComment"
        />
        <div v-else class="empty-text">评论区还空着，来留下第一条观点吧！</div>
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

.post-card {
  border-radius: var(--ccp-card-radius);
  border: 1px solid var(--ccp-card-border);
  box-shadow: var(--ccp-card-shadow);
}

.title {
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 6px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 10px;
}

.tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: #eef2ff;
  color: var(--ccp-primary);
}

.meta-text {
  color: var(--ccp-text-muted);
}

.author-chip {
  border: 0;
  background: transparent;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  cursor: pointer;
}

.author-chip-static {
  cursor: default;
}

.author-avatar {
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

.author-link {
  color: var(--ccp-primary);
}

.author-chip:hover .author-link {
  text-decoration: underline;
}

.author-chip-static:hover .meta-text {
  text-decoration: none;
}

.meta-dot {
  color: #9ca3af;
}

.content {
  font-size: 14px;
  color: #111827;
  line-height: 1.7;
  margin-bottom: 10px;
}

.post-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.comment-card {
  border-radius: var(--ccp-card-radius);
  border: 1px solid var(--ccp-card-border);
  box-shadow: var(--ccp-card-shadow);
}

.comment-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 10px;
}

.empty-text {
  padding: 18px 0;
  font-size: var(--ccp-sub-size);
  color: var(--ccp-text-light);
  text-align: center;
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
</style>
