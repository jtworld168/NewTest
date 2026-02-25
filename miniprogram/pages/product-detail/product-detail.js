const api = require('../../utils/api')

Page({
  data: {
    product: null,
    loading: true
  },

  onLoad(options) {
    if (options.id) {
      this.loadProduct(options.id)
    } else {
      this.setData({ loading: false })
    }
  },

  async loadProduct(id) {
    this.setData({ loading: true })
    try {
      const res = await api.getProductById(id)
      const product = res.data
      if (product) {
        const app = getApp()
        const isEmployee = app.globalData.userInfo && Boolean(app.globalData.userInfo.isHotelEmployee)

        product._imageUrl = product.image ? api.getFileUrl(product.image) : ''
        if (isEmployee && product.employeeDiscountRate) {
          product._isEmployee = true
          product._displayPrice = (product.price * product.employeeDiscountRate).toFixed(2)
        } else {
          product._isEmployee = false
          product._displayPrice = product.price ? product.price.toFixed(2) : '0.00'
        }
        // Load category name
        if (product.categoryId) {
          product.categoryName = product.categoryName || ''
        }
        this.setData({ product, loading: false })
      } else {
        this.setData({ product: null, loading: false })
      }
    } catch (e) {
      console.error('Failed to load product:', e)
      this.setData({ loading: false })
    }
  },

  async addToCart() {
    const app = getApp()
    if (!app.globalData.userInfo) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    try {
      await api.addCartItem({
        userId: app.globalData.userInfo.id,
        productId: this.data.product.id,
        quantity: 1
      })
      wx.showToast({ title: '已加入购物车', icon: 'success' })
    } catch (err) {
      wx.showToast({ title: '添加失败', icon: 'error' })
    }
  },

  async buyNow() {
    const app = getApp()
    if (!app.globalData.userInfo) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    const storeId = app.globalData.selectedStoreId
    if (!storeId) {
      wx.showToast({ title: '请先在首页选择店铺', icon: 'none' })
      return
    }
    try {
      const items = [{ productId: this.data.product.id, quantity: 1 }]
      const res = await api.addMultiItemOrder(app.globalData.userInfo.id, items, null, storeId)
      if (res.data && res.data.id) {
        wx.navigateTo({
          url: '/pages/payment/payment?orderId=' + res.data.id + '&amount=' + res.data.totalAmount
        })
      } else {
        wx.showToast({ title: '下单成功', icon: 'success' })
        setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 1500)
      }
    } catch (err) {
      wx.showToast({ title: '下单失败', icon: 'error' })
    }
  }
})
