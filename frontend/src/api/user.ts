import request from './request'
import type { Result, User } from '../types'

export function getUserById(id: number): Promise<Result<User>> {
  return request.get(`/users/get/${id}`)
}

export function listUsers(): Promise<Result<User[]>> {
  return request.get('/users/list')
}

export function getUsersByRole(role: string): Promise<Result<User[]>> {
  return request.get(`/users/getByRole/${role}`)
}

export function getUserByUsername(username: string): Promise<Result<User>> {
  return request.get(`/users/getByUsername/${username}`)
}

export function searchUsers(keyword: string): Promise<Result<User[]>> {
  return request.get('/users/search', { params: { keyword } })
}

export function addUser(data: User): Promise<Result<void>> {
  return request.post('/users/add', data)
}

export function updateUser(data: User): Promise<Result<void>> {
  return request.put('/users/update', data)
}

export function deleteUser(id: number): Promise<Result<void>> {
  return request.delete(`/users/delete/${id}`)
}

export function deleteBatchUsers(ids: number[]): Promise<Result<void>> {
  return request.delete('/users/deleteBatch', { data: ids })
}

export function listUsersPage(pageNum: number, pageSize: number): Promise<Result<any>> {
  return request.get('/users/listPage', { params: { pageNum, pageSize } })
}
