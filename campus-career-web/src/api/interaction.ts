import { postJson } from '@/api/http'

/** 点赞 Toggle 返回：当前是否已赞 + 最新点赞数 */
export type LikeToggleResp = {
  liked: boolean
  likeCount: number
}

/** 收藏 Toggle 返回：当前是否已收藏 */
export type FavoriteToggleResp = {
  favorited: boolean
}

export const interactionApi = {
  /**
   * 点赞/取消点赞 Toggle（推荐）：一次请求完成切换，根据返回更新 UI
   */
  likeToggle(postId: number) {
    return postJson<LikeToggleResp, unknown>(`/forum/post/${postId}/like/toggle`)
  },

  /**
   * 收藏/取消收藏 Toggle（推荐）：一次请求完成切换，根据返回更新 UI
   */
  favoriteToggle(postId: number) {
    return postJson<FavoriteToggleResp, unknown>(`/forum/post/${postId}/favorite/toggle`)
  },
}

