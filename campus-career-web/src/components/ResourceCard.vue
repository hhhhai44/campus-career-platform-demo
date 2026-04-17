<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { ResourceListItem } from '@/api/resource'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  resource: ResourceListItem
}>()

const router = useRouter()
const auth = useAuthStore()

function gotoDetail() {
  router.push({ name: 'resource-detail', params: { id: props.resource.id } })
}

function gotoUploaderMessage(e: MouseEvent) {
  e.stopPropagation()
  if (!props.resource.uploaderId || (auth.userId && auth.userId === props.resource.uploaderId)) return
  router.push({
    name: 'message-center',
    query: { peer: String(props.resource.uploaderId), peerName: props.resource.uploaderName },
  })
}
</script>

<template>
  <el-card class="resource-card ccp-card" shadow="never" @click="gotoDetail">
    <div class="resource-top">
      <span class="tag">{{ resource.categoryName }}</span>
      <button
        v-if="!auth.userId || auth.userId !== resource.uploaderId"
        type="button"
        class="uploader-link"
        @click="gotoUploaderMessage"
      >
        <span class="uploader-avatar">{{ resource.uploaderName?.slice(0, 1)?.toUpperCase() || 'U' }}</span>
        <span class="meta-text">{{ resource.uploaderName }}</span>
      </button>
      <span v-else class="uploader-link uploader-text">
        <span class="uploader-avatar">{{ resource.uploaderName?.slice(0, 1)?.toUpperCase() || 'U' }}</span>
        <span class="meta-text">{{ resource.uploaderName }}</span>
      </span>
    </div>
    <div class="title">{{ resource.title }}</div>
    <div v-if="resource.description" class="desc">
      {{ resource.description }}
    </div>
    <div class="bottom">
      <div class="stats">
        <span>⭐ {{ resource.scoreAvg?.toFixed?.(1) ?? '-' }} ({{ resource.scoreCount }})</span>
        <span>👍 {{ resource.likeCount }}</span>
        <span>⭐收藏 {{ resource.favoriteCount }}</span>
        <span>💬 {{ resource.commentCount }}</span>
        <span>⬇️ {{ resource.downloadCount }}</span>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.resource-card {
  border-radius: var(--ccp-card-radius);
  cursor: pointer;
  transition: transform var(--ccp-fast), box-shadow var(--ccp-fast);
  border: 1px solid var(--ccp-card-border);
  box-shadow: var(--ccp-card-shadow);
  background: var(--ccp-card-bg);
}

.resource-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--ccp-card-shadow-hover);
}

.title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--ccp-text);
}

.resource-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.meta {
  color: var(--ccp-text-muted);
}

.uploader-link {
  border: 0;
  background: transparent;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  cursor: pointer;
}

.uploader-text {
  cursor: default;
}

.uploader-avatar {
  width: 20px;
  height: 20px;
  border-radius: 999px;
  background: var(--ccp-primary-gradient);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
}

.uploader-link .meta-text {
  color: var(--ccp-primary);
}

.uploader-link:hover .meta-text {
  text-decoration: underline;
}

.uploader-text:hover .meta-text {
  text-decoration: none;
}

.tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 159, 67, 0.12);
  color: #b45309;
  font-weight: 600;
  font-size: 12px;
}

.desc {
  font-size: 13px;
  color: var(--ccp-text-secondary);
  margin-bottom: 6px;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bottom {
  margin-top: 2px;
}

.stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--ccp-text-light);
  flex-wrap: wrap;
}
</style>
