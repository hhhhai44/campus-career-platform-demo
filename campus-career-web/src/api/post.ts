import { deleteJson, getJson, postJson, putJson } from '@/api/http'

export type PageRequest = {
  page?: number
  size?: number
  keyword?: string | null
  categoryId?: number | null
}

export type PageResult<T> = {
  records: T[]
  total: number
  current: number
  size: number
}

export type PostListItem = {
  id: number
  title: string
  summary?: string | null
  categoryId: number
  categoryName: string
  authorId: number
  authorName: string
  likeCount: number
  favoriteCount: number
  status?: number | null
  deleted?: number | null
  /** 当前用户是否已点赞 */
  liked?: boolean
  /** 当前用户是否已收藏 */
  favorited?: boolean
  viewCount: number
  commentCount: number
  createTime: string
}

export type PostDetail = {
  id: number
  title: string
  content: string
  categoryId: number
  categoryName: string
  authorId: number
  authorName: string
  likeCount: number
  status?: number | null
  deleted?: number | null
  /** 当前用户是否已点赞 */
  liked?: boolean
  viewCount: number
  commentCount: number
  /** 当前用户是否已收藏 */
  favorited?: boolean
  /** 当前登录用户ID（未登录为 null） */
  currentUserId?: number | null
  createTime: string
}

export type CreatePostReq = {
  title: string
  content: string
  categoryId: number
}

export type AdminPostPageRequest = PageRequest & {
  status?: number | null
  deleted?: number | null
}

export type PostModerationReq = {
  status: number
}

export const postApi = {
  // POST /forum/post
  create(req: CreatePostReq) {
    return postJson<number, CreatePostReq>('/forum/post', req)
  },

  // GET /forum/post/page
  page(params: PageRequest) {
    return getJson<PageResult<PostListItem>>('/forum/post/page', { params })
  },

  // GET /forum/post/{id}
  detail(id: number) {
    return getJson<PostDetail>(`/forum/post/${id}`)
  },

  // DELETE /forum/post/{id}
  delete(id: number) {
    return deleteJson<void>(`/forum/post/${id}`)
  },

  // GET /admin/forum/post/page
  adminPage(params: AdminPostPageRequest) {
    return getJson<PageResult<PostListItem>>('/admin/forum/post/page', { params })
  },

  // GET /admin/forum/post/{id}
  adminDetail(id: number) {
    return getJson<PostDetail>(`/admin/forum/post/${id}`)
  },

  // PUT /admin/forum/post/{id}/status
  adminReview(id: number, req: PostModerationReq) {
    return putJson<void, PostModerationReq>(`/admin/forum/post/${id}/status`, req)
  },

  // DELETE /admin/forum/post/{id}
  adminDelete(id: number) {
    return deleteJson<void>(`/admin/forum/post/${id}`)
  },

  // PUT /admin/forum/post/{id}/restore
  adminRestore(id: number) {
    return putJson<void, void>(`/admin/forum/post/${id}/restore`)
  },
}
