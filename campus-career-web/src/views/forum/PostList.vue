<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { categoryApi, type CategoryItem } from '@/api/category'
import { postApi, type PageResult, type PostListItem } from '@/api/post'
import PostCard from '@/components/PostCard.vue'

const posts = ref<PostListItem[]>([])
const categories = ref<CategoryItem[]>([])
const loading = ref(false)
const loadingCategory = ref(false)

const keyword = ref('')
const activeCategoryId = ref<number | null>(null)

const page = ref(1)
const size = ref(10)
const total = ref(0)

async function fetchCategories() {
  loadingCategory.value = true
  try {
    categories.value = await categoryApi.list()
  } finally {
    loadingCategory.value = false
  }
}

async function fetchPosts() {
  loading.value = true
  try {
    const resp: PageResult<PostListItem> = await postApi.page({
      page: page.value,
      size: size.value,
      keyword: keyword.value || null,
      categoryId: activeCategoryId.value,
    })
    posts.value = resp.records
    total.value = resp.total
  } finally {
    loading.value = false
  }
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
  fetchCategories()
  fetchPosts()
})
</script>

<template>
  <div class="forum-list">
    <div class="header">
      <div class="title">论坛</div>
      <div class="sub">分享你的实习、竞赛、求职与课程体验</div>
    </div>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索帖子标题或关键词"
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

    <el-skeleton v-if="loading" animated :rows="6" />

    <div v-else class="list">
      <PostCard v-for="item in posts" :key="item.id" :post="item" @refresh="fetchPosts" />
      <div v-if="!posts.length && !loading" class="empty-text">还没有相关帖子</div>
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

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
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
