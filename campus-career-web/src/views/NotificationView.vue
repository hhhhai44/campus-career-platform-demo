<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { notificationApi, type Notification } from '@/api/notification'
import { useNotificationStore } from '@/stores/notification'

const notifications = ref<Notification[]>([])
const loading = ref(false)
const current = ref(1)
const size = ref(10)
const total = ref(0)
const markAllLoading = ref(false)
const activeFilter = ref<'all' | 'unread'>('all')

const notificationStore = useNotificationStore()
const filteredNotifications = computed(() => {
  if (activeFilter.value === 'unread') {
    return notifications.value.filter((item) => item.isRead === 0)
  }
  return notifications.value
})

async function fetchNotifications() {
  loading.value = true
  try {
    const resp = await notificationApi.page({
      page: current.value,
      size: size.value,
    })
    notifications.value = resp.records
    total.value = resp.total
  } catch {
    notifications.value = []
    ElMessage.error('通知加载失败，稍后再试试')
  } finally {
    loading.value = false
  }
}

async function handlePageChange(page: number) {
  current.value = page
  await fetchNotifications()
}

async function markAsRead(item: Notification) {
  if (item.isRead === 1) return
  try {
    await notificationApi.markRead(item.id)
    item.isRead = 1
    notificationStore.decreaseUnread(1)
    ElMessage.success('已标记为已读')
  } catch {
    ElMessage.error('标记失败，稍后再试试')
  }
}

async function markAllRead() {
  markAllLoading.value = true
  try {
    await notificationApi.markAllRead()
    notifications.value = notifications.value.map((item) => ({ ...item, isRead: 1 }))
    notificationStore.resetUnread()
    ElMessage.success('全部通知都已读啦')
  } catch {
    ElMessage.error('操作没成功，稍后再试试')
  } finally {
    markAllLoading.value = false
  }
}

function formatTime(value: string) {
  return new Date(value).toLocaleString()
}

onMounted(() => {
  fetchNotifications()
  notificationStore.fetchUnreadCount()
})
</script>

<template>
  <div class="notification-page">
    <div class="header ccp-page-header">
      <div>
        <div class="title">通知中心</div>
        <div class="sub">看看最近动态，及时跟进互动消息</div>
      </div>
      <div class="header-actions">
        <el-radio-group v-model="activeFilter" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="unread">未读</el-radio-button>
        </el-radio-group>
        <el-button type="primary" plain :loading="markAllLoading" @click="markAllRead">
          一键全部已读
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="list-card ccp-card">
      <el-skeleton v-if="loading" :rows="6" animated />

      <div v-else-if="!filteredNotifications.length" class="empty">暂时没有通知，去逛逛论坛吧</div>

      <div v-else class="list">
        <div
          v-for="item in filteredNotifications"
          :key="item.id"
          class="list-item"
          :class="{ unread: item.isRead === 0 }"
        >
          <div class="main" @click="markAsRead(item)">
            <div class="row-top">
              <div class="item-title">{{ item.title || '社区新动态' }}</div>
              <el-tag size="small" :type="item.isRead === 0 ? 'danger' : 'info'">
                {{ item.isRead === 0 ? '未读' : '已读' }}
              </el-tag>
            </div>
            <div class="content">{{ item.content }}</div>
            <div class="meta">
              <span>{{ item.typeDesc || item.typeName || '社区提醒' }}</span>
              <span>·</span>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
          <el-button
            v-if="item.isRead === 0"
            link
            type="primary"
            size="small"
            @click="markAsRead(item)"
          >
            标记已读
          </el-button>
        </div>
      </div>

      <div v-if="total > size" class="pager">
        <el-pagination
          :current-page="current"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.notification-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: var(--ccp-text);
}

.sub {
  margin-top: 2px;
  font-size: var(--ccp-sub-size);
  color: var(--ccp-sub-color);
}

.list-card {
  border-radius: var(--ccp-card-radius);
  border: 1px solid var(--ccp-card-border);
  box-shadow: var(--ccp-card-shadow);
}

.empty {
  color: var(--ccp-text-light);
  text-align: center;
  padding: 24px 0;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  border: 1px solid rgba(229, 231, 235, 0.9);
  border-radius: 14px;
  padding: 14px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  transition: transform var(--ccp-fast), box-shadow var(--ccp-fast), border-color var(--ccp-fast);
}

.list-item.unread {
  border-color: rgba(74, 111, 255, 0.2);
  background: rgba(74, 111, 255, 0.04);
}

.list-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(16, 24, 40, 0.06);
}

.main {
  flex: 1;
  cursor: pointer;
}

.row-top {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: space-between;
}

.item-title {
  font-weight: 700;
  color: var(--ccp-text);
}

.content {
  margin-top: 6px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.5;
}

.meta {
  margin-top: 8px;
  font-size: 12px;
  color: var(--ccp-text-light);
  display: flex;
  align-items: center;
  gap: 8px;
}

.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>

