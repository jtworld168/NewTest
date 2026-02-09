const api = require('../../utils/api')

Page({
  data: {
    cartItems: [],
    productMap: {},
    loading: true,
    totalPrice: 0,
    selectedIds: []
  },

  onShow() {
    this.loadCart()
  },

  async loadCart() {
    const app = getApp()
    if (!app.globalData.userInfo) {
      this.setData({ loading: false, cartItems: [] })
      return
    }
    this.setData({ loading: true })
    try {
      const res = await api.getCartByUserId(app.globalData.userInfo.id)
      const cartItems = res.data || []

      // Load product details for each cart item
      const productMap = {}
      const productPromises = cartItems.map(item =>
        api.getProductById(item.productId).then(pRes => {
          if (pRes.data) productMap[item.productId] = pRes.data
        }).catch(() => {})
      )
      await Promise.all(productPromises)

      // Select all by default
      const selectedIds = cartItems.map(item => item.id)

      this.setData({ cartItems, productMap, selectedIds, loading: false })
      this.calculateTotal()
    } catch (e) {
      console.error('Failed to load cart:', e)
      this.setData({ loading: false })
    }
  },

  toggleSelect(e) {
    const id = e.currentTarget.dataset.id
    let { selectedIds } = this.data
    const index = selectedIds.indexOf(id)
    if (index > -1) {
      selectedIds.splice(index, 1)
    } else {
      selectedIds.push(id)
    }
    this.setData({ selectedIds })
    this.calculateTotal()
  },

  toggleSelectAll() {
    const { cartItems, selectedIds } = this.data
    if (selectedIds.length === cartItems.length) {
      this.setData({ selectedIds: [] })
    } else {
      this.setData({ selectedIds: cartItems.map(item => item.id) })
    }
    this.calculateTotal()
  },

  calculateTotal() {
    const { cartItems, productMap, selectedIds } = this.data
    let totalPrice = 0
    cartItems.forEach(item => {
      if (selectedIds.includes(item.id)) {
        const product = productMap[item.productId]
        if (product) {
          totalPrice += product.price * item.quantity
        }
      }
    })
    this.setData({ totalPrice: totalPrice.toFixed(2) })
  },

  async changeQuantity(e) {
    const { id, action } = e.currentTarget.dataset
    const item = this.data.cartItems.find(i => i.id === id)
    if (!item) return

    let newQuantity = item.quantity
    if (action === 'add') {
      newQuantity++
    } else if (action === 'minus') {
      if (newQuantity <= 1) {
        this.deleteItem({ currentTarget: { dataset: { id } } })
        return
      }
      newQuantity--
    }

    try {
      await api.updateCartItem({ ...item, quantity: newQuantity })
      this.loadCart()
    } catch (e) {
      wx.showToast({ title: '更新失败', icon: 'error' })
    }
  },

  deleteItem(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定要移除该商品吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.deleteCartItem(id)
            wx.showToast({ title: '已移除', icon: 'success' })
            this.loadCart()
          } catch (e) {
            wx.showToast({ title: '移除失败', icon: 'error' })
          }
        }
      }
    })
  },

  async clearCart() {
    const app = getApp()
    if (!app.globalData.userInfo) return
    wx.showModal({
      title: '提示',
      content: '确定要清空购物车吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.clearCart(app.globalData.userInfo.id)
            wx.showToast({ title: '已清空', icon: 'success' })
            this.loadCart()
          } catch (e) {
            wx.showToast({ title: '清空失败', icon: 'error' })
          }
        }
      }
    })
  },

  async checkout() {
    const app = getApp()
    if (!app.globalData.userInfo) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    const { cartItems, productMap, selectedIds } = this.data
    const selectedItems = cartItems.filter(item => selectedIds.includes(item.id))
    if (selectedItems.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' })
      return
    }
    wx.showModal({
      title: '确认下单',
      content: '共 ' + selectedItems.length + ' 件商品，合计 ¥' + this.data.totalPrice,
      success: async (res) => {
        if (res.confirm) {
          try {
            for (const item of selectedItems) {
              await api.addOrder(app.globalData.userInfo.id, item.productId, item.quantity)
              await api.deleteCartItem(item.id)
            }
            wx.showToast({ title: '下单成功', icon: 'success' })
            setTimeout(() => {
              this.loadCart()
              wx.switchTab({ url: '/pages/orders/orders' })
            }, 1500)
          } catch (e) {
            wx.showToast({ title: '下单失败', icon: 'error' })
          }
        }
      }
    })
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  getFileUrl(filename) {
    return api.getFileUrl(filename)
  }
})
