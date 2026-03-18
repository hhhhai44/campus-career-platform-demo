import { getJson } from '@/api/http'

/** 分类列表项（帖子分类、资源分类通用） */
export type CategoryItem = {
  id: number
  name: string
}

export const categoryApi = {
  /** 帖子分类：GET /forum/category/list */
  list() {
    return getJson<CategoryItem[]>('/forum/category/list', { cacheTtlMs: 5 * 60 * 1000 })
  },
  /** 资源分类：GET /resource/category/list */
  resourceList() {
    return getJson<CategoryItem[]>('/resource/category/list', { cacheTtlMs: 5 * 60 * 1000 })
  },
}
