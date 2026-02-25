<template>
  <view class="register-page">
    <view class="logo-area">
      <text class="logo-icon">🏪</text>
      <text class="logo-title">智慧零售</text>
      <text class="logo-subtitle">创建新账号</text>
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
      <view class="form-item">
        <text class="form-icon">🔒</text>
        <input class="form-input" placeholder="请确认密码" :password="true" v-model="confirmPassword" />
      </view>
      <view class="form-item">
        <text class="form-icon">📱</text>
        <input class="form-input" placeholder="请输入手机号 (选填)" type="number" maxlength="11" v-model="phone" />
      </view>

      <button class="btn-primary register-btn" @click="doRegister" :loading="loading" :disabled="loading">注册</button>

      <view class="login-link" @click="goLogin">
        <text>已有账号？</text>
        <text class="link-text">返回登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import * as api from '../../utils/api.js'

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const phone = ref('')
const loading = ref(false)

async function doRegister() {
  if (!username.value.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }
  if (!password.value.trim()) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  if (password.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }

  loading.value = true
  try {
    const res = await api.register({
      username: username.value.trim(),
      password: password.value.trim(),
      phone: phone.value.trim() || null,
      role: 'CUSTOMER'
    })
    if (res.code === 200) {
      uni.showToast({ title: '注册成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '注册失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络错误', icon: 'error' })
  } finally {
    loading.value = false
  }
}

function goLogin() {
  uni.navigateBack()
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #C9A96E 0%, #F5EDE0 50%);
  display: flex;
  flex-direction: column;
}

.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0 60rpx;
}

.logo-icon {
  font-size: 100rpx;
  margin-bottom: 16rpx;
}

.logo-title {
  font-size: 44rpx;
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

.register-btn {
  margin-top: 36rpx;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 32rpx;
}

.login-link {
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
