// API Response wrapper
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

// Enums as const objects + type unions
export const UserRole = { ADMIN: 'ADMIN', EMPLOYEE: 'EMPLOYEE', CUSTOMER: 'CUSTOMER' } as const
export type UserRole = (typeof UserRole)[keyof typeof UserRole]

export const OrderStatus = { PENDING: 'PENDING', PAID: 'PAID', COMPLETED: 'COMPLETED', CANCELLED: 'CANCELLED' } as const
export type OrderStatus = (typeof OrderStatus)[keyof typeof OrderStatus]

export const PaymentStatus = { PENDING: 'PENDING', SUCCESS: 'SUCCESS', FAILED: 'FAILED', REFUNDED: 'REFUNDED' } as const
export type PaymentStatus = (typeof PaymentStatus)[keyof typeof PaymentStatus]

export const PaymentMethod = { WECHAT: 'WECHAT', ALIPAY: 'ALIPAY', CASH: 'CASH', CARD: 'CARD' } as const
export type PaymentMethod = (typeof PaymentMethod)[keyof typeof PaymentMethod]

export const CouponStatus = { AVAILABLE: 'AVAILABLE', USED: 'USED', EXPIRED: 'EXPIRED' } as const
export type CouponStatus = (typeof CouponStatus)[keyof typeof CouponStatus]

// Entities
export interface User {
  id?: number
  username: string
  password?: string
  phone?: string
  role: UserRole
  isHotelEmployee?: boolean
  avatar?: string
  createTime?: string
  updateTime?: string
  deleted?: number
}

export interface Category {
  id?: number
  name: string
  description?: string
  createTime?: string
  updateTime?: string
  deleted?: number
}

export interface Product {
  id?: number
  name: string
  price: number
  stock: number
  description?: string
  categoryId?: number
  employeeDiscountRate?: number
  barcode?: string
  image?: string
  status?: number
  stockAlertThreshold?: number
  createTime?: string
  updateTime?: string
  deleted?: number
}

export interface Order {
  id?: number
  userId: number
  productId: number
  quantity: number
  priceAtPurchase?: number
  totalAmount?: number
  userCouponId?: number
  status: OrderStatus
  createTime?: string
  updateTime?: string
  deleted?: number
}

export interface OrderItem {
  id?: number
  orderId: number
  productId: number
  quantity: number
  priceAtPurchase: number
  subtotal?: number
  createTime?: string
  updateTime?: string
  deleted?: number
}

export interface Payment {
  id?: number
  orderId: number
  amount: number
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  paymentTime?: string
  transactionNo?: string
  createTime?: string
  updateTime?: string
  deleted?: number
}

export interface Coupon {
  id?: number
  name: string
  discount: number
  minAmount: number
  totalCount: number
  remainingCount: number
  startTime: string
  endTime: string
  createTime?: string
  updateTime?: string
  deleted?: number
}

export interface UserCoupon {
  id?: number
  userId: number
  couponId: number
  status: CouponStatus
  useTime?: string
  createTime?: string
  updateTime?: string
  deleted?: number
}

export interface CartItem {
  id?: number
  userId: number
  productId: number
  quantity: number
  createTime?: string
  updateTime?: string
  deleted?: number
}

export interface LoginParam {
  username: string
  password: string
}
