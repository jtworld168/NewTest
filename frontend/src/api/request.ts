import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

export const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

request.interceptors.request.use((config) => {
  // Safeguard: prevent [object Object] in URLs
  if (config.url && config.url.includes('[object')) {
    console.error('[API] Invalid URL detected:', config.url)
    return Promise.reject(new Error('Invalid API URL: object was used where string expected'))
  }
  const token = localStorage.getItem('satoken')
  if (token) {
    config.headers['satoken'] = token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    // Store token from response header if present
    const token = response.headers['satoken']
    if (token) {
      localStorage.setItem('satoken', token)
    }
    const res = response.data
    if (res.code === 401) {
      localStorage.removeItem('satoken')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(new Error('未登录'))
    }
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('satoken')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
