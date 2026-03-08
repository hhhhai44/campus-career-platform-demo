import { deleteJson, getJson, postJson } from '@/api/http'

import type { LikeToggleResp } from './interaction'
import type { PageResult } from './post'

export type PostComment = {
  id: number
  postId: number
  rootId: number | null
  parentId: number | null
  fromUserId: number
  fromUserName: string
  toUserId: number | null
  toUserName: string | null
  content: string
  likeCount: number
  liked?: boolean
  createTime: string
  children?: PostComment[]
}

export type CreateCommentReq = {
  postId: number
  content: string
  rootId?: number | null
  parentId?: number | null
  toUserId?: number | null
}

export const commentApi = {
  // POST /forum/post/comment
  create(req: CreateCommentReq) {
    return postJson<number, CreateCommentReq>('/forum/post/comment', req)
  },

  // GET /forum/post/comment/{postId}/page
  page(postId: number, page: number, size: number) {
    return getJson<PageResult<PostComment>>(`/forum/post/comment/${postId}/page`, {
      params: { page, size },
    })
  },

  // DELETE /forum/post/comment/{id}
  delete(id: number) {
    return deleteJson(`/forum/post/comment/${id}`)
  },

  // POST /forum/post/comment/{id}/like/toggle
  likeToggle(id: number) {
    return postJson<LikeToggleResp, unknown>(`/forum/post/comment/${id}/like/toggle`)
  },
}
