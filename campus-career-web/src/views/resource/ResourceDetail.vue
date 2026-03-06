<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { resourceApi, type ResourceDetail } from '@/api/resource'
import { ratingApi } from '@/api/rating'

const route = useRoute()

const resource = ref<ResourceDetail | null>(null)
const loading = ref(false)

const score = ref(0)
const comment = ref('')
const submitting = ref(false)

async function fetchDetail(id: number) {
  loading.value = true
  try {
    resource.value = await resourceApi.detail(id)
  } catch {
    ElMessage.error('资源详情加载失败，请稍后重试')
    resource.value = null
  } finally {
    loading.value = false
  }
}

function onDownload() {
  if (!resource.value?.fileUrl) return
  window.open(resource.value.fileUrl, '_blank', 'noopener,noreferrer')
}

async function submitRating() {
  if (!resource.value || !score.value) return
  submitting.value = true
  try {
    await ratingApi.rate({
      resourceId: resource.value.id,
      score: score.value,
      comment: comment.value.trim() || undefined,
    })
    score.value = 0
    comment.value = ''
    await fetchDetail(resource.value.id)
    ElMessage.success('评分提交成功')
  } catch {
    ElMessage.error('评分提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

watch(
  () => Number(route.params.id),
  async (id) => {
    if (!id) {
      resource.value = null
      return
    }
    score.value = 0
    comment.value = ''
    await fetchDetail(id)
  },
  { immediate: true },
)
</script>

<template>
  <div class="detail">
    <el-skeleton v-if="loading" animated :rows="6" />
    <template v-else>
      <el-card v-if="resource" class="main-card" shadow="never">
        <div class="header">
          <div>
            <div class="title">{{ resource.title }}</div>
            <div class="meta">
              <span class="meta-text">{{ resource.categoryName }}</span>
              <span class="meta-dot">·</span>
              <span class="meta-text">{{ resource.uploaderName }}</span>
              <span class="meta-dot">·</span>
              <span class="meta-text">
                {{ new Date(resource.createTime).toLocaleString() }}
              </span>
            </div>
          </div>
          <div class="download">
            <el-button type="primary" round @click="onDownload">下载资源</el-button>
          </div>
        </div>

        <div v-if="resource.description" class="desc">
          {{ resource.description }}
        </div>
        <div v-if="resource.tags" class="tags">
          <span v-for="tag in resource.tags.split(',')" :key="tag" class="tag">
            {{ tag }}
          </span>
        </div>

        <div class="rating-summary">
          <div class="score">
            <div class="score-main">
              <span class="score-num">
                {{ resource.scoreAvg?.toFixed?.(1) ?? '-' }}
              </span>
              <span class="score-unit">/ 5</span>
            </div>
            <div class="score-sub">
              {{ resource.scoreCount }} 人评分 · {{ resource.downloadCount }} 次下载
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="rate-card" shadow="never">
        <div class="rate-title">为资源打个分吧</div>
        <el-form @submit.prevent>
          <el-form-item label="评分">
            <el-rate v-model="score" :max="5" allow-half />
          </el-form-item>
          <el-form-item label="评价">
            <el-input
              v-model="comment"
              type="textarea"
              :rows="3"
              placeholder="可以简单写下资源的质量、是否推荐给同学等"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submitRating">
              提交评分
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>
  </div>
</template>

<style scoped>
.detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.main-card {
  border-radius: var(--ccp-card-radius);
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.title {
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 4px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

.meta-text {
  color: var(--ccp-text-muted);
}

.meta-dot {
  color: #9ca3af;
}

.download {
  display: flex;
  align-items: center;
}

.desc {
  margin-top: 10px;
  font-size: 14px;
  color: #111827;
}

.tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #f3f4ff;
  color: #4f46e5;
}

.rating-summary {
  margin-top: 14px;
}

.score-main {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-num {
  font-size: 24px;
  font-weight: 800;
}

.score-unit {
  font-size: 13px;
  color: #6b7280;
}

.score-sub {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

.rate-card {
  border-radius: var(--ccp-card-radius);
}

.rate-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 10px;
}

@media (max-width: 640px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

