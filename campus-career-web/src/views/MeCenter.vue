<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { notificationApi, type Notification, type PageResult } from '@/api/notification'

const auth = useAuthStore()

const notifications = ref<Notification[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const displayUsername = computed(() => auth.username || '未登录用户')
const avatarText = computed(() => (auth.username?.slice(0, 1) || 'U').toUpperCase())

async function fetchNotifications() {
  loading.value = true
  try {
    const resp: PageResult<Notification> = await notificationApi.page({
      page: page.value,
      size: size.value,
    })
    notifications.value = resp.records
    total.value = resp.total
  } catch {
    notifications.value = []
    ElMessage.error('通知加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function markOne(id: number) {
  try {
    await notificationApi.markRead(id)
    await fetchNotifications()
  } catch {
    ElMessage.error('标记失败，请稍后重试')
  }
}

async function markAll() {
  try {
    await notificationApi.markAllRead()
    await fetchNotifications()
  } catch {
    ElMessage.error('操作失败，请稍后重试')
  }
}

function handlePageChange(p: number) {
  page.value = p
  fetchNotifications()
}

onMounted(() => {
  fetchNotifications()
})
</script>

<template>
  <div class="me">
    <div class="header">
      <div class="title">我的</div>
      <div class="sub">查看个人通知与互动动态</div>
    </div>

    <div class="user-card">
      <div class="avatar">
        <span>{{ avatarText }}</span>
      </div>
      <div class="info">
        <div class="name">当前登录用户</div>
        <div class="meta">用户名: {{ displayUsername }}</div>
      </div>
    </div>

    <el-card class="notify-card" shadow="never">
      <div class="notify-header">
        <div class="notify-title">消息通知</div>
        <el-button text type="primary" @click="markAll">全部标为已读</el-button>
      </div>

      <el-skeleton v-if="loading" animated :rows="6" />

      <div v-else class="notify-list">
        <div v-for="item in notifications" :key="item.id" class="notify-item">
          <div class="notify-main">
            <div class="notify-title-line">
              <span class="dot" :class="{ unread: item.isRead === 0 }"></span>
              <span class="title-text">{{ item.title }}</span>
            </div>
            <div class="content">{{ item.content }}</div>
            <div class="meta">
              <span>
                {{ new Date(item.createTime).toLocaleString() }}
              </span>
              <span class="meta-sep">·</span>
              <span>{{ item.typeName ?? item.typeDesc ?? `类型${item.type}` }} · {{ item.bizTypeName ?? item.bizTypeDesc ?? `业务${item.bizType}` }}</span>
            </div>
          </div>
          <div class="notify-actions">
            <el-button
              v-if="item.isRead === 0"
              link
              type="primary"
              size="small"
              @click="markOne(item.id)"
            >
              标为已读
            </el-button>
          </div>
        </div>
        <div v-if="!notifications.length" class="empty-text">暂无通知</div>
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
    </el-card>
  </div>
</template>

<style scoped>
.me {
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

.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 14px;
  background: #f9fafb;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  background: #111827;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
}

.info .name {
  font-size: 14px;
  font-weight: 600;
}

.info .meta {
  font-size: 12px;
  color: var(--ccp-text-light);
}

.notify-card {
  border-radius: var(--ccp-card-radius);
}

.notify-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.notify-title {
  font-size: 16px;
  font-weight: 700;
}

.notify-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notify-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.notify-main {
  flex: 1;
}

.notify-title-line {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #d1d5db;
}

.dot.unread {
  background: #22c55e;
}

.title-text {
  font-size: 14px;
  font-weight: 600;
}

.content {
  margin-top: 2px;
  font-size: 13px;
  color: #4b5563;
}

.meta {
  margin-top: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.meta-sep {
  margin: 0 4px;
}

.notify-actions {
  display: flex;
  align-items: center;
}

.empty-text {
  padding: 18px 0;
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
