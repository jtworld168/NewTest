App({
  globalData: {
    userInfo: null,
    baseUrl: 'http://localhost:8080/api'
  },

  onLaunch() {
    // Check if user is logged in
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.globalData.userInfo = userInfo
    }
  }
})
