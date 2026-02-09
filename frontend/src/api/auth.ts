import request from './request'
import type { Result, LoginParam, User } from '../types'

export function login(data: LoginParam): Promise<Result<User>> {
  return request.post('/auth/login', data)
}

export function logout(): Promise<Result<void>> {
  return request.post('/auth/logout')
}
