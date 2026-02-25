const api = require('../../utils/api')

Page({
  onShow() {
    wx.scanCode({
      onlyFromCamera: true,
      success: (res) => {
        const code = res.result
        wx.showLoading({ title: '查找商品...' })
        api.searchProducts(code).then(searchRes => {
          wx.hideLoading()
          const products = searchRes.data || []
          if (products.length > 0) {
            wx.showToast({ title: '找到商品: ' + products[0].name, icon: 'none', duration: 2000 })
          } else {
            wx.showToast({ title: '未找到该商品', icon: 'none' })
          }
        }).catch(() => {
          wx.hideLoading()
          wx.showToast({ title: '查找失败', icon: 'error' })
        })
      },
      fail: () => {
        wx.switchTab({ url: '/pages/index/index' })
      }
    })
  }
})
