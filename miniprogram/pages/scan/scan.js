const api = require('../../utils/api')

Page({
  data: {
    scanResult: false,
    scanCode: '',
    foundProduct: null,
    flashMode: 'off'
  },

  onShow() {
    // Reset state when page shows
    this.setData({ scanResult: false, scanCode: '', foundProduct: null })
  },

  // Camera scanCode event handler
  onScanCode(e) {
    if (this.data.scanResult) return // Prevent duplicate scans
    const code = e.detail.result
    if (!code) return

    this.setData({ scanCode: code })
    wx.showLoading({ title: '查找商品...' })

    api.searchProducts(code).then(searchRes => {
      wx.hideLoading()
      const products = searchRes.data || []
      if (products.length > 0) {
        this.setData({ scanResult: true, foundProduct: products[0] })
      } else {
        this.setData({ scanResult: true, foundProduct: null })
      }
    }).catch(() => {
      wx.hideLoading()
      this.setData({ scanResult: true, foundProduct: null })
    })
  },

  toggleFlash() {
    this.setData({
      flashMode: this.data.flashMode === 'torch' ? 'off' : 'torch'
    })
  },

  rescan() {
    this.setData({ scanResult: false, scanCode: '', foundProduct: null })
  },

  goBack() {
    wx.switchTab({ url: '/pages/index/index' })
  }
})
