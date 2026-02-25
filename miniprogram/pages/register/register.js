const api = require('../../utils/api')

Page({
  data: {
    username: '',
    password: '',
    confirmPassword: '',
    phone: '',
    loading: false
  },

  onUsernameInput(e) { this.setData({ username: e.detail.value }) },
  onPasswordInput(e) { this.setData({ password: e.detail.value }) },
  onConfirmPasswordInput(e) { this.setData({ confirmPassword: e.detail.value }) },
  onPhoneInput(e) { this.setData({ phone: e.detail.value }) },

  async doRegister() {
    const { username, password, confirmPassword, phone } = this.data

    if (!username.trim()) {
      wx.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    if (!password.trim()) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }
    if (password !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      const res = await api.register({
        username: username.trim(),
        password: password.trim(),
        phone: phone.trim() || null,
        role: 'CUSTOMER'
      })
      if (res.code === 200) {
        wx.showToast({ title: '注册成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({ title: res.message || '注册失败', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: '网络错误', icon: 'error' })
    } finally {
      this.setData({ loading: false })
    }
  },

  goLogin() {
    wx.navigateBack()
  }
})
