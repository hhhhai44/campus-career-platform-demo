<script setup lang="ts">
import { defineAsyncComponent, onActivated, onMounted, ref } from 'vue'
import { resourceApi, type PageResult, type ResourceListItem } from '@/api/resource'
import { categoryApi, type CategoryItem } from '@/api/category'

const ResourceCard = defineAsyncComponent(() => import('@/components/ResourceCard.vue'))

const resources = ref<ResourceListItem[]>([])
const categories = ref<CategoryItem[]>([])
const loading = ref(false)
const loadingCategory = ref(false)

const keyword = ref('')
const activeCategoryId = ref<number | null>(null)

const page = ref(1)
const size = ref(12)
const total = ref(0)
const lastLoadedAt = ref(0)

let categoryRequestSeq = 0
let resourceRequestSeq = 0
const STALE_REFRESH_MS = 45 * 1000

async function fetchCategories() {
  const requestSeq = ++categoryRequestSeq
  loadingCategory.value = true
  try {
    const response = await categoryApi.resourceList()
    if (requestSeq !== categoryRequestSeq) return
    categories.value = response
  } finally {
    if (requestSeq === categoryRequestSeq) {
      loadingCategory.value = false
    }
  }
}

async function fetchResources() {
  const requestSeq = ++resourceRequestSeq
  loading.value = true
  try {
    const resp: PageResult<ResourceListItem> = await resourceApi.page({
      page: page.value,
      size: size.value,
      keyword: keyword.value || null,
      categoryId: activeCategoryId.value,
    })
    if (requestSeq !== resourceRequestSeq) return
    resources.value = resp.records
    total.value = resp.total
    lastLoadedAt.value = Date.now()
  } finally {
    if (requestSeq === resourceRequestSeq) {
      loading.value = false
    }
  }
}

function onSearch() {
  page.value = 1
  fetchResources()
}

function onCategoryChange(id: number | null) {
  activeCategoryId.value = id
  page.value = 1
  fetchResources()
}

function handlePageChange(p: number) {
  page.value = p
  fetchResources()
}

onMounted(() => {
  fetchCategories()
  fetchResources()
})

onActivated(() => {
  if (Date.now() - lastLoadedAt.value > STALE_REFRESH_MS) {
    fetchResources()
  }
})
</script>

<template>
  <div class="resource-list-page">
    <div class="header">
      <div class="title">资源库</div>
      <div class="sub">整理课程笔记、面经、项目模板与学习资料</div>
    </div>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索资源标题或关键词"
        clearable
        class="search-input"
        @keyup.enter="onSearch"
        @clear="onSearch"
      >
        <template #append>
          <el-button type="primary" @click="onSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div class="categories">
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
      <span v-else class="cat-loading">加载分类中...</span>
    </div>

    <el-skeleton v-if="loading" animated :rows="8" />

    <div v-else class="grid">
      <ResourceCard v-for="item in resources" :key="item.id" :resource="item" />
      <div v-if="!resources.length && !loading" class="empty-text">暂时没有相关资源</div>
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
.resource-list-page {
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

.toolbar {
  display: flex;
  gap: 10px;
}

.search-input {
  max-width: 420px;
}

.categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cat-chip {
  border-radius: 999px;
  border: 1px solid var(--ccp-card-border);
  background: #f9fafb;
  padding: 4px 12px;
  font-size: 12px;
  color: var(--ccp-text-secondary);
  cursor: pointer;
}

.cat-chip.active {
  background: var(--ccp-text);
  border-color: var(--ccp-text);
  color: #f9fafb;
}

.cat-loading {
  font-size: 12px;
  color: var(--ccp-text-light);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}

.grid > * {
  content-visibility: auto;
  contain-intrinsic-size: 300px;
}

.empty-text {
  grid-column: 1 / -1;
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

