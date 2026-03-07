<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { postApi, type PostListItem } from '@/api/post'
import { resourceApi, type ResourceListItem } from '@/api/resource'

const router = useRouter()

const posts = ref<PostListItem[]>([])
const resources = ref<ResourceListItem[]>([])
const loadingPosts = ref(false)
const loadingResources = ref(false)

async function fetchPosts() {
  loadingPosts.value = true
  try {
    const page = await postApi.page({ page: 1, size: 5 })
    posts.value = page.records
  } catch {
    posts.value = []
    ElMessage.error('帖子加载失败，请稍后重试')
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
    ElMessage.error('资源加载失败，请稍后重试')
  } finally {
    loadingResources.value = false
  }
}

function gotoPost(id: number) {
  router.push({ name: 'forum-detail', params: { id } })
}

function gotoResource(id: number) {
  router.push({ name: 'resource-detail', params: { id } })
}

onMounted(() => {
  fetchPosts()
  fetchResources()
})
</script>

<template>
  <div class="home">
    <div class="hero">
      <div class="hero-main">
        <div class="hero-title">发现同校同路人的成长故事</div>
        <div class="hero-sub">在这里分享实习经验、课程资源与职涯心得，一起走得更远。</div>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="$router.push('/forum')">
            去逛论坛
          </el-button>
          <el-button size="large" round @click="$router.push('/resource')">浏览资源库</el-button>
        </div>
      </div>
      <div class="hero-side">
        <el-card class="hero-card" shadow="hover">
          <div class="hero-stat-title">今日校园动态</div>
          <div class="hero-stat-grid">
            <div class="stat-item">
              <div class="stat-value">{{ posts.length }}</div>
              <div class="stat-label">新帖子</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ resources.length }}</div>
              <div class="stat-label">新资源</div>
            </div>
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
              <span class="meta-text">{{ item.authorName }}</span>
              <span class="meta-dot">·</span>
              <span class="meta-text">
                {{ new Date(item.createTime).toLocaleString() }}
              </span>
            </div>
            <div v-if="item.summary" class="post-summary">
              {{ item.summary }}
            </div>
            <div class="post-stats">
              <span>👍 {{ item.likeCount }}</span>
              <span>⭐ {{ item.favoriteCount }}</span>
              <span>💬 {{ item.commentCount }}</span>
              <span>👀 {{ item.viewCount }}</span>
            </div>
          </el-card>
          <div v-if="!posts.length && !loadingPosts" class="empty-text">还没有帖子，去论坛发布一条吧～</div>
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
              <span class="meta-text">{{ res.uploaderName }}</span>
            </div>
            <div v-if="res.description" class="resource-desc">
              {{ res.description }}
            </div>
            <div class="resource-stats">
              <span>⭐ {{ res.scoreAvg?.toFixed?.(1) ?? '-' }} ({{ res.scoreCount }})</span>
              <span>收藏 {{ res.favoriteCount }}</span>
              <span>💬 {{ res.commentCount }}</span>
              <span>⬇️ {{ res.downloadCount }}</span>
            </div>
          </el-card>
          <div v-if="!resources.length && !loadingResources" class="empty-text">暂时没有资源</div>
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
  padding: 10px 12px;
  border-radius: 12px;
  background: #f9fafb;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
}

.stat-label {
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
