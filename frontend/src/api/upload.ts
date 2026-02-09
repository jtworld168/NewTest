import request from '../utils/request'
import type { Result } from '../types'

export function uploadImage(file: File): Promise<Result<string>> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function listFiles(): Promise<Result<string[]>> {
  return request.get('/api/file/list')
}

export function downloadFile(filename: string): void {
  const safeName = filename.replace(/[/\\]/g, '').replace(/\.\./g, '')
  if (!safeName) return
  const url = `/api/file/download/${encodeURIComponent(safeName)}`
  const link = document.createElement('a')
  link.href = url
  link.download = safeName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
