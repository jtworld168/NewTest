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

      // Load available coupons
      let availableCoupons = []
      try {
        const couponRes = await api.getUserCouponsByUserIdAndStatus(userInfo.id, 'AVAILABLE')
        const userCoupons = couponRes.data || []
        // Load coupon template details
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

          // Calculate employee discount price
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

    // If employee, use employee total; otherwise use original
    let basePrice = isEmployee && hasEmployeeDiscount ? employeeTotal : originalTotal

    // Apply coupon (only if NOT employee — they can't stack)
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
    const { cartItems, selectedIds, selectedCouponId, finalPrice, isEmployee } = this.data
    const selectedItems = cartItems.filter(item => selectedIds.includes(item.id))
    if (selectedItems.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' })
      return
    }

    let confirmMsg = '共 ' + selectedItems.length + ' 件商品，合计 ¥' + finalPrice
    if (isEmployee && this.data.hasEmployeeDiscount) {
      confirmMsg += '\n（已享受员工折扣）'
    }
    if (selectedCouponId && !isEmployee) {
      confirmMsg += '\n（已使用优惠券 -¥' + this.data.selectedCouponDiscount + '）'
    }

    wx.showModal({
      title: '确认下单',
      content: confirmMsg,
      success: async (res) => {
        if (res.confirm) {
          try {
            // Only pass coupon for non-employee users
            const couponToUse = (!isEmployee && selectedCouponId) ? selectedCouponId : null
            for (const item of selectedItems) {
              await api.addOrder(app.globalData.userInfo.id, item.productId, item.quantity, couponToUse)
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

  goShopping() {
    wx.switchTab({ url: '/pages/index/index' })
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  getFileUrl(filename) {
    return api.getFileUrl(filename)
  }
})
