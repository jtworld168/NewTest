import request from '../utils/request'
import type { Result } from '../types'

export function uploadImage(file: File): Promise<Result<string>> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
