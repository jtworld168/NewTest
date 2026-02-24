const api = require('../../utils/api')

Page({
  data: {
    cartItems: [],
    productMap: {},
    loading: true,
    totalPrice: 0,
    selectedIds: [],
    // Employee discount
    isEmployee: false,
    employeeTotal: 0,
    hasEmployeeDiscount: false,
    // Coupon
    availableCoupons: [],
    selectedCouponId: null,
    selectedCouponDiscount: 0,
    showCouponPicker: false,
    // Final price
    finalPrice: 0
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
      const userInfo = app.globalData.userInfo
      const isEmployee = Boolean(userInfo.isHotelEmployee)

      const res = await api.getCartByUserId(userInfo.id)
      const rawCartItems = res.data || []

      // Load product details for each cart item
      const productMap = {}
      const productPromises = rawCartItems.map(item =>
        api.getProductById(item.productId).then(pRes => {
          if (pRes.data) productMap[item.productId] = pRes.data
        }).catch(() => {})
      )
      await Promise.all(productPromises)

      // Pre-calculate display values for each cart item (avoid WXML expressions)
      const cartItems = rawCartItems.map(item => {
        const product = productMap[item.productId]
        const price = product ? product.price : 0
        const hasDiscount = isEmployee && product && product.employeeDiscountRate
        const effectivePrice = hasDiscount ? (price * product.employeeDiscountRate) : price
        return {
          ...item,
          _productName: product ? product.name : '商品',
          _price: price.toFixed(2),
          _employeePrice: hasDiscount ? (price * product.employeeDiscountRate).toFixed(2) : '',
          _subtotal: (effectivePrice * item.quantity).toFixed(2),
          _imageUrl: product && product.image ? api.getFileUrl(product.image) : ''
        }
      })

      // Select all by default
      const selectedIds = cartItems.map(item => item.id)

      // Load available coupons
      let availableCoupons = []
      try {
        const couponRes = await api.getUserCouponsByUserIdAndStatus(userInfo.id, 'AVAILABLE')
        const userCoupons = couponRes.data || []
        const couponIds = [...new Set(userCoupons.map(uc => uc.couponId).filter(Boolean))]
        const couponMap = {}
        await Promise.all(couponIds.map(id =>
          api.getCouponById(id).then(cRes => {
            if (cRes.data) couponMap[id] = cRes.data
          }).catch(() => {})
        ))
        availableCoupons = userCoupons.map(uc => ({
          ...uc,
          couponName: couponMap[uc.couponId] ? couponMap[uc.couponId].name : '优惠券',
          discount: couponMap[uc.couponId] ? couponMap[uc.couponId].discount : 0,
          minAmount: couponMap[uc.couponId] ? couponMap[uc.couponId].minAmount : 0
        }))
      } catch (e) {
        console.error('Failed to load coupons:', e)
      }

      this.setData({
        cartItems, productMap, selectedIds, isEmployee,
        availableCoupons, loading: false,
        selectedCouponId: null, selectedCouponDiscount: 0
      })
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
    const { cartItems, productMap, selectedIds, isEmployee, selectedCouponId, availableCoupons } = this.data
    let originalTotal = 0
    let employeeTotal = 0
    let hasEmployeeDiscount = false

    cartItems.forEach(item => {
      if (selectedIds.includes(item.id)) {
        const product = productMap[item.productId]
        if (product) {
          const itemOriginal = product.price * item.quantity
          originalTotal += itemOriginal

          if (isEmployee && product.employeeDiscountRate) {
            const discountedPrice = product.price * product.employeeDiscountRate
            employeeTotal += discountedPrice * item.quantity
            hasEmployeeDiscount = true
          } else {
            employeeTotal += itemOriginal
          }
        }
      }
    })

    let basePrice = isEmployee && hasEmployeeDiscount ? employeeTotal : originalTotal

    // Apply coupon (only if NOT employee)
    let couponDiscount = 0
    if (!isEmployee && selectedCouponId) {
      const coupon = availableCoupons.find(c => c.id === selectedCouponId)
      if (coupon && originalTotal >= coupon.minAmount) {
        couponDiscount = coupon.discount
      }
    }

    const finalPrice = Math.max(0, basePrice - couponDiscount)

    this.setData({
      totalPrice: originalTotal.toFixed(2),
      employeeTotal: employeeTotal.toFixed(2),
      hasEmployeeDiscount,
      selectedCouponDiscount: couponDiscount,
      finalPrice: finalPrice.toFixed(2)
    })
  },

  toggleCouponPicker() {
    if (this.data.isEmployee) {
      wx.showToast({ title: '员工折扣与优惠券不可叠加', icon: 'none' })
      return
    }
    this.setData({ showCouponPicker: !this.data.showCouponPicker })
  },

  selectCoupon(e) {
    const couponId = e.currentTarget.dataset.id
    if (this.data.selectedCouponId === couponId) {
      this.setData({ selectedCouponId: null, showCouponPicker: false })
    } else {
      this.setData({ selectedCouponId: couponId, showCouponPicker: false })
    }
    this.calculateTotal()
  },

  clearCoupon() {
    this.setData({ selectedCouponId: null, showCouponPicker: false })
    this.calculateTotal()
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
      await api.updateCartItem({ id: item.id, userId: item.userId, productId: item.productId, quantity: newQuantity })
      this.loadCart()
    } catch (err) {
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
          } catch (err) {
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
          } catch (err) {
            wx.showToast({ title: '清空失败', icon: 'error' })
          }
        }
      }
    })
  },

  checkout() {
    const app = getApp()
    if (!app.globalData.userInfo) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    const { cartItems, productMap, selectedIds, selectedCouponId, finalPrice, isEmployee, availableCoupons } = this.data
    const selectedItems = cartItems.filter(item => selectedIds.includes(item.id))
    if (selectedItems.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' })
      return
    }

    // Check if any coupon meets minimum spend but none selected
    const totalForCoupon = parseFloat(this.data.totalPrice)
    if (!isEmployee && !selectedCouponId && availableCoupons.length > 0) {
      const usable = availableCoupons.filter(c => totalForCoupon >= c.minAmount)
      if (usable.length > 0) {
        wx.showModal({
          title: '提示',
          content: '您有' + usable.length + '张可用优惠券，是否使用？',
          confirmText: '去选券',
          cancelText: '不使用',
          success: (res) => {
            if (res.confirm) {
              this.setData({ showCouponPicker: true })
            } else {
              this.doCheckout(selectedItems, null)
            }
          }
        })
        return
      }
    }

    this.doCheckout(selectedItems, (!isEmployee && selectedCouponId) ? selectedCouponId : null)
  },

  async doCheckout(selectedItems, couponId) {
    const app = getApp()
    try {
      wx.showLoading({ title: '创建订单中...' })

      // Build multi-item order: one order with all selected products
      const items = selectedItems.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))

      const res = await api.addMultiItemOrder(app.globalData.userInfo.id, items, couponId, app.globalData.selectedStoreId || null)
      const order = res.data

      // Delete cart items after order creation
      for (const item of selectedItems) {
        await api.deleteCartItem(item.id)
      }

      wx.hideLoading()

      // Coupon is now LOCKED by backend; it will be USED when order is paid, or AVAILABLE if cancelled

      // Navigate to payment page with the order
      if (order && order.id) {
        wx.navigateTo({
          url: '/pages/payment/payment?orderId=' + order.id + '&amount=' + this.data.finalPrice
        })
      } else {
        wx.showToast({ title: '下单成功', icon: 'success' })
        setTimeout(() => {
          this.loadCart()
          wx.switchTab({ url: '/pages/orders/orders' })
        }, 1500)
      }
    } catch (err) {
      wx.hideLoading()
      wx.showToast({ title: '下单失败', icon: 'error' })
    }
  },

  goShopping() {
    wx.switchTab({ url: '/pages/index/index' })
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  }
})
