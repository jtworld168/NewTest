const api = require('../../utils/api')

Page({
  data: {
    order: null,
    payment: null,
    loading: true
  },

  onLoad(options) {
    if (options.id) {
      this.loadOrderDetail(Number(options.id))
    }
  },

  async loadOrderDetail(orderId) {
    this.setData({ loading: true })
    try {
      const res = await api.getOrderById(orderId)
      const order = res.data
      if (!order) {
        wx.showToast({ title: '订单不存在', icon: 'none' })
        this.setData({ loading: false })
        return
      }

      // Load product info
      let product = null
      if (order.productId) {
        try {
          const pRes = await api.getProductById(order.productId)
          product = pRes.data
        } catch (e) {}
      }

      order._productName = product ? product.name : '商品 #' + order.productId
      order._imageUrl = product && product.image ? api.getFileUrl(product.image) : ''
      order._barcode = product ? product.barcode : ''
      order._hasEmployeeDiscount = product && order.priceAtPurchase && order.priceAtPurchase < product.price
      order._originalPrice = product ? (product.price * order.quantity).toFixed(2) : ''
      order._discountAmount = order._hasEmployeeDiscount
        ? ((product.price * order.quantity) - (order.priceAtPurchase * order.quantity)).toFixed(2)
        : '0.00'
      order._couponDiscount = order.userCouponId ? ((order.priceAtPurchase * order.quantity) - order.totalAmount).toFixed(2) : '0.00'

      // Load payment info
      let payment = null
      try {
        const payRes = await api.getPaymentsByOrderId(orderId)
        const payments = payRes.data || []
        if (payments.length > 0) {
          payment = payments[0]
        }
      } catch (e) {}

      this.setData({ order, payment, loading: false })
    } catch (e) {
      console.error('Failed to load order detail:', e)
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'error' })
    }
  },

  goToPay() {
    const { order } = this.data
    wx.navigateTo({
      url: '/pages/payment/payment?orderId=' + order.id + '&amount=' + order.totalAmount
    })
  },

  cancelOrder() {
    const { order } = this.data
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.updateOrder({ id: order.id, status: 'CANCELLED' })
            wx.showToast({ title: '已取消', icon: 'success' })
            this.loadOrderDetail(order.id)
          } catch (err) {
            wx.showToast({ title: '取消失败', icon: 'error' })
          }
        }
      }
    })
  },

  confirmReceive() {
    const { order } = this.data
    wx.showModal({
      title: '提示',
      content: '确认已收到商品？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.updateOrder({ id: order.id, status: 'COMPLETED' })
            wx.showToast({ title: '已确认收货', icon: 'success' })
            this.loadOrderDetail(order.id)
          } catch (err) {
            wx.showToast({ title: '操作失败', icon: 'error' })
          }
        }
      }
    })
  }
})
