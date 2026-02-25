const api = require('../../utils/api')

Page({
  data: {
    coupons: [],
    loading: true,
    currentTab: 'AVAILABLE',
    tabs: [
      { key: 'AVAILABLE', name: '可使用' },
      { key: 'LOCKED', name: '已锁定' },
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
      const now = new Date()
      const coupons = userCoupons.map(uc => {
        const template = couponMap[uc.couponId]
        // Check if coupon is expired on client side
        let status = uc.status
        if (status === 'AVAILABLE' && template && template.endTime) {
          const endDate = new Date(template.endTime.replace(/-/g, '/'))
          if (endDate < now) {
            status = 'EXPIRED'
          }
        }
        return {
          ...uc,
          status,
          couponName: template ? template.name : '优惠券',
          discount: template ? template.discount : 0,
          minAmount: template ? template.minAmount : 0,
          endTime: template ? template.endTime : ''
        }
      })

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
