import { getJson, postJson } from '@/api/http'
import type { FavoriteToggleResp, LikeToggleResp } from '@/api/interaction'

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
  likeCount: number
  favoriteCount: number
  commentCount: number
  contentPreview?: string | null
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
  content: string
  tags: string | null
  scoreAvg: number
  scoreCount: number
  likeCount: number
  favoriteCount: number
  liked: boolean
  favorited: boolean
  owner: boolean
  hasRated: boolean
  createTime: string
}

export type UploadResourceReq = {
  title: string
  description?: string
  content: string
  categoryId: number
  tags?: string
}

export const resourceApi = {
  // POST /resource
  upload(req: UploadResourceReq) {
    return postJson<number, UploadResourceReq>('/resource', req)
  },

  // GET /resource/page
  page(params: { page?: number; size?: number; keyword?: string | null; categoryId?: number | null; timeRange?: string | null }) {
    return getJson<PageResult<ResourceListItem>>('/resource/page', { params })
  },

  // GET /resource/my
  myPage(page = 1, size = 10) {
    return getJson<PageResult<ResourceListItem>>('/resource/my', { params: { page, size } })
  },

  // GET /resource/{id}
  detail(id: number) {
    return getJson<ResourceDetail>(`/resource/${id}`)
  },


  // POST /resource/{id}/like/toggle
  likeToggle(id: number) {
    return postJson<LikeToggleResp, unknown>(`/resource/${id}/like/toggle`)
  },

  // POST /resource/{id}/favorite/toggle
  favoriteToggle(id: number) {
    return postJson<FavoriteToggleResp, unknown>(`/resource/${id}/favorite/toggle`)
  },
}
