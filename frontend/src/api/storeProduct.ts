import request from './request'

export const listStoreProducts = () => request.get('/store-products/list')
export const getStoreProductById = (id: number) => request.get(`/store-products/get/${id}`)
export const getStoreProductsByStoreId = (storeId: number) => request.get(`/store-products/getByStoreId/${storeId}`)
export const getStoreProductsByProductId = (productId: number) => request.get(`/store-products/getByProductId/${productId}`)
export const listStoreProductsPage = (pageNum: number, pageSize: number, storeId?: number) =>
  request.get('/store-products/listPage', { params: { pageNum, pageSize, storeId } })
export const addStoreProduct = (data: any) => request.post('/store-products/add', data)
export const updateStoreProduct = (data: any) => request.put('/store-products/update', data)
export const deleteStoreProduct = (id: number) => request.delete(`/store-products/delete/${id}`)
export const deleteBatchStoreProducts = (ids: number[]) => request.delete('/store-products/deleteBatch', { data: ids })
