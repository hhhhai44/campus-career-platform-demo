<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userAdminApi, type UserAdminDetail, type UserAdminListItem } from '@/api/user'

const loading = ref(false)
const saving = ref(false)
const records = ref<UserAdminListItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const fetchSeq = ref(0)
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentDetail = ref<UserAdminDetail | null>(null)

const filters = reactive({
  keyword: '',
  role: '' as '' | '1' | '2' | '3',
  status: '' as '' | '0' | '1',
})

const summary = computed(() => ({
  total: total.value,
  normal: records.value.filter((item) => item.status === 1).length,
  disabled: records.value.filter((item) => item.status === 0).length,
}))

function parseNullableInt(value: unknown) {
  if (value === '' || value === null || value === undefined) return null
  const n = Number(value)
  return Number.isFinite(n) ? Math.trunc(n) : null
}

function statusTagType(item: UserAdminListItem) {
  return item.status === 0 ? 'danger' : 'success'
}

function buildParams() {
  return {
    page: page.value,
    size: pageSize.value,
    keyword: filters.keyword.trim() || null,
    role: parseNullableInt(filters.role),
    status: parseNullableInt(filters.status),
  }
}

async function fetchUsers() {
  const seq = ++fetchSeq.value
  loading.value = true
  try {
    const result = await userAdminApi.adminPage(buildParams())
    if (seq !== fetchSeq.value) return
    records.value = result.records ?? []
    const nextTotal = Number(result.total ?? 0)
    total.value = Number.isFinite(nextTotal) ? nextTotal : 0
  } catch (error) {
    if (seq !== fetchSeq.value) return
    records.value = []
    total.value = 0
    ElMessage.error('用户列表加载失败')
    console.error(error)
  } finally {
    if (seq === fetchSeq.value) {
      loading.value = false
    }
  }
}

function handleSearch() {
  page.value = 1
  void fetchUsers()
}

function handleReset() {
  filters.keyword = ''
  filters.role = ''
  filters.status = ''
  page.value = 1
  void fetchUsers()
}

function handlePageChange(nextPage: number) {
  page.value = nextPage
  void fetchUsers()
}

function handleSizeChange(nextSize: number) {
  pageSize.value = nextSize
  page.value = 1
  void fetchUsers()
}

async function openDetail(item: UserAdminListItem) {
  detailVisible.value = true
  detailLoading.value = true
  currentDetail.value = null
  try {
    currentDetail.value = await userAdminApi.adminDetail(item.id)
  } catch {
    ElMessage.error('用户详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function updateStatus(item: UserAdminListItem, status: number) {
  const text = status === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${text}用户「${item.username}」吗？`, '用户管理', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  })
  saving.value = true
  try {
    await userAdminApi.updateStatus(item.id, status)
    ElMessage.success('用户状态已更新')
    await fetchUsers()
  } catch (error) {
    ElMessage.error('用户状态更新失败，请稍后重试')
    console.error(error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void fetchUsers()
})
</script>

<template>
  <div class="admin-page">
    <section class="hero-card">
      <div>
        <div class="eyebrow">后台管理</div>
        <h1 class="title">用户管理中心</h1>
        <p class="subtitle">统一查看账号信息、登录状态，并对异常账号执行禁用或启用。</p>
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
      </div>
    </section>

    <el-card class="filter-card" shadow="never">
      <div class="filters">
        <el-input v-model="filters.keyword" clearable placeholder="按用户名搜索" class="filter-input" />
        <el-select v-model="filters.role" clearable placeholder="用户角色" class="filter-select">
          <el-option label="学生" value="1" />
          <el-option label="管理员" value="2" />
          <el-option label="教师" value="3" />
        </el-select>
        <el-select v-model="filters.status" clearable placeholder="账号状态" class="filter-select">
          <el-option label="正常" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
        <div class="actions">
          <el-button type="primary" :loading="loading || saving" @click="handleSearch">搜索</el-button>
          <el-button :disabled="loading || saving" @click="handleReset">重置</el-button>
          <el-button :loading="loading || saving" @click="fetchUsers">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column label="用户名" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button link type="primary" class="title-btn" @click="openDetail(row)">{{ row.username }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="roleDesc" label="角色" min-width="100" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }"><el-tag :type="statusTagType(row)">{{ row.statusDesc || '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="loginFailCount" label="失败次数" width="100" />
        <el-table-column label="最后登录" min-width="180">
          <template #default="{ row }">{{ row.lastLoginTime ? new Date(row.lastLoginTime).toLocaleString('zh-CN', { hour12: false }) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="200">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" :loading="saving" @click="openDetail(row)">查看</el-button>
              <el-button v-if="row.status === 1" link type="warning" :loading="saving" @click="updateStatus(row, 0)">禁用</el-button>
              <el-button v-else link type="success" :loading="saving" @click="updateStatus(row, 1)">启用</el-button>
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

    <el-dialog v-model="detailVisible" title="用户详情" width="720px" destroy-on-close>
      <el-skeleton v-if="detailLoading" animated :rows="6" />
      <template v-else>
        <div v-if="currentDetail" class="detail-panel">
          <div class="detail-title">{{ currentDetail.username }}</div>
          <div class="detail-meta">
            <span>角色：{{ currentDetail.roleDesc }}</span>
            <span>状态：{{ currentDetail.statusDesc }}</span>
          </div>
          <div class="detail-content">邮箱：{{ currentDetail.email || '-' }}</div>
          <div class="detail-content">手机号：{{ currentDetail.phone || '-' }}</div>
          <div class="detail-content">登录失败次数：{{ currentDetail.loginFailCount ?? 0 }}</div>
        </div>
        <el-empty v-else description="未获取到用户详情" />
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
.stats { display: grid; grid-template-columns: repeat(3, minmax(110px, 1fr)); gap: 12px; min-width: 380px; }
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
.detail-content { color: #111827; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; }
.pagination { display: flex; justify-content: flex-end; padding-top: 16px; }
@media (max-width: 960px) { .hero-card { flex-direction: column; } .stats { min-width: 0; grid-template-columns: repeat(2, minmax(0, 1fr)); } .filters { grid-template-columns: 1fr; } .actions { justify-content: stretch; } }
</style>

