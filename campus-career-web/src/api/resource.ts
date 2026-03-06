import { getJson, postJson } from '@/api/http'

import type { PageResult as PostPageResult } from './post'

export type PageResult<T> = PostPageResult<T>

export type ResourceListItem = {
  id: number
  title: string
  description?: string | null
  categoryId: number
  categoryName: string
  uploaderId: number
  uploaderName: string
  scoreAvg: number
  scoreCount: number
  favoriteCount: number
  downloadCount: number
  createTime: string
}

export type ResourceDetail = {
  id: number
  title: string
  description: string
  categoryId: number
  categoryName: string
  uploaderId: number
  uploaderName: string
  fileUrl: string
  tags: string | null
  scoreAvg: number
  scoreCount: number
  downloadCount: number
  createTime: string
}

export type UploadResourceReq = {
  title: string
  description?: string
  categoryId: number
  fileUrl: string
  tags?: string
}

export const resourceApi = {
  // POST /resource
  upload(req: UploadResourceReq) {
    return postJson<number, UploadResourceReq>('/resource', req)
  },

  // GET /resource/page
  page(params: { page?: number; size?: number; keyword?: string | null; categoryId?: number | null }) {
    return getJson<PageResult<ResourceListItem>>('/resource/page', { params })
  },

  // GET /resource/{id}
  detail(id: number) {
    return getJson<ResourceDetail>(`/resource/${id}`)
  },
}
