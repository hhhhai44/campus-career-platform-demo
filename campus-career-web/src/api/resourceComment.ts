import { deleteJson, getJson, postJson } from '@/api/http'
import type { PageResult } from '@/api/post'

export type ResourceComment = {
  id: number
  resourceId: number
  rootId: number | null
  parentId: number | null
  fromUserId: number
  fromUserName: string
  toUserId: number | null
  toUserName: string | null
  content: string
  likeCount: number
  createTime: string
  children?: ResourceComment[]
}

export type CreateResourceCommentReq = {
  resourceId: number
  content: string
  rootId?: number | null
  parentId?: number | null
  toUserId?: number | null
}

export const resourceCommentApi = {
  create(req: CreateResourceCommentReq) {
    return postJson<number, CreateResourceCommentReq>('/resource/comment', req)
  },

  page(resourceId: number, page: number, size: number) {
    return getJson<PageResult<ResourceComment>>(`/resource/comment/${resourceId}/page`, {
      params: { page, size },
    })
  },

  delete(id: number) {
    return deleteJson(`/resource/comment/${id}`)
  },
}

