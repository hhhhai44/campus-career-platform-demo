<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { postApi, type PostListItem } from '@/api/post'
import { resourceApi, type ResourceListItem } from '@/api/resource'
import { useAuthStore } from '@/stores/auth'
import { buildTimeRangeQuery, type TimeRangeKey } from '@/utils/timeRange'

const router = useRouter()
const auth = useAuthStore()

const posts = ref<PostListItem[]>([])
const resources = ref<ResourceListItem[]>([])
const todayPostTotal = ref(0)
const todayResourceTotal = ref(0)
const loadingPosts = ref(false)
const loadingResources = ref(false)
const loadingStats = ref(false)

function gotoList(name: 'forum-list' | 'resource-list', timeRange: TimeRangeKey) {
  router.push({ name, query: buildTimeRangeQuery(timeRange) })
}

async function fetchPosts() {
  loadingPosts.value = true
  try {
    const page = await postApi.page({ page: 1, size: 5 })
    posts.value = page.records
  } catch {
    posts.value = []
    ElMessage.error('帖子加载失败，稍后再试试')
  } finally {
    loadingPosts.value = false
  }
}

async function fetchResources() {
  loadingResources.value = true
  try {
    const page = await resourceApi.page({ page: 1, size: 6 })
    resources.value = page.records
  } catch {
    resources.value = []
    ElMessage.error('资源加载失败，稍后再试试')
  } finally {
    loadingResources.value = false
  }
}

async function fetchTodayStats() {
  loadingStats.value = true
  try {
    const [postPage, resourcePage] = await Promise.all([
      postApi.page({ page: 1, size: 1, timeRange: 'today' }),
      resourceApi.page({ page: 1, size: 1, timeRange: 'today' }),
    ])
    todayPostTotal.value = postPage.total
    todayResourceTotal.value = resourcePage.total
  } catch {
    todayPostTotal.value = 0
    todayResourceTotal.value = 0
  } finally {
    loadingStats.value = false
  }
}

function gotoPost(id: number) {
  router.push({ name: 'forum-detail', params: { id } })
}

function gotoAuthorMessage(e: MouseEvent, userId: number) {
  e.stopPropagation()
  if (auth.userId && auth.userId === userId) return
  const matched = posts.value.find((item) => item.authorId === userId)
  router.push({
    name: 'message-center',
    query: { peer: String(userId), peerName: matched?.authorName },
  })
}

function gotoUploaderMessage(e: MouseEvent, userId: number) {
  e.stopPropagation()
  if (auth.userId && auth.userId === userId) return
  const matched = resources.value.find((item) => item.uploaderId === userId)
  router.push({
    name: 'message-center',
    query: { peer: String(userId), peerName: matched?.uploaderName },
  })
}

function gotoResource(id: number) {
  router.push({ name: 'resource-detail', params: { id } })
}

onMounted(() => {
  void fetchPosts()
  void fetchResources()
  void fetchTodayStats()
})
</script>

<template>
  <div class="home">
    <div class="hero">
      <div class="hero-main">
        <div class="hero-title">把校园成长，变成看得见的进步</div>
        <div class="hero-sub">分享经验、交换资料、获得反馈，和同路人一起把职业路走得更清晰。</div>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="$router.push('/forum')">
            去逛论坛
          </el-button>
          <el-button size="large" round @click="$router.push('/resource')">找找资源</el-button>
        </div>
      </div>
      <div class="hero-side">
        <el-card class="hero-card" shadow="hover">
          <div class="hero-stat-title">今日校园动态</div>
          <div class="hero-stat-grid">
            <button class="stat-item" type="button" :disabled="loadingStats" @click="gotoList('forum-list', 'today')">
              <span class="stat-value">{{ todayPostTotal }}</span>
              <span class="stat-label">今日新帖子</span>
            </button>
            <button class="stat-item" type="button" :disabled="loadingStats" @click="gotoList('resource-list', 'today')">
              <span class="stat-value">{{ todayResourceTotal }}</span>
              <span class="stat-label">今日新资源</span>
            </button>
          </div>
        </el-card>
      </div>
    </div>

    <div class="columns">
      <section class="col-main">
        <div class="section-header">
          <div class="section-title">最新帖子</div>
          <el-button text type="primary" @click="$router.push('/forum')">查看全部</el-button>
        </div>
        <el-skeleton v-if="loadingPosts" animated :rows="4" />
        <div v-else class="card-list">
          <el-card
            v-for="item in posts"
            :key="item.id"
            class="post-card"
            shadow="hover"
            @click="gotoPost(item.id)"
          >
            <div class="post-title">{{ item.title }}</div>
            <div class="post-meta">
              <span class="tag">{{ item.categoryName }}</span>
              <button
                v-if="!auth.userId || auth.userId !== item.authorId"
                type="button"
                class="author-link"
                @click="gotoAuthorMessage($event, item.authorId)"
              >
                {{ item.authorName }}
              </button>
              <span v-else class="author-link author-text">{{ item.authorName }}</span>
              <span class="meta-dot">·</span>
              <span class="meta-text">
                {{ new Date(item.createTime).toLocaleString() }}
              </span>
            </div>
            <div v-if="item.summary" class="post-summary">
              {{ item.summary }}
            </div>
            <div class="post-stats">
              <span> {{ item.likeCount }}</span>
              <span>⭐ {{ item.favoriteCount }}</span>
              <span> {{ item.commentCount }}</span>
              <span> {{ item.viewCount }}</span>
            </div>
          </el-card>
          <div v-if="!posts.length && !loadingPosts" class="empty-text">
            这里还空空的，发布第一个帖子，开启讨论吧！
          </div>
        </div>
      </section>

      <section class="col-side">
        <div class="section-header">
          <div class="section-title">精选资源</div>
        </div>
        <el-skeleton v-if="loadingResources" animated :rows="5" />
        <div v-else class="resource-list">
          <el-card
            v-for="res in resources"
            :key="res.id"
            class="resource-card"
            shadow="hover"
            @click="gotoResource(res.id)"
          >
            <div class="resource-title">{{ res.title }}</div>
            <div class="resource-meta">
              <span class="meta-text">{{ res.categoryName }}</span>
              <span class="meta-dot">·</span>
              <button
                v-if="!auth.userId || auth.userId !== res.uploaderId"
                type="button"
                class="author-link"
                @click="gotoUploaderMessage($event, res.uploaderId)"
              >
                {{ res.uploaderName }}
              </button>
              <span v-else class="author-link author-text">{{ res.uploaderName }}</span>
            </div>
            <div v-if="res.contentPreview || res.description" class="resource-desc">
              {{ res.contentPreview || res.description }}
            </div>
            <div class="resource-stats">
              <span>⭐ {{ res.scoreAvg?.toFixed?.(1) ?? '-' }} ({{ res.scoreCount }})</span>
              <span> {{ res.likeCount }}</span>
              <span>收藏 {{ res.favoriteCount }}</span>
              <span> {{ res.commentCount }}</span>
            </div>
          </el-card>
          <div v-if="!resources.length && !loadingResources" class="empty-text">
            资源区还在等第一份分享，上传一份你觉得有用的资料吧。
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 2.2fr) minmax(0, 1.3fr);
  gap: 16px;
}

.hero-main {
  padding: 22px 22px 24px;
  border-radius: var(--ccp-card-radius);
  background: radial-gradient(circle at 0 0, #eef2ff 0, #eff6ff 40%, #f9fafb 100%);
  box-shadow: var(--ccp-card-shadow);
}

.hero-title {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0.03em;
  margin-bottom: 8px;
}

.hero-sub {
  font-size: 14px;
  color: var(--ccp-text-secondary);
  max-width: 480px;
}

.hero-actions {
  margin-top: 18px;
  display: flex;
  gap: 10px;
}

.hero-side {
  display: flex;
  align-items: stretch;
}

.hero-card {
  width: 100%;
  border-radius: var(--ccp-card-radius);
}

.hero-stat-title {
  font-size: 14px;
  color: var(--ccp-text-muted);
  margin-bottom: 14px;
}

.hero-stat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.stat-item {
  border: 0;
  text-align: left;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f9fafb;
  cursor: pointer;
  transition: all 0.2s ease;
}

.stat-item:hover:not(:disabled) {
  transform: translateY(-1px);
  background: #eef4ff;
}

.stat-item:disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: var(--ccp-text-muted);
}

.columns {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1.2fr);
  gap: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.section-title {
  font-size: var(--ccp-title-size);
  font-weight: var(--ccp-title-weight);
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.post-card {
  border-radius: var(--ccp-card-radius);
  cursor: pointer;
}

.post-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  font-size: 12px;
  color: var(--ccp-text-muted);
  margin-bottom: 6px;
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

.author-link {
  border: 0;
  background: transparent;
  color: var(--ccp-primary);
  font-size: 12px;
  padding: 0;
  cursor: pointer;
}

.author-text {
  cursor: default;
}

.author-link:hover {
  text-decoration: underline;
}

.author-text:hover {
  text-decoration: none;
}

.meta-dot {
  color: var(--ccp-text-light);
}

.post-stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: var(--ccp-text-light);
}

.post-summary {
  margin-bottom: 6px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resource-card {
  border-radius: var(--ccp-card-radius);
  cursor: pointer;
}

.resource-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.resource-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 12px;
  color: var(--ccp-text-muted);
  margin-bottom: 4px;
}

.resource-desc {
  font-size: 13px;
  color: #4b5563;
  margin-bottom: 4px;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.resource-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--ccp-text-light);
}

.empty-text {
  padding: 24px 0;
  text-align: center;
  font-size: var(--ccp-sub-size);
  color: var(--ccp-text-light);
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: minmax(0, 1fr);
  }

  .columns {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
