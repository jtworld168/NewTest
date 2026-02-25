const api = require('../../utils/api')

Page({
  data: {
    order: null,
    orderItems: [],
    payment: null,
    loading: true,
    isEmployeeOrder: false,
    originalTotal: '0.00',
    discountAmount: '0.00'
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

      // Load store info
      let store = null
      if (order.storeId) {
        try {
          const sRes = await api.getStoreById(order.storeId)
          store = sRes.data
        } catch (e) {}
      }
      order._storeName = store ? store.name : ''

      // Load order items (multi-product)
      let orderItems = []
      try {
        const itemsRes = await api.getOrderItemsByOrderId(orderId)
        const rawItems = itemsRes.data || []
        for (let i = 0; i < rawItems.length; i++) {
          const item = rawItems[i]
          let product = null
          try {
            const pRes = await api.getProductById(item.productId)
            product = pRes.data
          } catch (e) {}
          item._productName = product ? product.name : '商品 #' + item.productId
          item._imageUrl = product && product.image ? api.getFileUrl(product.image) : ''
          item._barcode = product ? (product.barcode || '') : ''
          item._subtotalDisplay = item.subtotal ? Number(item.subtotal).toFixed(2) : '0.00'
          item._priceDisplay = item.priceAtPurchase ? Number(item.priceAtPurchase).toFixed(2) : '0.00'
          // Detect employee discount: compare as cents to avoid floating-point issues
          if (product && item.priceAtPurchase) {
            const purchaseCents = Math.round(Number(item.priceAtPurchase) * 100)
            const priceCents = Math.round(Number(product.price) * 100)
            if (purchaseCents < priceCents) {
              item._hasEmployeeDiscount = true
              item._originalPrice = Number(product.price).toFixed(2)
            }
          }
          orderItems.push(item)
        }
      } catch (e) {
        console.error('Failed to load order items:', e)
      }

      // If no order items found, fall back to single-product display
      if (orderItems.length === 0 && order.productId) {
        let product = null
        try {
          const pRes = await api.getProductById(order.productId)
          product = pRes.data
        } catch (e) {}
        orderItems.push({
          productId: order.productId,
          quantity: order.quantity || 1,
          priceAtPurchase: order.priceAtPurchase,
          subtotal: order.totalAmount,
          _productName: product ? product.name : '商品 #' + order.productId,
          _imageUrl: product && product.image ? api.getFileUrl(product.image) : '',
          _barcode: product ? (product.barcode || '') : '',
          _subtotalDisplay: order.totalAmount ? Number(order.totalAmount).toFixed(2) : '0.00',
          _priceDisplay: order.priceAtPurchase ? Number(order.priceAtPurchase).toFixed(2) : '0.00'
        })
      }

      order._totalDisplay = order.totalAmount ? Number(order.totalAmount).toFixed(2) : '0.00'

      // Calculate employee discount info
      let isEmployeeOrder = orderItems.some(item => item._hasEmployeeDiscount)
      let originalTotal = 0
      orderItems.forEach(item => {
        if (item._hasEmployeeDiscount) {
          originalTotal += Number(item._originalPrice) * (item.quantity || 1)
        } else {
          originalTotal += Number(item.priceAtPurchase || 0) * (item.quantity || 1)
        }
      })
      const discountAmount = isEmployeeOrder ? (originalTotal - Number(order.totalAmount || 0)) : 0

      // Load payment info
      let payment = null
      try {
        const payRes = await api.getPaymentsByOrderId(orderId)
        const payments = payRes.data || []
        if (payments.length > 0) {
          payment = payments[0]
        }
      } catch (e) {}

      this.setData({ order, orderItems, payment, loading: false, isEmployeeOrder, originalTotal: originalTotal.toFixed(2), discountAmount: discountAmount.toFixed(2) })
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
