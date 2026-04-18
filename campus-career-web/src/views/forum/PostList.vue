<script setup lang="ts">
import { defineAsyncComponent, onActivated, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { categoryApi, type CategoryItem } from '@/api/category'
import { postApi, type PageResult, type PostListItem } from '@/api/post'
import { TIME_RANGE_OPTIONS, buildTimeRangeQuery, normalizeTimeRange, type TimeRangeKey } from '@/utils/timeRange'

const PostCard = defineAsyncComponent(() => import('@/components/PostCard.vue'))

const route = useRoute()
const router = useRouter()

const posts = ref<PostListItem[]>([])
const categories = ref<CategoryItem[]>([])
const loading = ref(false)
const loadingCategory = ref(false)

const keyword = ref('')
const activeCategoryId = ref<number | null>(null)
const activeTimeRange = ref<TimeRangeKey>(normalizeTimeRange(route.query.timeRange))

const page = ref(1)
const size = ref(10)
const total = ref(0)
const lastLoadedAt = ref(0)

let categoryRequestSeq = 0
let postRequestSeq = 0
const STALE_REFRESH_MS = 45 * 1000

async function fetchCategories() {
  const requestSeq = ++categoryRequestSeq
  loadingCategory.value = true
  try {
    const response = await categoryApi.list()
    if (requestSeq !== categoryRequestSeq) return
    categories.value = response
  } finally {
    if (requestSeq === categoryRequestSeq) {
      loadingCategory.value = false
    }
  }
}

async function fetchPosts() {
  const requestSeq = ++postRequestSeq
  loading.value = true
  try {
    const resp: PageResult<PostListItem> = await postApi.page({
      page: page.value,
      size: size.value,
      keyword: keyword.value || null,
      categoryId: activeCategoryId.value,
      timeRange: activeTimeRange.value === 'all' ? null : activeTimeRange.value,
    })
    if (requestSeq !== postRequestSeq) return
    posts.value = resp.records
    total.value = resp.total
    lastLoadedAt.value = Date.now()
  } finally {
    if (requestSeq === postRequestSeq) {
      loading.value = false
    }
  }
}

function syncTimeRangeToRoute(timeRange: TimeRangeKey) {
  const query = { ...route.query } as Record<string, string | number | (string | number)[]>
  if (timeRange === 'all') {
    delete query.timeRange
  } else {
    Object.assign(query, buildTimeRangeQuery(timeRange))
  }
  void router.replace({ name: 'forum-list', query })
}

function onTimeRangeChange(timeRange: TimeRangeKey) {
  if (activeTimeRange.value === timeRange) return
  activeTimeRange.value = timeRange
  page.value = 1
  fetchPosts()
  syncTimeRangeToRoute(timeRange)
}

function onSearch() {
  page.value = 1
  fetchPosts()
}

function onCategoryChange(id: number | null) {
  activeCategoryId.value = id
  page.value = 1
  fetchPosts()
}

function handlePageChange(p: number) {
  page.value = p
  fetchPosts()
}

onMounted(() => {
  activeTimeRange.value = normalizeTimeRange(route.query.timeRange)
  fetchCategories()
  fetchPosts()
})

onActivated(() => {
  if (Date.now() - lastLoadedAt.value > STALE_REFRESH_MS) {
    fetchPosts()
  }
})

watch(
  () => route.query.timeRange,
  (value) => {
    const next = normalizeTimeRange(value)
    if (next === activeTimeRange.value) return
    activeTimeRange.value = next
    page.value = 1
    fetchPosts()
  },
)
</script>

<template>
  <div class="forum-list">
    <div class="header ccp-page-header">
      <div>
        <div class="title">论坛广场</div>
        <div class="sub">分享实习、竞赛、课程与求职经历，也看看大家最近在聊什么</div>
      </div>
    </div>

    <div class="toolbar ccp-card">
      <el-input
        v-model="keyword"
        placeholder="找找看你关心的话题"
        clearable
        class="search-input"
        @keyup.enter="onSearch"
        @clear="onSearch"
      >
        <template #append>
          <el-button type="primary" @click="onSearch">找找看</el-button>
        </template>
      </el-input>
    </div>

    <div class="time-filters ccp-card">
      <span class="time-filter-label">时间范围</span>
      <button
        v-for="option in TIME_RANGE_OPTIONS"
        :key="option.value"
        class="time-chip"
        :class="{ active: activeTimeRange === option.value }"
        type="button"
        @click="onTimeRangeChange(option.value)"
      >
        {{ option.label }}
      </button>
    </div>

    <div class="categories ccp-card">
      <button
        class="cat-chip"
        :class="{ active: activeCategoryId === null }"
        type="button"
        @click="onCategoryChange(null)"
      >
        全部
      </button>
      <template v-if="!loadingCategory">
        <button
          v-for="cat in categories"
          :key="cat.id"
          class="cat-chip"
          :class="{ active: activeCategoryId === cat.id }"
          type="button"
          @click="onCategoryChange(cat.id)"
        >
          {{ cat.name }}
        </button>
      </template>
      <span v-else class="cat-loading">正在准备分类...</span>
    </div>

    <el-skeleton v-if="loading" animated :rows="6" />

    <div v-else class="list">
      <PostCard v-for="item in posts" :key="item.id" :post="item" @refresh="fetchPosts" />
      <div v-if="!posts.length && !loading" class="empty-text">
        还没有匹配内容，换个关键词试试，或发布你的第一个帖子。
      </div>
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
  </div>
</template>

<style scoped>
.forum-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.header {
  margin-bottom: 4px;
}

.title {
  font-size: 24px;
  font-weight: var(--ccp-title-weight);
  color: var(--ccp-text);
}

.sub {
  margin-top: 4px;
  font-size: var(--ccp-sub-size);
  color: var(--ccp-sub-color);
  line-height: 1.6;
}

.toolbar {
  display: flex;
  padding: 16px;
  gap: 10px;
}

.time-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 14px 16px;
  align-items: center;
}

.time-filter-label {
  font-size: 12px;
  color: var(--ccp-text-light);
  margin-right: 4px;
}

.time-chip {
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(248, 249, 250, 0.95);
  padding: 7px 14px;
  font-size: 12px;
  color: var(--ccp-text-secondary);
  cursor: pointer;
  transition: all var(--ccp-fast);
}

.time-chip.active {
  background: var(--ccp-primary-gradient);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 10px 22px rgba(42, 92, 255, 0.18);
}

.time-chip:hover {
  transform: translateY(-1px);
  border-color: rgba(74, 111, 255, 0.24);
}

.search-input {
  max-width: 480px;
}

.categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 14px 16px;
  align-items: center;
}

.cat-chip {
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(248, 249, 250, 0.95);
  padding: 7px 14px;
  font-size: 12px;
  color: var(--ccp-text-secondary);
  cursor: pointer;
  transition: all var(--ccp-fast);
}

.cat-chip.active {
  background: var(--ccp-primary-gradient);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 10px 22px rgba(42, 92, 255, 0.18);
}

.cat-chip:hover {
  transform: translateY(-1px);
  border-color: rgba(74, 111, 255, 0.24);
}

.cat-loading {
  font-size: 12px;
  color: var(--ccp-text-light);
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list > * {
  content-visibility: auto;
  contain-intrinsic-size: 240px;
}

.empty-text {
  padding: 32px 0;
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
