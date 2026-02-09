const api = require('../../utils/api')

Page({
  data: {
    coupons: [],
    loading: true,
    currentTab: 'AVAILABLE',
    tabs: [
      { key: 'AVAILABLE', name: '可使用' },
      { key: 'USED', name: '已使用' },
      { key: 'EXPIRED', name: '已过期' }
    ]
  },

  onShow() {
    this.loadCoupons()
  },

  async loadCoupons() {
    const app = getApp()
    if (!app.globalData.userInfo) {
      this.setData({ loading: false, coupons: [] })
      return
    }
    this.setData({ loading: true })
    try {
      const res = await api.getUserCouponsByUserId(app.globalData.userInfo.id)
      const userCoupons = res.data || []

      // Load coupon template details for each user coupon
      const couponIds = [...new Set(userCoupons.map(uc => uc.couponId).filter(Boolean))]
      const couponMap = {}
      await Promise.all(couponIds.map(id =>
        api.getCouponById(id).then(cRes => {
          if (cRes.data) couponMap[id] = cRes.data
        }).catch(() => {})
      ))

      // Merge coupon details into user coupons
      const coupons = userCoupons.map(uc => ({
        ...uc,
        couponName: couponMap[uc.couponId] ? couponMap[uc.couponId].name : '优惠券',
        discount: couponMap[uc.couponId] ? couponMap[uc.couponId].discount : 0,
        minAmount: couponMap[uc.couponId] ? couponMap[uc.couponId].minAmount : 0,
        endTime: couponMap[uc.couponId] ? couponMap[uc.couponId].endTime : ''
      }))

      this.setData({ coupons, loading: false })
    } catch (e) {
      console.error('Failed to load coupons:', e)
      this.setData({ loading: false })
    }
  },

  switchTab(e) {
    this.setData({ currentTab: e.currentTarget.dataset.tab })
  }
})
