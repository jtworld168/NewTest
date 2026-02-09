const api = require('../../utils/api')

Page({
  data: {
    products: [],
    categories: [],
    loading: true
  },

  onLoad() {
    this.loadData()
  },

  onPullDownRefresh() {
    this.loadData().then(() => wx.stopPullDownRefresh())
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const [productRes, categoryRes] = await Promise.all([
        api.getProductList(),
        api.getCategoryList()
      ])
      this.setData({
        products: (productRes.data || []).slice(0, 8),
        categories: categoryRes.data || [],
        loading: false
      })
    } catch (e) {
      console.error('Failed to load data:', e)
      this.setData({ loading: false })
    }
  },

  goToCategory() {
    wx.switchTab({ url: '/pages/category/category' })
  },

  onScan() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => {
        const code = res.result
        // Search for product by barcode
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
      fail: () => {
        // User cancelled or scan failed
      }
    })
  },

  goToCategoryDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.switchTab({
      url: '/pages/category/category',
      success() {
        const page = getCurrentPages().pop()
        if (page) page.setData({ selectedCategoryId: id })
      }
    })
  },

  goToProductDetail(e) {
    const id = e.currentTarget.dataset.id
    const product = this.data.products.find(p => p.id === id)
    if (!product) return
    // Add to cart directly
    const app = getApp()
    const userInfo = app.globalData.userInfo
    if (!userInfo) {
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

  async addToCart(product) {
    const app = getApp()
    try {
      await api.addCartItem({
        userId: app.globalData.userInfo.id,
        productId: product.id,
        quantity: 1
      })
      wx.showToast({ title: '已加入购物车', icon: 'success' })
    } catch (e) {
      wx.showToast({ title: '添加失败', icon: 'error' })
    }
  },

  async buyNow(product) {
    const app = getApp()
    try {
      await api.addOrder(app.globalData.userInfo.id, product.id, 1)
      wx.showToast({ title: '下单成功', icon: 'success' })
      setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 1500)
    } catch (e) {
      wx.showToast({ title: '下单失败', icon: 'error' })
    }
  },

  getFileUrl(filename) {
    return api.getFileUrl(filename)
  }
})
