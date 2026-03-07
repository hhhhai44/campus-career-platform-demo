<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { postApi, type PostListItem, type PageResult } from '@/api/post'

const auth = useAuthStore()
const router = useRouter()

const posts = ref<PostListItem[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const displayUsername = computed(() => auth.username || '未登录用户')
const avatarText = computed(() => (auth.username?.slice(0, 1) || 'U').toUpperCase())

async function fetchMyPosts() {
  if (!auth.isAuthed) {
    ElMessage.warning('请先登录')
    return
  }
  loading.value = true
  try {
    const resp: PageResult<PostListItem> = await postApi.myPosts(page.value, size.value)
    posts.value = resp.records
    total.value = resp.total
  } catch {
    posts.value = []
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleDelete(postId: number) {
  try {
    await ElMessageBox.confirm('确定要删除这个帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await postApi.delete(postId)
    ElMessage.success('删除成功')
    await fetchMyPosts()
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err?.message || '删除失败')
    }
  }
}

function handlePageChange(p: number) {
  page.value = p
  fetchMyPosts()
}

function goToDetail(id: number) {
  router.push(`/forum/post/${id}`)
}

onMounted(() => {
  if (auth.isAuthed) {
    fetchMyPosts()
  }
})
</script>

<template>
  <div class="me">
    <div class="header">
      <div class="title">我的</div>
      <div class="sub">查看我发布的帖子</div>
    </div>

    <div class="user-card">
      <div class="avatar">
        <span>{{ avatarText }}</span>
      </div>
      <div class="info">
        <div class="name">当前登录用户</div>
        <div class="meta">用户名: {{ displayUsername }}</div>
      </div>
    </div>

    <el-card class="posts-card" shadow="never">
      <div class="posts-header">
        <div class="posts-title">我的帖子</div>
      </div>

      <el-skeleton v-if="loading" animated :rows="6" />

      <div v-else class="posts-list">
        <div v-for="item in posts" :key="item.id" class="post-item">
          <div class="post-main" @click="goToDetail(item.id)">
            <div class="post-title">{{ item.title }}</div>
            <div class="post-summary">{{ item.summary || item.title }}</div>
            <div class="post-meta">
              <span class="tag">{{ item.categoryName }}</span>
              <span class="meta-text">
                {{ new Date(item.createTime).toLocaleString() }}
              </span>
              <span class="meta-sep">·</span>
              <span class="meta-text">浏览 {{ item.viewCount }}</span>
              <span class="meta-sep">·</span>
              <span class="meta-text">评论 {{ item.commentCount }}</span>
              <span class="meta-sep">·</span>
              <span class="meta-text">点赞 {{ item.likeCount }}</span>
            </div>
          </div>
          <div class="post-actions">
            <el-button
              link
              type="danger"
              size="small"
              @click.stop="handleDelete(item.id)"
            >
              删除
            </el-button>
          </div>
        </div>
        <div v-if="!posts.length" class="empty-text">还没有发布过帖子</div>
      </div>

      <div v-if="total > size" class="pager">
        <el-pagination
          :current-page="page"
          :page-size="size"
          layout="prev, pager, next"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.me {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.header {
  margin-bottom: 4px;
}

.title {
  font-size: var(--ccp-title-size);
  font-weight: var(--ccp-title-weight);
}

.sub {
  margin-top: 2px;
  font-size: var(--ccp-sub-size);
  color: var(--ccp-sub-color);
}

.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 14px;
  background: #f9fafb;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  background: #111827;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
}

.info .name {
  font-size: 14px;
  font-weight: 600;
}

.info .meta {
  font-size: 12px;
  color: var(--ccp-text-light);
}

.posts-card {
  border-radius: var(--ccp-card-radius);
}

.posts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.posts-title {
  font-size: 16px;
  font-weight: 700;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background-color 0.2s;
}

.post-item:hover {
  background-color: #f9fafb;
  border-radius: 4px;
  padding-left: 8px;
  padding-right: 8px;
}

.post-main {
  flex: 1;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 6px;
}

.post-summary {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
  line-height: 1.5;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  flex-wrap: wrap;
}

.tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: #eef2ff;
  color: var(--ccp-primary);
  font-size: 11px;
}

.meta-text {
  color: #9ca3af;
}

.meta-sep {
  color: #d1d5db;
}

.post-actions {
  display: flex;
  align-items: center;
}

.empty-text {
  padding: 18px 0;
  text-align: center;
  font-size: var(--ccp-sub-size);
  color: var(--ccp-text-light);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>
