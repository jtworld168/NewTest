const api = require('../../utils/api')

Page({
  data: {
    products: [],
    categories: [],
    loading: true,
    cartMap: {}, // productId -> cartItem
    showDropBall: false,
    dropBallX: 0,
    dropBallY: 0,
    dropBallAnimation: null
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const [productRes, categoryRes] = await Promise.all([
        api.getProductList(),
        api.getCategoryList()
      ])

      const products = (productRes.data || []).slice(0, 8)

      // Check employee status
      const app = getApp()
      const isEmployee = app.globalData.userInfo && Boolean(app.globalData.userInfo.isHotelEmployee)

      // Pre-calculate image URLs and employee pricing
      products.forEach(p => {
        p._imageUrl = p.image ? api.getFileUrl(p.image) : ''
        if (isEmployee && p.employeeDiscountRate) {
          p._isEmployee = true
          p._displayPrice = (p.price * p.employeeDiscountRate).toFixed(2)
        } else {
          p._isEmployee = false
          p._displayPrice = p.price.toFixed(2)
        }
      })

      // Load cart items to show quantities
      let cartMap = {}
      if (app.globalData.userInfo) {
        try {
          const cartRes = await api.getCartByUserId(app.globalData.userInfo.id)
          const cartItems = cartRes.data || []
          cartItems.forEach(item => {
            cartMap[item.productId] = item
          })
        } catch (e) {
          console.error('Failed to load cart:', e)
        }
      }

      // Set cart quantity on products
      products.forEach(p => {
        p._cartQty = cartMap[p.id] ? cartMap[p.id].quantity : 0
      })

      this.setData({
        products,
        categories: categoryRes.data || [],
        loading: false,
        cartMap
      })
    } catch (e) {
      console.error('Failed to load data:', e)
      this.setData({ loading: false })
    }
  },

  goToCategory() {
    wx.navigateTo({ url: '/pages/category/category' })
  },

  onScan() {
    wx.scanCode({
      onlyFromCamera: true,
      success: (res) => {
        const code = res.result
        wx.showLoading({ title: '查找商品...' })
        api.searchProducts(code).then(searchRes => {
          wx.hideLoading()
          const products = searchRes.data || []
          if (products.length > 0) {
            const product = products[0]
            wx.showActionSheet({
              itemList: ['加入购物车', '立即购买'],
              success: (actionRes) => {
                if (actionRes.tapIndex === 0) {
                  this.addToCart(product)
                } else if (actionRes.tapIndex === 1) {
                  this.buyNow(product)
                }
              }
            })
          } else {
            wx.showToast({ title: '未找到该商品', icon: 'none' })
          }
        }).catch(() => {
          wx.hideLoading()
          wx.showToast({ title: '查找失败', icon: 'error' })
        })
      },
      fail: () => {}
    })
  },

  goToCategoryDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: '/pages/category/category?categoryId=' + id
    })
  },

  goToProductDetail(e) {
    const id = e.currentTarget.dataset.id
    const product = this.data.products.find(p => p.id === id)
    if (!product) return
    const app = getApp()
    if (!app.globalData.userInfo) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showActionSheet({
      itemList: ['加入购物车', '立即购买'],
      success: (res) => {
        if (res.tapIndex === 0) {
          this.addToCart(product)
        } else if (res.tapIndex === 1) {
          this.buyNow(product)
        }
      }
    })
  },

  async increaseQty(e) {
    const productId = e.currentTarget.dataset.id
    const app = getApp()
    if (!app.globalData.userInfo) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }

    const cartItem = this.data.cartMap[productId]
    try {
      if (cartItem) {
        await api.updateCartItem({ id: cartItem.id, userId: cartItem.userId, productId: cartItem.productId, quantity: cartItem.quantity + 1 })
      } else {
        await api.addCartItem({ userId: app.globalData.userInfo.id, productId, quantity: 1 })
      }

      // Get button position for drop animation
      const that = this
      const query = wx.createSelectorQuery()
      query.selectViewport().scrollOffset()
      query.exec(function(res) {
        const touch = e.touches && e.touches[0]
        const startX = touch ? touch.clientX : 300
        const startY = touch ? touch.clientY : 400

        // Animate: start at button, drop to bottom-left (cart tab position)
        that.setData({
          showDropBall: true,
          dropBallX: startX - 15,
          dropBallY: startY - 15
        })

        setTimeout(function() {
          const animation = wx.createAnimation({
            duration: 600,
            timingFunction: 'ease-in'
          })
          // Target: bottom-left cart icon area
          const sysInfo = wx.getSystemInfoSync()
          const targetX = sysInfo.windowWidth * 0.25 - startX + 15
          const targetY = sysInfo.windowHeight - startY - 30

          animation.translate(targetX, targetY).scale(0.3).opacity(0.4).step()
          that.setData({ dropBallAnimation: animation.export() })

          setTimeout(function() {
            that.setData({ showDropBall: false, dropBallAnimation: null })
          }, 650)
        }, 50)
      })

      this.loadData()
    } catch (err) {
      wx.showToast({ title: '操作失败', icon: 'error' })
    }
  },

  async decreaseQty(e) {
    const productId = e.currentTarget.dataset.id
    const app = getApp()
    if (!app.globalData.userInfo) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }

    const cartItem = this.data.cartMap[productId]
    if (!cartItem || cartItem.quantity <= 0) return

    try {
      if (cartItem.quantity <= 1) {
        await api.deleteCartItem(cartItem.id)
      } else {
        await api.updateCartItem({ id: cartItem.id, userId: cartItem.userId, productId: cartItem.productId, quantity: cartItem.quantity - 1 })
      }
      this.loadData()
    } catch (err) {
      wx.showToast({ title: '操作失败', icon: 'error' })
    }
  },

  async addToCart(product) {
    const app = getApp()
    try {
      await api.addCartItem({
        userId: app.globalData.userInfo.id,
        productId: product.id,
        quantity: 1
      })

      // Show drop animation
      const that = this
      const sysInfo = wx.getSystemInfoSync()
      const startX = sysInfo.windowWidth / 2
      const startY = sysInfo.windowHeight / 2

      that.setData({
        showDropBall: true,
        dropBallX: startX - 15,
        dropBallY: startY - 15
      })

      setTimeout(function() {
        const animation = wx.createAnimation({
          duration: 600,
          timingFunction: 'ease-in'
        })
        const targetX = sysInfo.windowWidth * 0.25 - startX + 15
        const targetY = sysInfo.windowHeight - startY - 30
        animation.translate(targetX, targetY).scale(0.3).opacity(0.4).step()
        that.setData({ dropBallAnimation: animation.export() })

        setTimeout(function() {
          that.setData({ showDropBall: false, dropBallAnimation: null })
        }, 650)
      }, 50)

      wx.showToast({ title: '已加入购物车', icon: 'success' })
      this.loadData()
    } catch (err) {
      wx.showToast({ title: '添加失败', icon: 'error' })
    }
  },

  async buyNow(product) {
    const app = getApp()
    try {
      const res = await api.addOrder(app.globalData.userInfo.id, product.id, 1)
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
