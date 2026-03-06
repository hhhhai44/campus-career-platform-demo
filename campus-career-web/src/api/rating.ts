import { postJson } from '@/api/http'

export type RateResourceReq = {
  resourceId: number
  score: number
  comment?: string
}

export const ratingApi = {
  // POST /resource/rating
  rate(req: RateResourceReq) {
    return postJson<void, RateResourceReq>('/resource/rating', req)
  },
}

