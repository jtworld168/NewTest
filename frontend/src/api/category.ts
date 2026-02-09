import request from './request'
import type { Result, Category } from '../types'

export function getCategoryById(id: number): Promise<Result<Category>> {
  return request.get(`/categories/get/${id}`)
}

export function listCategories(): Promise<Result<Category[]>> {
  return request.get('/categories/list')
}

export function getCategoryByName(name: string): Promise<Result<Category>> {
  return request.get(`/categories/getByName/${name}`)
}

export function addCategory(data: Category): Promise<Result<void>> {
  return request.post('/categories/add', data)
}

export function updateCategory(data: Category): Promise<Result<void>> {
  return request.put('/categories/update', data)
}

export function deleteCategory(id: number): Promise<Result<void>> {
  return request.delete(`/categories/delete/${id}`)
}

export function deleteBatchCategories(ids: number[]): Promise<Result<void>> {
  return request.delete('/categories/deleteBatch', { data: ids })
}
