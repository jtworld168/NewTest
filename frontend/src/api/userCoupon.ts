import request from './request'
import type { Result, UserCoupon } from '../types'

export function getUserCouponById(id: number): Promise<Result<UserCoupon>> {
  return request.get(`/user-coupons/get/${id}`)
}

export function listUserCoupons(): Promise<Result<UserCoupon[]>> {
  return request.get('/user-coupons/list')
}

export function getUserCouponsByUserId(userId: number): Promise<Result<UserCoupon[]>> {
  return request.get(`/user-coupons/getByUserId/${userId}`)
}

export function getUserCouponsByCouponId(couponId: number): Promise<Result<UserCoupon[]>> {
  return request.get(`/user-coupons/getByCouponId/${couponId}`)
}

export function getUserCouponsByStatus(status: string): Promise<Result<UserCoupon[]>> {
  return request.get(`/user-coupons/getByStatus/${status}`)
}

export function addUserCoupon(data: UserCoupon): Promise<Result<void>> {
  return request.post('/user-coupons/add', data)
}

export function updateUserCoupon(data: UserCoupon): Promise<Result<void>> {
  return request.put('/user-coupons/update', data)
}

export function deleteUserCoupon(id: number): Promise<Result<void>> {
  return request.delete(`/user-coupons/delete/${id}`)
}

export function deleteBatchUserCoupons(ids: number[]): Promise<Result<void>> {
  return request.delete('/user-coupons/deleteBatch', { data: ids })
}

export function listUserCouponsPage(pageNum: number, pageSize: number): Promise<Result<any>> {
  return request.get('/user-coupons/listPage', { params: { pageNum, pageSize } })
}

export function distributeToAllUsers(couponId: number): Promise<Result<void>> {
  return request.post('/user-coupons/distributeAll', null, { params: { couponId } })
}
