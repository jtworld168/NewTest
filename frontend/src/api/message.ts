import request from './request'

export const listMessages = () => request.get('/messages/list')
export const listMessagesPage = (pageNum: number, pageSize: number) =>
  request.get('/messages/listPage', { params: { pageNum, pageSize } })
export const sendMessage = (data: any) => request.post('/messages/send', data)
export const broadcastMessage = (data: any) => request.post('/messages/broadcast', data)
export const getUserMessages = (userId: number) => request.get(`/messages/user/${userId}`)
export const getUnreadCount = (userId: number) => request.get(`/messages/unread/${userId}`)
export const markAsRead = (id: number) => request.put(`/messages/read/${id}`)
export const markAllAsRead = (userId: number) => request.put(`/messages/readAll/${userId}`)
export const deleteMessage = (id: number) => request.delete(`/messages/delete/${id}`)
export const deleteBatchMessages = (ids: number[]) => request.delete('/messages/deleteBatch', { data: ids })
