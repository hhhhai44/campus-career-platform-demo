<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reportApi, type ReportDetail, type ReportListItem } from '@/api/report'

const loading = ref(false)
const saving = ref(false)
const records = ref<ReportListItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const fetchSeq = ref(0)
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentDetail = ref<ReportDetail | null>(null)

const filters = reactive({
  keyword: '',
  bizType: '' as '' | 'POST' | 'RESOURCE' | 'POST_COMMENT' | 'RESOURCE_COMMENT',
  status: '' as '' | '0' | '1' | '2',
})

const summary = computed(() => ({
  total: total.value,
  pending: records.value.filter((item) => item.status === 0).length,
  handled: records.value.filter((item) => item.status === 1).length,
  rejected: records.value.filter((item) => item.status === 2).length,
}))

function parseNullableInt(value: unknown) {
  if (value === '' || value === null || value === undefined) return null
  const n = Number(value)
  return Number.isFinite(n) ? Math.trunc(n) : null
}

function statusTagType(item: ReportListItem) {
  if (item.status === 0) return 'warning'
  if (item.status === 1) return 'success'
  return 'info'
}

function buildParams() {
  return {
    page: page.value,
    size: pageSize.value,
    keyword: filters.keyword.trim() || null,
    bizType: filters.bizType || null,
    status: parseNullableInt(filters.status),
  }
}

async function fetchReports() {
  const seq = ++fetchSeq.value
  loading.value = true
  try {
    const result = await reportApi.adminPage(buildParams())
    if (seq !== fetchSeq.value) return
    records.value = result.records ?? []
    const nextTotal = Number(result.total ?? 0)
    total.value = Number.isFinite(nextTotal) ? nextTotal : 0
  } catch (error) {
    if (seq !== fetchSeq.value) return
    records.value = []
    total.value = 0
    ElMessage.error('举报列表加载失败')
    console.error(error)
  } finally {
    if (seq === fetchSeq.value) {
      loading.value = false
    }
  }
}

function handleSearch() {
  page.value = 1
  void fetchReports()
}

function handleReset() {
  filters.keyword = ''
  filters.bizType = ''
  filters.status = ''
  page.value = 1
  void fetchReports()
}

function handlePageChange(nextPage: number) {
  page.value = nextPage
  void fetchReports()
}

function handleSizeChange(nextSize: number) {
  pageSize.value = nextSize
  page.value = 1
  void fetchReports()
}

async function openDetail(item: ReportListItem) {
  detailVisible.value = true
  detailLoading.value = true
  currentDetail.value = null
  try {
    currentDetail.value = await reportApi.adminDetail(item.id)
  } catch {
    ElMessage.error('举报详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleDecision(item: ReportListItem, status: number) {
  const text = status === 1 ? '处理' : '驳回'
  await ElMessageBox.confirm(`确认${text}举报「${item.bizTitle}」吗？`, '举报处理', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  })
  saving.value = true
  try {
    await reportApi.adminHandle(item.id, {
      status,
      handleRemark: status === 1 ? '已处理' : '已驳回',
    })
    ElMessage.success('举报状态已更新')
    await fetchReports()
  } catch (error) {
    ElMessage.error('举报处理失败，请稍后重试')
    console.error(error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void fetchReports()
})
</script>

<template>
  <div class="admin-page">
    <section class="hero-card">
      <div>
        <div class="eyebrow">后台管理</div>
        <h1 class="title">举报管理中心</h1>
        <p class="subtitle">集中查看用户举报，快速处理帖子、资源和评论违规内容。</p>
      </div>
      <div class="stats">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ summary.total }}</div>
          <div class="stat-label">当前结果数</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ summary.pending }}</div>
          <div class="stat-label">待处理</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ summary.handled }}</div>
          <div class="stat-label">已处理</div>
        </el-card>
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ summary.rejected }}</div>
          <div class="stat-label">已驳回</div>
        </el-card>
      </div>
    </section>

    <el-card class="filter-card" shadow="never">
      <div class="filters">
        <el-input v-model="filters.keyword" clearable placeholder="按举报原因或标题搜索" class="filter-input" />
        <el-select v-model="filters.bizType" clearable placeholder="举报类型" class="filter-select">
          <el-option label="帖子" value="POST" />
          <el-option label="资源" value="RESOURCE" />
          <el-option label="帖子评论" value="POST_COMMENT" />
          <el-option label="资源评论" value="RESOURCE_COMMENT" />
        </el-select>
        <el-select v-model="filters.status" clearable placeholder="处理状态" class="filter-select">
          <el-option label="待处理" value="0" />
          <el-option label="已处理" value="1" />
          <el-option label="已驳回" value="2" />
        </el-select>
        <div class="actions">
          <el-button type="primary" :loading="loading || saving" @click="handleSearch">搜索</el-button>
          <el-button :disabled="loading || saving" @click="handleReset">重置</el-button>
          <el-button :loading="loading || saving" @click="fetchReports">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column label="标题" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button link type="primary" class="title-btn" @click="openDetail(row)">{{ row.bizTitle || '-' }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="bizTypeDesc" label="类型" min-width="120" />
        <el-table-column prop="reporterName" label="举报人" min-width="120" />
        <el-table-column prop="bizOwnerName" label="被举报人" min-width="120" />
        <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }"><el-tag :type="statusTagType(row)">{{ row.statusDesc || '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">{{ row.createTime ? new Date(row.createTime).toLocaleString('zh-CN', { hour12: false }) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="220">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" :loading="saving" @click="openDetail(row)">查看</el-button>
              <el-button v-if="row.status === 0" link type="success" :loading="saving" @click="handleDecision(row, 1)">处理</el-button>
              <el-button v-if="row.status === 0" link type="warning" :loading="saving" @click="handleDecision(row, 2)">驳回</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="举报详情" width="820px" destroy-on-close>
      <el-skeleton v-if="detailLoading" animated :rows="8" />
      <template v-else>
        <div v-if="currentDetail" class="detail-panel">
          <div class="detail-title">{{ currentDetail.bizTitle }}</div>
          <div class="detail-meta">
            <span>类型：{{ currentDetail.bizTypeDesc }}</span>
            <span>举报人：{{ currentDetail.reporterName }}</span>
            <span>被举报人：{{ currentDetail.bizOwnerName }}</span>
            <span>状态：{{ currentDetail.statusDesc }}</span>
          </div>
          <div class="detail-content">{{ currentDetail.detail || currentDetail.bizSnippet || '暂无补充说明' }}</div>
          <div class="detail-content muted">举报原因：{{ currentDetail.reason }}</div>
          <div class="detail-content muted">处理备注：{{ currentDetail.handleRemark || '暂无' }}</div>
        </div>
        <el-empty v-else description="未获取到举报详情" />
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page { display: grid; gap: 16px; }
.hero-card, .filter-card, .table-card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.hero-card { display: flex; justify-content: space-between; gap: 20px; padding: 20px; background: linear-gradient(135deg, rgba(24, 144, 255, 0.08), rgba(24, 144, 255, 0.02)); }
.eyebrow { color: #1890ff; font-weight: 700; font-size: 13px; margin-bottom: 6px; }
.title { margin: 0; font-size: 24px; font-weight: 800; color: #1f2937; }
.subtitle { margin: 8px 0 0; color: #64748b; line-height: 1.6; }
.stats { display: grid; grid-template-columns: repeat(4, minmax(110px, 1fr)); gap: 12px; min-width: 480px; }
.stat-card { border-radius: 12px; text-align: center; }
.stat-value { font-size: 24px; font-weight: 800; color: #1890ff; }
.stat-label { margin-top: 4px; font-size: 13px; color: #64748b; }
.filters { display: grid; grid-template-columns: 1.4fr 0.8fr 0.8fr auto; gap: 12px; align-items: center; }
.actions { display: flex; gap: 8px; justify-content: flex-end; flex-wrap: wrap; }
.row-actions { display: flex; gap: 4px; flex-wrap: wrap; }
.title-btn { display: inline-block; max-width: 100%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.detail-panel { display: flex; flex-direction: column; gap: 10px; }
.detail-title { font-size: 18px; font-weight: 700; color: #1f2937; }
.detail-meta { display: flex; gap: 12px; flex-wrap: wrap; color: #64748b; font-size: 13px; }
.detail-content { max-height: 420px; overflow: auto; white-space: pre-wrap; line-height: 1.7; color: #111827; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; }
.detail-content.muted { color: #475569; }
.pagination { display: flex; justify-content: flex-end; padding-top: 16px; }
@media (max-width: 960px) { .hero-card { flex-direction: column; } .stats { min-width: 0; grid-template-columns: repeat(2, minmax(0, 1fr)); } .filters { grid-template-columns: 1fr; } .actions { justify-content: stretch; } }
</style>

