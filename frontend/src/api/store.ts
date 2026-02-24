import request from './request'

export const listStores = () => request.get('/stores/list')
export const getStoreById = (id: number) => request.get(`/stores/get/${id}`)
export const searchStores = (keyword: string) => request.get('/stores/search', { params: { keyword } })
export const listStoresPage = (pageNum: number, pageSize: number) =>
  request.get('/stores/listPage', { params: { pageNum, pageSize } })
export const addStore = (data: any) => request.post('/stores/add', data)
export const updateStore = (data: any) => request.put('/stores/update', data)
export const deleteStore = (id: number) => request.delete(`/stores/delete/${id}`)
export const deleteBatchStores = (ids: number[]) => request.delete('/stores/deleteBatch', { data: ids })
export const getLowStockProducts = (storeId: number) => request.get(`/stores/lowStock/${storeId}`)
