import { BASE_URL } from './request'

export function getExportProductsUrl(): string {
  return BASE_URL + '/api/excel/export/products'
}

export function getExportUsersUrl(): string {
  return BASE_URL + '/api/excel/export/users'
}
