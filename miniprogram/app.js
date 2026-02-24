App({
  globalData: {
    userInfo: null,
    selectedStoreId: 0,
    selectedStoreName: '全部店铺',
    // 环境配置：开发环境使用本地地址，生产环境替换为服务器地址
    // 开发环境: http://localhost:8080/api
    // 测试环境: http://test-server:8080/api
    // 生产环境: https://your-domain.com/api
    baseUrl: 'http://localhost:8080/api',
    fileBaseUrl: 'http://localhost:8080'
  },

  onLaunch() {
    // Check if user is logged in
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.globalData.userInfo = userInfo
    }
  }
})
