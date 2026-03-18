import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationApi } from '@/api/notification'

const UNREAD_REFRESH_INTERVAL_MS = 10 * 1000

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const lastFetchAt = ref(0)

  async function fetchUnreadCount(options?: { force?: boolean }) {
    const now = Date.now()
    if (!options?.force && now - lastFetchAt.value < UNREAD_REFRESH_INTERVAL_MS) {
      return unreadCount.value
    }
    unreadCount.value = await notificationApi.unreadCount()
    lastFetchAt.value = now
    return unreadCount.value
  }

  function decreaseUnread(delta = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - Math.max(0, delta))
    lastFetchAt.value = Date.now()
  }

  function resetUnread() {
    unreadCount.value = 0
    lastFetchAt.value = 0
  }

  return {
    unreadCount,
    fetchUnreadCount,
    decreaseUnread,
    resetUnread,
  }
})
