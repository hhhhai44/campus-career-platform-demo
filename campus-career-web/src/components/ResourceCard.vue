<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { ResourceListItem } from '@/api/resource'

const props = defineProps<{
  resource: ResourceListItem
}>()

const router = useRouter()

function gotoDetail() {
  router.push({ name: 'resource-detail', params: { id: props.resource.id } })
}
</script>

<template>
  <el-card class="resource-card ccp-card" shadow="never" @click="gotoDetail">
    <div class="resource-top">
      <span class="tag">{{ resource.categoryName }}</span>
      <span class="meta-text">{{ resource.uploaderName }}</span>
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
