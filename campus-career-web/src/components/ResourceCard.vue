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
  <el-card class="resource-card" shadow="hover" @click="gotoDetail">
    <div class="title">{{ resource.title }}</div>
    <div class="meta">
      <span class="meta-text">{{ resource.categoryName }}</span>
      <span class="meta-dot">·</span>
      <span class="meta-text">{{ resource.uploaderName }}</span>
    </div>
    <div v-if="resource.description" class="desc">
      {{ resource.description }}
    </div>
    <div class="bottom">
      <div class="stats">
        <span>⭐ {{ resource.scoreAvg?.toFixed?.(1) ?? '-' }} ({{ resource.scoreCount }})</span>
        <span>收藏 {{ resource.favoriteCount }}</span>
        <span>⬇️ {{ resource.downloadCount }}</span>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.resource-card {
  border-radius: var(--ccp-card-radius);
  cursor: pointer;
}

.title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.meta-text {
  color: var(--ccp-text-muted);
}

.meta-dot {
  color: #9ca3af;
}

.desc {
  font-size: 13px;
  color: #4b5563;
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
  color: #9ca3af;
}
</style>
