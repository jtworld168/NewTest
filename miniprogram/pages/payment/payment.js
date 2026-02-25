const api = require('../../utils/api')

Page({
  data: {
    orderId: null,
    amount: '0.00',
    paymentMethod: 'CASH',
    paying: false,
    countdown: 1800,
    countdownText: '30:00'
  },

  _timer: null,

  onLoad(options) {
    this.setData({
      orderId: options.orderId ? Number(options.orderId) : null,
      amount: options.amount || '0.00'
    })
    this.startCountdown()
  },

  onUnload() {
    if (this._timer) {
      clearInterval(this._timer)
      this._timer = null
    }
  },

  startCountdown() {
    let seconds = 1800
    this.setData({ countdown: seconds, countdownText: '30:00' })
    this._timer = setInterval(() => {
      seconds--
      if (seconds <= 0) {
        clearInterval(this._timer)
        this._timer = null
        this.setData({ countdown: 0, countdownText: '00:00' })
        wx.showToast({ title: '订单已超时', icon: 'none' })
        return
      }
      var m = Math.floor(seconds / 60)
      var s = seconds % 60
      this.setData({
        countdown: seconds,
        countdownText: (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s)
      })
    }, 1000)
  },

  selectMethod(e) {
    this.setData({ paymentMethod: e.currentTarget.dataset.method })
  },

  async quickTestPay() {
    this.setData({ paymentMethod: 'CASH' })
    await this.doPayment()
  },

  async confirmPay() {
    await this.doPayment()
  },

  async doPayment() {
    if (this.data.paying) return
    const { orderId, amount, paymentMethod } = this.data
    if (!orderId) {
      wx.showToast({ title: '订单信息错误', icon: 'none' })
      return
    }

    this.setData({ paying: true })
    try {
      const transactionNo = 'TXN' + Date.now() + Math.floor(Math.random() * 1000)
      await api.addPayment({
        orderId,
        amount: parseFloat(amount),
        paymentMethod,
        paymentStatus: 'SUCCESS',
        transactionNo,
        paymentTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
      })

      await api.updateOrder({ id: orderId, status: 'PAID' })

      if (this._timer) {
        clearInterval(this._timer)
        this._timer = null
      }

      wx.showToast({ title: '支付成功', icon: 'success' })
      setTimeout(() => {
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
