import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationApi } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  async function fetchUnreadCount() {
    unreadCount.value = await notificationApi.unreadCount()
  }

  function decreaseUnread(delta = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - Math.max(0, delta))
  }

  function resetUnread() {
    unreadCount.value = 0
  }

  return {
    unreadCount,
    fetchUnreadCount,
    decreaseUnread,
    resetUnread,
  }
})

