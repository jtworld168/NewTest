import axios from 'axios'
import { BASE_URL } from './request'

function downloadFile(url: string, filename: string) {
  const token = localStorage.getItem('satoken')
  axios.get(url, {
    responseType: 'blob',
    headers: token ? { satoken: token } : {}
  }).then(res => {
    const blob = new Blob([res.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = filename
    link.click()
    URL.revokeObjectURL(link.href)
  })
}

export function exportProducts() {
  downloadFile(BASE_URL + '/api/excel/export/products', '商品数据.xlsx')
}

export function exportUsers() {
  downloadFile(BASE_URL + '/api/excel/export/users', '用户数据.xlsx')
}
