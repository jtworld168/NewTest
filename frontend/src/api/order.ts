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

export function getOrdersByUserCouponId(userCouponId: number): Promise<Result<Order[]>> {
  return request.get(`/orders/getByUserCouponId/${userCouponId}`)
}

export function addOrder(params: { userId: number; productId: number; quantity: number; userCouponId?: number }): Promise<Result<void>> {
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

export function listOrdersPage(pageNum: number, pageSize: number, storeId?: number): Promise<Result<any>> {
  const params: any = { pageNum, pageSize }
  if (storeId) params.storeId = storeId
  return request.get('/orders/listPage', { params })
}

export function getOrdersByStoreId(storeId: number): Promise<Result<Order[]>> {
  return request.get(`/orders/getByStoreId/${storeId}`)
}

export function addMultiItemOrder(data: { userId: number; storeId?: number; items: { productId: number; quantity: number }[]; userCouponId?: number }): Promise<Result<Order>> {
  return request.post('/orders/addMultiItem', data)
}
