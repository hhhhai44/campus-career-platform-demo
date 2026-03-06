import { postJson } from '@/api/http'

export type LoginReq = {
  username: string
  password: string
}

export type LoginResp = {
  token: string
  expiresIn: number
}

export type RegisterReq = {
  username: string
  password: string
  /** 确认密码，需与 password 一致 */
  confirmPassword: string
  /** 真实姓名，可选 */
  realName?: string
  /** 学号，可选 */
  studentNo?: string
  /** 邮箱，可选 */
  email?: string
  /** 手机号，可选 */
  phone?: string
  /** 昵称，可选，后端若需要可扩展 */
  nickname?: string
}

export const authApi = {
  login(req: LoginReq) {
    return postJson<LoginResp, LoginReq>('/auth/login', req)
  },
  register(req: RegisterReq) {
    return postJson<void, RegisterReq>('/auth/register', req)
  },
}
