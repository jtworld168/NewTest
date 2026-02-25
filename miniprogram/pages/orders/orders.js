const api = require('../../utils/api')

Page({
  data: {
    orders: [],
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

      // Load product details
      const productIds = [...new Set(orders.map(o => o.productId).filter(Boolean))]
      const productMap = {}
      await Promise.all(productIds.map(id =>
        api.getProductById(id).then(pRes => {
          if (pRes.data) productMap[id] = pRes.data
        }).catch(() => {})
      ))

      // Load store details
      const storeIds = [...new Set(orders.map(o => o.storeId).filter(Boolean))]
      const storeMap = {}
      await Promise.all(storeIds.map(id =>
        api.getStoreById(id).then(sRes => {
          if (sRes.data) storeMap[id] = sRes.data
        }).catch(() => {})
      ))

      // Pre-calculate display values to avoid WXML expression issues
      orders.forEach(order => {
        const product = productMap[order.productId]
        order._productName = product ? product.name : '商品 #' + order.productId
        order._imageUrl = product && product.image ? api.getFileUrl(product.image) : ''
        order._barcode = product ? (product.barcode || '') : ''
        order._hasEmployeeDiscount = product && order.priceAtPurchase && order.priceAtPurchase < product.price
        const store = order.storeId ? storeMap[order.storeId] : null
        order._storeName = store ? store.name : ''
      })

      this.setData({ orders, loading: false })
    } catch (e) {
      console.error('Failed to load orders:', e)
      this.setData({ loading: false })
    }
  },

  switchTab(e) {
    this.setData({ currentTab: e.currentTarget.dataset.tab })
  },

  goToOrderDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/order-detail/order-detail?id=' + id })
  },

  goToPay(e) {
    const id = e.currentTarget.dataset.id
    const amount = e.currentTarget.dataset.amount
    wx.navigateTo({ url: '/pages/payment/payment?orderId=' + id + '&amount=' + amount })
  },

  cancelOrder(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.updateOrder({ id, status: 'CANCELLED' })
            wx.showToast({ title: '已取消', icon: 'success' })
            this.loadOrders()
          } catch (err) {
            wx.showToast({ title: '取消失败', icon: 'error' })
          }
        }
      }
    })
  }
})
