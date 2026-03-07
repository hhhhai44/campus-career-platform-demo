import { deleteJson, getJson, postJson } from '@/api/http'

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
    return deleteJson(`/forum/post/${id}`)
  },

  // GET /forum/post/my
  myPosts(page: number, size: number) {
    return getJson<PageResult<PostListItem>>('/forum/post/my', {
      params: { page, size },
    })
  },
}
