import request from './request'
import type { Result, Order } from '../types'

export function getOrderById(id: number): Promise<Result<Order>> {
  return request.get(`/orders/get/${id}`)
}

export function listOrders(): Promise<Result<Order[]>> {
  return request.get('/orders/list')
}

export function getOrdersByUserId(userId: number): Promise<Result<Order[]>> {
  return request.get(`/orders/getByUserId/${userId}`)
}

export function getOrdersByStatus(status: string): Promise<Result<Order[]>> {
  return request.get(`/orders/getByStatus/${status}`)
}

export function getOrdersByProductId(productId: number): Promise<Result<Order[]>> {
  return request.get(`/orders/getByProductId/${productId}`)
}

export function getOrdersByCouponId(couponId: number): Promise<Result<Order[]>> {
  return request.get(`/orders/getByCouponId/${couponId}`)
}

export function addOrder(params: { userId: number; productId: number; quantity: number; couponId?: number }): Promise<Result<void>> {
  return request.post('/orders/add', null, { params })
}

export function updateOrder(data: Order): Promise<Result<void>> {
  return request.put('/orders/update', data)
}

export function deleteOrder(id: number): Promise<Result<void>> {
  return request.delete(`/orders/delete/${id}`)
}

export function deleteBatchOrders(ids: number[]): Promise<Result<void>> {
  return request.delete('/orders/deleteBatch', { data: ids })
}
