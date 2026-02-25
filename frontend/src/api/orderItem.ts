import request from './request'
import type { Result, OrderItem } from '../types'

export function getOrderItemById(id: number): Promise<Result<OrderItem>> {
  return request.get(`/order-items/get/${id}`)
}

export function listOrderItems(): Promise<Result<OrderItem[]>> {
  return request.get('/order-items/list')
}

export function getOrderItemsByOrderId(orderId: number): Promise<Result<OrderItem[]>> {
  return request.get(`/order-items/getByOrderId/${orderId}`)
}

export function getOrderItemsByProductId(productId: number): Promise<Result<OrderItem[]>> {
  return request.get(`/order-items/getByProductId/${productId}`)
}

export function addOrderItem(data: OrderItem): Promise<Result<void>> {
  return request.post('/order-items/add', data)
}

export function updateOrderItem(data: OrderItem): Promise<Result<void>> {
  return request.put('/order-items/update', data)
}

export function deleteOrderItem(id: number): Promise<Result<void>> {
  return request.delete(`/order-items/delete/${id}`)
}

export function deleteBatchOrderItems(ids: number[]): Promise<Result<void>> {
  return request.delete('/order-items/deleteBatch', { data: ids })
}

export function listOrderItemsPage(pageNum: number, pageSize: number): Promise<Result<any>> {
  return request.get('/order-items/listPage', { params: { pageNum, pageSize } })
}
