import request from './request'
import type { Result, CartItem } from '../types'

export function getCartItemById(id: number): Promise<Result<CartItem>> {
  return request.get(`/cart-items/get/${id}`)
}

export function listCartItems(): Promise<Result<CartItem[]>> {
  return request.get('/cart-items/list')
}

export function getCartItemsByUserId(userId: number): Promise<Result<CartItem[]>> {
  return request.get(`/cart-items/getByUserId/${userId}`)
}

export function getCartItemsByProductId(productId: number): Promise<Result<CartItem[]>> {
  return request.get(`/cart-items/getByProductId/${productId}`)
}

export function addCartItem(data: CartItem): Promise<Result<void>> {
  return request.post('/cart-items/add', data)
}

export function updateCartItem(data: CartItem): Promise<Result<void>> {
  return request.put('/cart-items/update', data)
}

export function deleteCartItem(id: number): Promise<Result<void>> {
  return request.delete(`/cart-items/delete/${id}`)
}

export function deleteBatchCartItems(ids: number[]): Promise<Result<void>> {
  return request.delete('/cart-items/deleteBatch', { data: ids })
}

export function clearCart(userId: number): Promise<Result<void>> {
  return request.delete(`/cart-items/clear/${userId}`)
}
