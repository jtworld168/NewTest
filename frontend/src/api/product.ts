import request from './request'
import type { Result, Product } from '../types'

export function getProductById(id: number): Promise<Result<Product>> {
  return request.get(`/products/get/${id}`)
}

export function listProducts(): Promise<Result<Product[]>> {
  return request.get('/products/list')
}

export function searchProducts(keyword: string): Promise<Result<Product[]>> {
  return request.get('/products/searchAll', { params: { keyword } })
}

export function getProductByName(name: string): Promise<Result<Product>> {
  return request.get(`/products/getByName/${name}`)
}

export function getProductsByCategoryId(categoryId: number): Promise<Result<Product[]>> {
  return request.get(`/products/getByCategoryId/${categoryId}`)
}

export function addProduct(data: Product): Promise<Result<void>> {
  return request.post('/products/add', data)
}

export function updateProduct(data: Product): Promise<Result<void>> {
  return request.put('/products/update', data)
}

export function deleteProduct(id: number): Promise<Result<void>> {
  return request.delete(`/products/delete/${id}`)
}

export function deleteBatchProducts(ids: number[]): Promise<Result<void>> {
  return request.delete('/products/deleteBatch', { data: ids })
}
