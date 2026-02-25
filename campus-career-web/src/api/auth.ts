import { postJson } from '@/api/http'

export type LoginReq = {
  username: string
  password: string
}

export type LoginResp = {
  token: string
  expiresIn: number
}

export const authApi = {
  login(req: LoginReq) {
    // 后端接口：POST /auth/login
    return postJson<LoginResp, LoginReq>('/auth/login', req)
  },
}


