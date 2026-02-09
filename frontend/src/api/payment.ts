import request from './request'
import type { Result, Payment } from '../types'

export function getPaymentById(id: number): Promise<Result<Payment>> {
  return request.get(`/payments/get/${id}`)
}

export function listPayments(): Promise<Result<Payment[]>> {
  return request.get('/payments/list')
}

export function getPaymentByOrderId(orderId: number): Promise<Result<Payment>> {
  return request.get(`/payments/getByOrderId/${orderId}`)
}

export function getPaymentsByStatus(status: string): Promise<Result<Payment[]>> {
  return request.get(`/payments/getByStatus/${status}`)
}

export function getPaymentByTransactionNo(transactionNo: string): Promise<Result<Payment>> {
  return request.get(`/payments/getByTransactionNo/${transactionNo}`)
}

export function searchPayments(keyword: string): Promise<Result<Payment[]>> {
  return request.get('/payments/search', { params: { keyword } })
}

export function addPayment(data: Payment): Promise<Result<void>> {
  return request.post('/payments/add', data)
}

export function updatePayment(data: Payment): Promise<Result<void>> {
  return request.put('/payments/update', data)
}

export function deletePayment(id: number): Promise<Result<void>> {
  return request.delete(`/payments/delete/${id}`)
}

export function deleteBatchPayments(ids: number[]): Promise<Result<void>> {
  return request.delete('/payments/deleteBatch', { data: ids })
}
