const api = require('../../utils/api')

const STATUS_MAP = {
  'PENDING': { text: '待支付', color: '#ff9800' },
  'PAID': { text: '已支付', color: '#07c160' },
  'COMPLETED': { text: '已完成', color: '#999' },
  'CANCELLED': { text: '已取消', color: '#ee0a24' }
}

Page({
  data: {
    orders: [],
    productMap: {},
    loading: true,
    currentTab: 'all',
    tabs: [
      { key: 'all', name: '全部' },
      { key: 'PENDING', name: '待支付' },
      { key: 'PAID', name: '已支付' },
      { key: 'COMPLETED', name: '已完成' },
      { key: 'CANCELLED', name: '已取消' }
    ]
  },

  onShow() {
    this.loadOrders()
  },

  onPullDownRefresh() {
    this.loadOrders().then(() => wx.stopPullDownRefresh())
  },

  async loadOrders() {
    const app = getApp()
    if (!app.globalData.userInfo) {
      this.setData({ loading: false, orders: [] })
      return
    }
    this.setData({ loading: true })
    try {
      const res = await api.getOrdersByUserId(app.globalData.userInfo.id)
      const orders = res.data || []

      // Load product names
      const productIds = [...new Set(orders.map(o => o.productId).filter(Boolean))]
      const productMap = {}
      await Promise.all(productIds.map(id =>
        api.getProductById(id).then(pRes => {
          if (pRes.data) productMap[id] = pRes.data
        }).catch(() => {})
      ))

      this.setData({ orders, productMap, loading: false })
    } catch (e) {
      console.error('Failed to load orders:', e)
      this.setData({ loading: false })
    }
  },

  switchTab(e) {
    this.setData({ currentTab: e.currentTarget.dataset.tab })
  },

  getFilteredOrders() {
    const { orders, currentTab } = this.data
    if (currentTab === 'all') return orders
    return orders.filter(o => o.status === currentTab)
  },

  getStatusInfo(status) {
    return STATUS_MAP[status] || { text: status, color: '#999' }
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  getFileUrl(filename) {
    return api.getFileUrl(filename)
  }
})
