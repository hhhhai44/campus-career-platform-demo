import { getJson, postJson, putJson } from '@/api/http'
import type { PageResult } from '@/api/post'

export type ReportBizType = 'POST' | 'RESOURCE' | 'POST_COMMENT' | 'RESOURCE_COMMENT'

export type CreateReportReq = {
  bizType: ReportBizType
  bizId: number
  reason: string
  detail?: string | null
}

export type ReportListItem = {
  id: number
  bizType: ReportBizType
  bizTypeDesc?: string
  bizId: number
  bizTitle: string
  bizOwnerId: number
  bizOwnerName?: string
  reporterId: number
  reporterName?: string
  reason: string
  status: number
  statusDesc?: string
  handlerId?: number | null
  handlerName?: string | null
  createTime: string
  updateTime: string
}

export type ReportDetail = ReportListItem & {
  bizSnippet?: string | null
  detail?: string | null
  handleRemark?: string | null
}

export type HandleReportReq = {
  status: number
  handleRemark?: string | null
}

export const reportApi = {
  create(req: CreateReportReq) {
    return postJson<number, CreateReportReq>('/report', req)
  },

  adminPage(params: {
    page?: number
    size?: number
    bizType?: ReportBizType | null
    status?: number | null
    keyword?: string | null
  }) {
    return getJson<PageResult<ReportListItem>>('/admin/report/page', { params })
  },

  adminDetail(id: number) {
    return getJson<ReportDetail>(`/admin/report/${id}`)
  },

  adminHandle(id: number, req: HandleReportReq) {
    return putJson<void, HandleReportReq>(`/admin/report/${id}/handle`, req)
  },
}

