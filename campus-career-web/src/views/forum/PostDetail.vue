<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { postApi, type PostDetail } from '@/api/post'
import { commentApi, type PostComment } from '@/api/comment'
import { interactionApi } from '@/api/interaction'
import CommentList from '@/components/CommentList.vue'

const route = useRoute()

const post = ref<PostDetail | null>(null)
const comments = ref<PostComment[]>([])
const loadingPost = ref(false)
const loadingComments = ref(false)
const submittingComment = ref(false)
const commentContent = ref('')
const likeLoading = ref(false)
const favoriteLoading = ref(false)

async function fetchPost(id: number) {
  loadingPost.value = true
  try {
    const detail = await postApi.detail(id)
    post.value = {
      ...detail,
      liked: !!detail.liked,
      favorited: !!detail.favorited,
    }
  } catch {
    ElMessage.error('帖子加载失败，请稍后重试')
    post.value = null
  } finally {
    loadingPost.value = false
  }
}

async function fetchComments(id: number) {
  loadingComments.value = true
  try {
    const page = await commentApi.page(id, 1, 20)
    comments.value = page.records
  } catch {
    ElMessage.error('评论加载失败，请稍后重试')
    comments.value = []
  } finally {
    loadingComments.value = false
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
    ElMessage.error('操作失败，请稍后重试')
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
    ElMessage.success(res.favorited ? '已收藏' : '已取消收藏')
  } catch {
    ElMessage.error('操作失败，请稍后重试')
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
    })
    commentContent.value = ''
    await fetchComments(post.value.id)
  } catch {
    ElMessage.error('评论发表失败，请稍后重试')
  } finally {
    submittingComment.value = false
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
    <el-skeleton v-if="loadingPost" animated :rows="6" />
    <template v-else>
      <el-card v-if="post" class="post-card" shadow="never">
        <div class="title">{{ post.title }}</div>
        <div class="meta">
          <span class="tag">{{ post.categoryName }}</span>
          <span class="meta-text">{{ post.authorName }}</span>
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
          <span class="meta-text">浏览 {{ post.viewCount }} · 评论 {{ post.commentCount }}</span>
        </div>
      </el-card>
    </template>

    <el-card class="comment-card" shadow="never">
      <div class="comment-title">评论</div>
      <el-form @submit.prevent>
        <el-form-item>
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="写下你的想法..."
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submittingComment" @click="submitComment">
            发表
          </el-button>
        </el-form-item>
      </el-form>

      <el-skeleton v-if="loadingComments" animated :rows="5" />
      <template v-else>
        <CommentList v-if="comments.length" :comments="comments" />
        <div v-else class="empty-text">还没有评论，来占个沙发吧～</div>
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.post-card {
  border-radius: var(--ccp-card-radius);
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
}

.comment-card {
  border-radius: var(--ccp-card-radius);
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
</style>
