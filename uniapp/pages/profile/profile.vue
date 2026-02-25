<template>
  <view class="container">
    <!-- User Header -->
    <view class="user-header">
      <template v-if="isLoggedIn">
        <image class="avatar" :src="avatarUrl" mode="aspectFill" />
        <view class="user-info">
          <text class="username">{{ userInfo.username }}</text>
          <view class="role-row">
            <text class="user-role">{{ roleText }}</text>
            <text class="employee-badge" v-if="isEmployee">🏷️ 员工内部价</text>
          </view>
          <text class="user-phone" v-if="userInfo.phone">📱 {{ userInfo.phone }}</text>
        </view>
      </template>
      <template v-else>
        <image class="avatar" src="/static/default-avatar.png" mode="aspectFill" />
        <view class="user-info">
          <text class="username" @click="goLogin">点击登录</text>
          <text class="user-role">未登录</text>
        </view>
      </template>
    </view>

    <!-- Menu List -->
    <view class="menu-list" v-if="isLoggedIn">
      <view class="menu-item" @click="goOrders">
        <text class="menu-icon">📋</text>
        <text class="menu-text">我的订单</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="goCart">
        <text class="menu-icon">🛒</text>
        <text class="menu-text">购物车</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="goCoupons">
        <text class="menu-icon">🎫</text>
        <text class="menu-text">我的优惠券</text>
        <text class="menu-arrow">></text>
      </view>
    </view>

    <!-- Employee Discount Info -->
    <view class="employee-info" v-if="isLoggedIn && isEmployee">
      <view class="employee-info-header">
        <text class="employee-info-icon">💰</text>
        <text class="employee-info-title">员工专享折扣</text>
      </view>
      <text class="employee-info-desc">您是内部员工，部分商品享有员工专享折扣价，下单时系统将自动按折扣价计算。</text>
      <text class="employee-info-note">注意：员工折扣与优惠券不可叠加使用。</text>
    </view>

    <!-- Actions -->
    <view class="actions" v-if="!isLoggedIn">
      <button class="btn-primary" @click="goLogin">登录</button>
      <button class="btn-register" @click="goRegister">注册新账号</button>
    </view>

    <!-- Settings Section -->
    <view class="menu-list" v-if="isLoggedIn">
      <view class="menu-item" @click="showAbout">
        <text class="menu-icon">ℹ️</text>
        <text class="menu-text">关于我们</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item">
        <text class="menu-icon">📱</text>
        <text class="menu-text">版本信息</text>
        <text class="menu-version">v1.0.0</text>
      </view>
    </view>

    <view class="actions" v-if="isLoggedIn">
      <button class="btn-danger logout-btn" @click="doLogout">退出登录</button>
    </view>
  </view>
</template>

<script>
import * as api from '../../utils/api.js'

const ROLE_MAP = { ADMIN: '管理员', EMPLOYEE: '员工', CUSTOMER: '顾客' }

export default {
  data() {
    return {
      userInfo: null,
      isLoggedIn: false,
      roleText: '',
      isEmployee: false,
      avatarUrl: '/static/default-avatar.png'
    }
  },
  onShow() {
    this.onPageShow()
  },
  methods: {
    async refreshUserInfo(id) {
      try {
        const res = await api.getUserById(id)
        if (res.data) {
          const app = getApp()
          app.globalData.userInfo = res.data
          uni.setStorageSync('userInfo', res.data)
          this.userInfo = res.data
          this.roleText = ROLE_MAP[res.data.role] || res.data.role
          this.isEmployee = Boolean(res.data.isHotelEmployee)
          this.avatarUrl = res.data.avatar ? api.getFileUrl(res.data.avatar) : '/static/default-avatar.png'
        }
      } catch (e) {
        console.error('Failed to refresh user info:', e)
      }
    },
    onPageShow() {
      const app = getApp()
      const info = app.globalData.userInfo
      if (info) {
        this.userInfo = info
        this.isLoggedIn = true
        this.roleText = ROLE_MAP[info.role] || info.role
        this.isEmployee = Boolean(info.isHotelEmployee)
        this.avatarUrl = info.avatar ? api.getFileUrl(info.avatar) : '/static/default-avatar.png'
        this.refreshUserInfo(info.id)
      } else {
        this.userInfo = null
        this.isLoggedIn = false
        this.avatarUrl = '/static/default-avatar.png'
      }
    },
    goLogin() {
      uni.navigateTo({ url: '/pages/login/login' })
    },
    goRegister() {
      uni.navigateTo({ url: '/pages/register/register' })
    },
    goOrders() {
      uni.switchTab({ url: '/pages/orders/orders' })
    },
    goCart() {
      uni.switchTab({ url: '/pages/cart/cart' })
    },
    goCoupons() {
      uni.navigateTo({ url: '/pages/coupons/coupons' })
    },
    showAbout() {
      uni.showModal({
        title: '关于我们',
        content: '智慧零售小程序 v1.0.0\n\n无人超市智能购物系统，为您提供便捷的扫码购物体验。',
        showCancel: false,
        confirmText: '知道了'
      })
    },
    doLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: async (res) => {
          if (res.confirm) {
            try { await api.logout() } catch (e) { /* ignore */ }
            const app = getApp()
            app.globalData.userInfo = null
            uni.removeStorageSync('userInfo')
            uni.removeStorageSync('satoken')
            this.userInfo = null
            this.isLoggedIn = false
            this.avatarUrl = '/static/default-avatar.png'
            uni.showToast({ title: '已退出登录', icon: 'success' })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.user-header {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #C9A96E, #B8956A);
  padding: 60rpx 40rpx;
  color: #fff;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  margin-right: 30rpx;
  background-color: rgba(255, 255, 255, 0.2);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.user-role {
  font-size: 24rpx;
  opacity: 0.8;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  display: inline-block;
  margin-bottom: 8rpx;
  width: fit-content;
}

.role-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.employee-badge {
  font-size: 22rpx;
  background-color: rgba(255, 215, 0, 0.3);
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  color: #fff;
}

.user-phone {
  font-size: 24rpx;
  opacity: 0.9;
}

/* Employee Info Card */
.employee-info {
  margin: 20rpx;
  background: linear-gradient(135deg, #fff9e6, #fff3cc);
  border-radius: 16rpx;
  padding: 24rpx;
  border: 1rpx solid #ffe58f;
}

.employee-info-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.employee-info-icon {
  font-size: 36rpx;
  margin-right: 10rpx;
}

.employee-info-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #d48806;
}

.employee-info-desc {
  font-size: 24rpx;
  color: #8c6d1f;
  line-height: 1.6;
  display: block;
  margin-bottom: 8rpx;
}

.employee-info-note {
  font-size: 22rpx;
  color: #ad8b00;
  display: block;
}

/* Menu */
.menu-list {
  margin-top: 20rpx;
  background-color: #fff;
  border-radius: 16rpx;
  margin: 20rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx 24rpx;
  border-bottom: 1rpx solid var(--color-border);
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: var(--color-text);
}

.menu-arrow {
  font-size: 28rpx;
  color: #ccc;
}

.menu-version {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

/* Actions */
.actions {
  padding: 40rpx;
}

.btn-register {
  margin-top: 20rpx;
  background-color: #fff;
  color: var(--color-primary);
  border: 2rpx solid var(--color-primary);
  border-radius: 12rpx;
  padding: 20rpx 0;
  font-size: 32rpx;
}

.btn-register::after {
  border: none;
}

.logout-btn {
  margin-top: 40rpx;
}
</style>
