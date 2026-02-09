const api = require('../../utils/api')

const ROLE_MAP = {
  'ADMIN': '管理员',
  'EMPLOYEE': '员工',
  'CUSTOMER': '顾客'
}

Page({
  data: {
    userInfo: null,
    isLoggedIn: false,
    roleText: ''
  },

  onShow() {
    const app = getApp()
    const userInfo = app.globalData.userInfo
    if (userInfo) {
      this.setData({
        userInfo,
        isLoggedIn: true,
        roleText: ROLE_MAP[userInfo.role] || userInfo.role
      })
      // Refresh user data from server
      this.refreshUserInfo(userInfo.id)
    } else {
      this.setData({ userInfo: null, isLoggedIn: false })
    }
  },

  async refreshUserInfo(id) {
    try {
      const res = await api.getUserById(id)
      if (res.data) {
        const app = getApp()
        app.globalData.userInfo = res.data
        wx.setStorageSync('userInfo', res.data)
        this.setData({
          userInfo: res.data,
          roleText: ROLE_MAP[res.data.role] || res.data.role
        })
      }
    } catch (e) {
      console.error('Failed to refresh user info:', e)
    }
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  },

  goOrders() {
    wx.switchTab({ url: '/pages/orders/orders' })
  },

  goCart() {
    wx.switchTab({ url: '/pages/cart/cart' })
  },

  async doLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.logout()
          } catch (e) {
            // Ignore logout error
          }
          const app = getApp()
          app.globalData.userInfo = null
          wx.removeStorageSync('userInfo')
          this.setData({ userInfo: null, isLoggedIn: false })
          wx.showToast({ title: '已退出登录', icon: 'success' })
        }
      }
    })
  },

  getFileUrl(filename) {
    return api.getFileUrl(filename)
  }
})
