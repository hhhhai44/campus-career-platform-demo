/**
 * 示例 Store（模板演示用）：
 * - 展示 Pinia 的组合式写法（setup store）
 * - 当前项目默认并未引用该 store
 *
 * 可删除：
 * - 如果你不需要该示例，且全项目没有引用 `useCounterStore`，可直接删除此文件。
 * - 如果你决定完全不使用 Pinia，可删除整个 `src/stores/` 并移除 `src/main.ts` 中的 Pinia 注册及依赖。
 */
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useCounterStore = defineStore('counter', () => {
  const count = ref(0)
  const doubleCount = computed(() => count.value * 2)
  function increment() {
    count.value++
  }

  return { count, doubleCount, increment }
})
