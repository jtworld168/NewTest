import request from './request'
import type { Result, Coupon } from '../types'

export function getCouponById(id: number): Promise<Result<Coupon>> {
  return request.get(`/coupons/get/${id}`)
}

export function listCoupons(): Promise<Result<Coupon[]>> {
  return request.get('/coupons/list')
}

export function getCouponsByStatus(status: string): Promise<Result<Coupon[]>> {
  return request.get(`/coupons/getByStatus/${status}`)
}

export function getCouponByName(name: string): Promise<Result<Coupon>> {
  return request.get(`/coupons/getByName/${name}`)
}

export function addCoupon(data: Coupon): Promise<Result<void>> {
  return request.post('/coupons/add', data)
}

export function updateCoupon(data: Coupon): Promise<Result<void>> {
  return request.put('/coupons/update', data)
}

export function deleteCoupon(id: number): Promise<Result<void>> {
  return request.delete(`/coupons/delete/${id}`)
}

export function deleteBatchCoupons(ids: number[]): Promise<Result<void>> {
  return request.delete('/coupons/deleteBatch', { data: ids })
}
