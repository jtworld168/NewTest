const api = require('../../utils/api')

Page({
  data: {
    categories: [],
    products: [],
    selectedCategoryId: null,
    loading: true,
    searchKeyword: ''
  },

  onLoad() {
    this.loadCategories()
  },

  onShow() {
    if (this.data.selectedCategoryId && this.data.categories.length > 0) {
      this.loadProductsByCategory(this.data.selectedCategoryId)
    }
  },

  async loadCategories() {
    try {
      const res = await api.getCategoryList()
      const categories = res.data || []
      this.setData({ categories })
      if (categories.length > 0) {
        const selectedId = this.data.selectedCategoryId || categories[0].id
        this.setData({ selectedCategoryId: selectedId })
        this.loadProductsByCategory(selectedId)
      } else {
        this.setData({ loading: false })
      }
    } catch (e) {
      console.error('Failed to load categories:', e)
      this.setData({ loading: false })
    }
  },

  async loadProductsByCategory(categoryId) {
    this.setData({ loading: true })
    try {
      const res = await api.getProductsByCategoryId(categoryId)
      this.setData({
        products: this._enrichProducts(res.data || []),
        loading: false
      })
    } catch (e) {
      console.error('Failed to load products:', e)
      this.setData({ loading: false })
    }
  },

  selectCategory(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ selectedCategoryId: id })
    this.loadProductsByCategory(id)
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  async doSearch() {
    const keyword = this.data.searchKeyword.trim()
    if (!keyword) {
      if (this.data.selectedCategoryId) {
        this.loadProductsByCategory(this.data.selectedCategoryId)
      }
      return
    }
    this.setData({ loading: true })
    try {
      const res = await api.searchProducts(keyword)
      this.setData({
        products: this._enrichProducts(res.data || []),
        selectedCategoryId: null,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
    }
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
        if (res.tapIndex === 0) this.addToCart(product)
        else if (res.tapIndex === 1) this.buyNow(product)
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

  _enrichProducts(products) {
    const app = getApp()
    const isEmployee = app.globalData.userInfo && Boolean(app.globalData.userInfo.isHotelEmployee)
    return products.map(p => {
      p._imageUrl = p.image ? api.getFileUrl(p.image) : ''
      if (isEmployee && p.employeeDiscountRate) {
        p._isEmployee = true
        p._displayPrice = (p.price * p.employeeDiscountRate).toFixed(2)
      } else {
        p._isEmployee = false
        p._displayPrice = p.price.toFixed(2)
      }
      return p
    })
  }
})
