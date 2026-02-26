const api = require('../../utils/api')

Page({
  data: {
    messages: [],
    typeMap: {
      'SYSTEM': '系统消息',
      'ORDER': '订单消息',
      'COUPON': '优惠券消息'
    }
  },

  onShow() {
    this.loadMessages()
  },

  async loadMessages() {
    const app = getApp()
    const userInfo = app.globalData.userInfo
    if (!userInfo) return
    try {
      const res = await api.getUserMessages(userInfo.id)
      this.setData({ messages: res.data || [] })
    } catch (e) {
      console.error('Failed to load messages:', e)
    }
  },

  async readMessage(e) {
    const { id, index } = e.currentTarget.dataset
    const msg = this.data.messages[index]
    if (msg && msg.isRead === 0) {
      try {
        await api.markMessageAsRead(id)
        const key = 'messages[' + index + '].isRead'
        this.setData({ [key]: 1 })
      } catch (e) {
        console.error('Failed to mark as read:', e)
      }
    }
  },

  async markAllRead() {
    const app = getApp()
    const userInfo = app.globalData.userInfo
    if (!userInfo) return
    try {
      await api.markAllMessagesAsRead(userInfo.id)
      const messages = this.data.messages.map(m => ({ ...m, isRead: 1 }))
      this.setData({ messages })
      wx.showToast({ title: '全部已读', icon: 'success' })
    } catch (e) {
      console.error('Failed to mark all as read:', e)
    }
  }
})
