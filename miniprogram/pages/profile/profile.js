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
    roleText: '',
    isEmployee: false,
    avatarUrl: '/images/default-avatar.png'
  },

  onShow() {
    const app = getApp()
    const userInfo = app.globalData.userInfo
    if (userInfo) {
      const avatarUrl = userInfo.avatar ? api.getFileUrl(userInfo.avatar) : '/images/default-avatar.png'
      this.setData({
        userInfo,
        isLoggedIn: true,
        roleText: ROLE_MAP[userInfo.role] || userInfo.role,
        isEmployee: Boolean(userInfo.isHotelEmployee),
        avatarUrl
      })
      this.refreshUserInfo(userInfo.id)
    } else {
      this.setData({ userInfo: null, isLoggedIn: false, avatarUrl: '/images/default-avatar.png' })
    }
  },

  async refreshUserInfo(id) {
    try {
      const res = await api.getUserById(id)
      if (res.data) {
        const app = getApp()
        app.globalData.userInfo = res.data
        wx.setStorageSync('userInfo', res.data)
        const avatarUrl = res.data.avatar ? api.getFileUrl(res.data.avatar) : '/images/default-avatar.png'
        this.setData({
          userInfo: res.data,
          roleText: ROLE_MAP[res.data.role] || res.data.role,
          isEmployee: Boolean(res.data.isHotelEmployee),
          avatarUrl
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

  goCoupons() {
    wx.navigateTo({ url: '/pages/coupons/coupons' })
  },

  showAbout() {
    wx.showModal({
      title: '关于我们',
      content: '智慧零售小程序 v1.0.0\n\n无人超市智能购物系统，为您提供便捷的扫码购物体验。',
      showCancel: false,
      confirmText: '知道了'
    })
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
          wx.removeStorageSync('satoken')
          this.setData({ userInfo: null, isLoggedIn: false, avatarUrl: '/images/default-avatar.png' })
          wx.showToast({ title: '已退出登录', icon: 'success' })
        }
      }
    })
  }
})
