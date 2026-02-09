const api = require('../../utils/api')

Page({
  data: {
    orderId: null,
    amount: '0.00',
    paymentMethod: 'WECHAT',
    paying: false
  },

  onLoad(options) {
    this.setData({
      orderId: options.orderId ? Number(options.orderId) : null,
      amount: options.amount || '0.00'
    })
  },

  selectMethod(e) {
    this.setData({ paymentMethod: e.currentTarget.dataset.method })
  },

  async confirmPay() {
    if (this.data.paying) return
    const { orderId, amount, paymentMethod } = this.data
    if (!orderId) {
      wx.showToast({ title: '订单信息错误', icon: 'none' })
      return
    }

    this.setData({ paying: true })
    try {
      // Create payment record
      const transactionNo = 'TXN' + Date.now() + Math.floor(Math.random() * 1000)
      await api.addPayment({
        orderId,
        amount: parseFloat(amount),
        paymentMethod,
        paymentStatus: 'SUCCESS',
        transactionNo,
        paymentTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
      })

      // Update order status to PAID
      await api.updateOrder({ id: orderId, status: 'PAID' })

      wx.showToast({ title: '支付成功', icon: 'success' })
      setTimeout(() => {
        // Go back to orders page
        wx.switchTab({ url: '/pages/orders/orders' })
      }, 1500)
    } catch (err) {
      console.error('Payment failed:', err)
      wx.showToast({ title: '支付失败', icon: 'error' })
    } finally {
      this.setData({ paying: false })
    }
  }
})
