import request from './request'

// 按天统计销量
export const getDailySales = (year: number, month: number, storeId?: number) => {
  const params: any = { year, month }
  if (storeId) params.storeId = storeId
  return request.get('/sales/summary/daily', { params })
}

// 按月统计销量
export const getMonthlySales = (year?: number, storeId?: number) => {
  const params: any = {}
  if (year) params.year = year
  if (storeId) params.storeId = storeId
  return request.get('/sales/summary/monthly', { params })
}

// 按年统计销量
export const getYearlySales = (storeId?: number) => {
  const params: any = {}
  if (storeId) params.storeId = storeId
  return request.get('/sales/summary/yearly', { params })
}

// 分类商品数量统计
export const getCategoryProductCount = (storeId?: number) => {
  const params: any = {}
  if (storeId) params.storeId = storeId
  return request.get('/sales/category-products', { params })
}

// 获取销量总额
export const getTotalSales = (storeId?: number) => {
  const params: any = {}
  if (storeId) params.storeId = storeId
  return request.get('/sales/total', { params })
}

// 近7天销售趋势
export const getWeeklySalesTrend = (storeId?: number) => {
  const params: any = {}
  if (storeId) params.storeId = storeId
  return request.get('/sales/trend/weekly', { params })
}

// 用户增长趋势
export const getUserGrowth = () => {
  return request.get('/sales/user-growth')
}

// 热门商品TOP10
export const getTopProducts = (storeId?: number) => {
  const params: any = {}
  if (storeId) params.storeId = storeId
  return request.get('/sales/top-products', { params })
}
