<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { PostListItem } from '@/api/post'
import { interactionApi } from '@/api/interaction'

const props = withDefaults(
  defineProps<{
    post: PostListItem
    clickable?: boolean
  }>(),
  {
    clickable: true,
  },
)

const emit = defineEmits<{
  refresh: []
}>()

const router = useRouter()

const likeCount = ref(props.post.likeCount)
const liked = ref(!!props.post.liked)
const likeLoading = ref(false)
const favorited = ref(!!props.post.favorited)
const favoriteLoading = ref(false)

watch(
  () => props.post,
  (p) => {
    likeCount.value = p.likeCount
    liked.value = !!p.liked
    favorited.value = !!p.favorited
  },
  { deep: true },
)

/** 点赞/取消点赞 Toggle：一次接口，根据返回更新 UI，防重复请求 */
async function toggleLike(e: MouseEvent) {
  e.stopPropagation()
  if (likeLoading.value) return
  likeLoading.value = true
  try {
    const res = await interactionApi.likeToggle(props.post.id)
    liked.value = res.liked
    likeCount.value = res.likeCount
    emit('refresh')
  } catch {
    // 可加 ElMessage
  } finally {
    likeLoading.value = false
  }
}

/** 收藏/取消收藏 Toggle：一次接口，根据返回更新 UI */
async function toggleFavorite(e: MouseEvent) {
  e.stopPropagation()
  if (favoriteLoading.value) return
  favoriteLoading.value = true
  try {
    const res = await interactionApi.favoriteToggle(props.post.id)
    favorited.value = res.favorited
    emit('refresh')
  } catch {
    // ignore
  } finally {
    favoriteLoading.value = false
  }
}

function gotoDetail() {
  if (!props.clickable) return
  router.push({ name: 'forum-detail', params: { id: props.post.id } })
}
</script>

<template>
  <el-card class="post-card" shadow="hover" @click="gotoDetail">
    <div class="title">{{ post.title }}</div>
    <div class="meta">
      <span class="tag">{{ post.categoryName }}</span>
      <span class="meta-text">{{ post.authorName }}</span>
      <span class="meta-dot">·</span>
      <span class="meta-text">
        {{ new Date(post.createTime).toLocaleString() }}
      </span>
    </div>
    <div v-if="post.summary" class="summary">
      {{ post.summary }}
    </div>
    <div class="bottom">
      <div class="stats">
        <span>👍 {{ likeCount }}</span>
        <span>⭐ {{ post.favoriteCount }}</span>
        <span>💬 {{ post.commentCount }}</span>
        <span>👀 {{ post.viewCount }}</span>
      </div>
      <div class="actions">
        <el-button
          link
          size="small"
          :type="liked ? 'primary' : 'default'"
          :loading="likeLoading"
          @click="toggleLike"
        >
          {{ liked ? '已赞' : '点赞' }}
        </el-button>
        <el-button
          link
          size="small"
          :type="favorited ? 'warning' : 'default'"
          :loading="favoriteLoading"
          @click="toggleFavorite"
        >
          {{ favorited ? '已收藏' : '收藏' }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.post-card {
  border-radius: var(--ccp-card-radius);
  cursor: pointer;
}

.title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 6px;
}

.tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4f46e5;
}

.meta-text {
  color: var(--ccp-text-muted);
}

.meta-dot {
  color: #9ca3af;
}

.summary {
  margin: 6px 0 8px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}

.stats {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #9ca3af;
}

.actions {
  display: flex;
  gap: 4px;
}
</style>
