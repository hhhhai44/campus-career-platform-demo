import { getJson, putJson } from '@/api/http'
import type { PageResult } from '@/api/post'

export type UserAdminListItem = {
  id: number
  username: string
  role?: number | null
  roleDesc?: string | null
  status?: number | null
  statusDesc?: string | null
  loginFailCount?: number | null
  lastLoginTime?: string | null
}

export type UserAdminDetail = UserAdminListItem & {
  email?: string | null
  phone?: string | null
}

export const userAdminApi = {
  adminPage(params: {
    page?: number
    size?: number
    keyword?: string | null
    role?: number | null
    status?: number | null
  }) {
    return getJson<PageResult<UserAdminListItem>>('/admin/user/page', { params })
  },

  adminDetail(id: number) {
    return getJson<UserAdminDetail>(`/admin/user/${id}`)
  },

  updateStatus(id: number, status: number) {
    return putJson<void, { status: number }>(`/admin/user/${id}/status`, { status })
  },
}

