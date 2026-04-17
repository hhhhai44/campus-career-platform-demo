<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { postApi, type PostDetail, type PostListItem } from '@/api/post'

const loading = ref(false)
const saving = ref(false)
const records = ref<PostListItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentDetail = ref<PostDetail | null>(null)

const filters = reactive({
  keyword: '',
  status: '' as '' | '1' | '0',
  deleted: '' as '' | '0' | '1',
})

const summary = computed(() => ({
  total: total.value,
  normal: records.value.filter((item) => item.deleted !== 1 && item.status !== 0).length,
  disabled: records.value.filter((item) => item.deleted !== 1 && item.status === 0).length,
  deleted: records.value.filter((item) => item.deleted === 1).length,
}))

function statusTagType(item: PostListItem) {
  if (item.deleted === 1) return 'info'
  return item.status === 0 ? 'warning' : 'success'
}

function statusText(item: PostListItem) {
  if (item.deleted === 1) return '已软删'
  return item.status === 0 ? '已禁用' : '正常'
}

const statusFilterDisabled = computed(() => filters.deleted === '1')

function parseNullableInt(value: unknown) {
  if (value === '' || value === null || value === undefined) return null
  const n = Number(value)
  if (!Number.isFinite(n)) return null
  const intVal = Math.trunc(n)
  return Number.isFinite(intVal) ? intVal : null
}

watch(
  () => filters.deleted,
  (value) => {
    // 已删除数据不再参与“禁用/正常”筛选，避免条件互相冲突。
    if (value === '1') {
      filters.status = ''
    }
  },
)

function formatDateTime(value?: string | null) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

function buildParams() {
  const deletedValue = parseNullableInt(filters.deleted)
  const statusValue = parseNullableInt(filters.status)
  return {
    page: page.value,
    size: pageSize.value,
    keyword: filters.keyword.trim() || null,
    status: deletedValue === 1 ? null : statusValue,
    deleted: deletedValue,
  }
}

async function fetchPosts() {
  loading.value = true
  try {
    const result = await postApi.adminPage(buildParams())
    records.value = result.records ?? []
    total.value = result.total ?? 0
  } catch (error) {
    records.value = []
    total.value = 0
    ElMessage.error('管理员帖子列表加载失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  void fetchPosts()
}

function handleReset() {
  filters.keyword = ''
  filters.status = ''
  filters.deleted = ''
  page.value = 1
  void fetchPosts()
}

function handlePageChange(nextPage: number) {
  page.value = nextPage
  void fetchPosts()
}

function handleSizeChange(nextSize: number) {
  pageSize.value = nextSize
  page.value = 1
  void fetchPosts()
}

async function handleReview(item: PostListItem, status: number) {
  const text = status === 1 ? '通过' : '禁用'
  await ElMessageBox.confirm(`确认将帖子「${item.title}」${text}吗？`, '帖子审核', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  })
  saving.value = true
  try {
    await postApi.adminReview(item.id, { status })
    ElMessage.success('审核状态已更新')
    await fetchPosts()
  } catch (error) {
    ElMessage.error('审核失败，请稍后重试')
    console.error(error)
  } finally {
    saving.value = false
  }
}

async function handleDelete(item: PostListItem) {
  await ElMessageBox.confirm(`确认软删除帖子「${item.title}」吗？`, '软删除确认', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  })
  saving.value = true
  try {
    await postApi.adminDelete(item.id)
    ElMessage.success('帖子已软删除')
    await fetchPosts()
  } catch (error) {
    ElMessage.error('软删除失败，请稍后重试')
    console.error(error)
  } finally {
    saving.value = false
  }
}

async function handleRestore(item: PostListItem) {
  await ElMessageBox.confirm(`确认恢复帖子「${item.title}」吗？`, '恢复确认', {
    type: 'info',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  })
  saving.value = true
  try {
    await postApi.adminRestore(item.id)
    ElMessage.success('帖子已恢复')
    await fetchPosts()
  } catch (error) {
    ElMessage.error('恢复失败，请稍后重试')
    console.error(error)
  } finally {
    saving.value = false
  }
}

async function openDetail(item: PostListItem) {
  detailVisible.value = true
  detailLoading.value = true
  currentDetail.value = null
  try {
    currentDetail.value = await postApi.adminDetail(item.id)
  } catch {
    ElMessage.error('帖子详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  void fetchPosts()
})
</script>

<template>
  <div class="admin-page">
    <section class="hero-card">
      <div>
        <div class="eyebrow">后台管理</div>
        <h1 class="title">帖子审核中心</h1>
        <p class="subtitle">管理员可以在这里查看全量帖子、禁用违规内容、软删除或恢复帖子。</p>
      </div>
      <div class="stats">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ summary.total }}</div>
          <div class="stat-label">当前结果数</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ summary.normal }}</div>
          <div class="stat-label">正常</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ summary.disabled }}</div>
          <div class="stat-label">已禁用</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ summary.deleted }}</div>
          <div class="stat-label">已软删</div>
        </el-card>
      </div>
    </section>

    <el-card class="filter-card" shadow="never">
      <div class="filters">
        <el-input v-model="filters.keyword" clearable placeholder="按标题搜索" class="filter-input" />
        <el-select
          v-model="filters.status"
          clearable
          placeholder="帖子状态"
          class="filter-select"
          :disabled="statusFilterDisabled"
        >
          <el-option label="正常" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
        <el-select v-model="filters.deleted" clearable placeholder="删除状态" class="filter-select">
          <el-option label="未删除" value="0" />
          <el-option label="已删除" value="1" />
        </el-select>
        <div class="actions">
          <el-button type="primary" :loading="loading || saving" @click="handleSearch">搜索</el-button>
          <el-button :disabled="loading || saving" @click="handleReset">重置</el-button>
          <el-button :loading="loading || saving" @click="fetchPosts">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column label="标题" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button link type="primary" class="title-btn" @click="openDetail(row)">
              {{ row.title }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" min-width="140" />
        <el-table-column prop="categoryName" label="分类" min-width="120" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row)">{{ statusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞" width="90" />
        <el-table-column prop="viewCount" label="浏览" width="90" />
        <el-table-column prop="commentCount" label="评论" width="90" />
        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="260">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" :loading="saving" @click="openDetail(row)">查看</el-button>
              <el-button v-if="row.deleted !== 1 && row.status !== 1" link type="success" :loading="saving" @click="handleReview(row, 1)">通过</el-button>
              <el-button v-if="row.deleted !== 1 && row.status !== 0" link type="warning" :loading="saving" @click="handleReview(row, 0)">禁用</el-button>
              <el-button v-if="row.deleted !== 1" link type="danger" :loading="saving" @click="handleDelete(row)">软删除</el-button>
              <el-button v-else link type="primary" :loading="saving" @click="handleRestore(row)">恢复</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="帖子详情预览" width="780px" destroy-on-close>
      <el-skeleton v-if="detailLoading" animated :rows="8" />
      <template v-else>
        <div v-if="currentDetail" class="detail-panel">
          <div class="detail-title">{{ currentDetail.title }}</div>
          <div class="detail-meta">
            <span>作者：{{ currentDetail.authorName || `用户${currentDetail.authorId}` }}</span>
            <span>分类：{{ currentDetail.categoryName || '-' }}</span>
            <span>发布时间：{{ formatDateTime(currentDetail.createTime) }}</span>
          </div>
          <div class="detail-content">{{ currentDetail.content || '暂无内容' }}</div>
        </div>
        <el-empty v-else description="未获取到帖子详情" />
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
}

.hero-card,
.filter-card,
.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.08), rgba(24, 144, 255, 0.02));
}

.eyebrow {
  color: #1890ff;
  font-weight: 700;
  font-size: 13px;
  margin-bottom: 6px;
}

.title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #1f2937;
}

.subtitle {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(110px, 1fr));
  gap: 12px;
  min-width: 480px;
}

.stat-card {
  border-radius: 12px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: #1890ff;
}

.stat-label {
  margin-top: 4px;
  font-size: 13px;
  color: #64748b;
}

.filters {
  display: grid;
  grid-template-columns: 1.4fr 0.8fr 0.8fr auto;
  gap: 12px;
  align-items: center;
}

.actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.row-actions {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.title-btn {
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.detail-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.detail-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  color: #64748b;
  font-size: 13px;
}

.detail-content {
  max-height: 420px;
  overflow: auto;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #111827;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

@media (max-width: 960px) {
  .hero-card {
    flex-direction: column;
  }

  .stats {
    min-width: 0;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .actions {
    justify-content: stretch;
  }
}
</style>







