<template>
  <view class="login-page">
    <view class="logo-area">
      <text class="logo-icon">🏪</text>
      <text class="logo-title">无人超市</text>
      <text class="logo-subtitle">欢迎回来</text>
    </view>

    <view class="form-area">
      <view class="form-item">
        <text class="form-icon">👤</text>
        <input class="form-input" placeholder="请输入用户名" v-model="username" />
      </view>
      <view class="form-item">
        <text class="form-icon">🔒</text>
        <input class="form-input" placeholder="请输入密码" :password="true" v-model="password" />
      </view>

      <button class="btn-primary login-btn" @click="doLogin" :loading="loading" :disabled="loading">登录</button>

      <view class="register-link" @click="goRegister">
        <text>没有账号？</text>
        <text class="link-text">立即注册</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import * as api from '../../utils/api.js'

const username = ref('')
const password = ref('')
const loading = ref(false)

async function doLogin() {
  if (!username.value.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }
  if (!password.value.trim()) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  loading.value = true
  try {
    const res = await api.login(username.value.trim(), password.value.trim())
    if (res.code === 200 && res.data) {
      const app = getApp()
      const userData = res.data.userInfo || res.data
      const token = res.data.token || res.data.satoken || ''

      app.globalData.userInfo = userData
      uni.setStorageSync('userInfo', userData)
      if (token) {
        uni.setStorageSync('satoken', token)
      }

      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/index/index' })
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '登录失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络错误', icon: 'error' })
  } finally {
    loading.value = false
  }
}

function goRegister() {
  uni.navigateTo({ url: '/pages/register/register' })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #C9A96E 0%, #F5EDE0 50%);
  display: flex;
  flex-direction: column;
}

.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0 60rpx;
}

.logo-icon {
  font-size: 120rpx;
  margin-bottom: 16rpx;
}

.logo-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.logo-subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.form-area {
  margin: 0 40rpx;
  background-color: #fff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  box-shadow: 0 8rpx 40rpx rgba(60, 50, 38, 0.08);
  animation: scaleIn 0.4s ease-out;
}

.form-item {
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid var(--color-border);
  padding: 22rpx 0;
  margin-bottom: 16rpx;
}

.form-icon {
  font-size: 34rpx;
  margin-right: 18rpx;
}

.form-input {
  flex: 1;
  font-size: 28rpx;
  height: 56rpx;
}

.login-btn {
  margin-top: 40rpx;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 32rpx;
}

.register-link {
  text-align: center;
  margin-top: 28rpx;
  font-size: 26rpx;
  color: var(--color-text-secondary);
}

.link-text {
  color: var(--color-primary);
  margin-left: 8rpx;
}
</style>
