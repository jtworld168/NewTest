<template>
  <view class="container">
    <!-- Tabs -->
    <view class="tabs">
      <view
        class="tab"
        :class="{ active: currentTab === item.key }"
        v-for="item in tabs"
        :key="item.key"
        @click="switchTab(item.key)"
      >
        <text>{{ item.name }}</text>
      </view>
    </view>

    <!-- Coupon List -->
    <view class="coupon-list" v-if="!loading">
      <template v-for="item in coupons" :key="item.id">
        <view class="coupon-card" :class="{ disabled: item.status !== 'AVAILABLE' }" v-if="item.status === currentTab">
          <view class="coupon-left">
            <text class="coupon-symbol">¥</text>
            <text class="coupon-amount">{{ item.discount }}</text>
          </view>
          <view class="coupon-divider"></view>
          <view class="coupon-right">
            <text class="coupon-name">{{ item.couponName }}</text>
            <text class="coupon-condition">满{{ item.minAmount }}元可用</text>
            <text class="coupon-expire" v-if="item.endTime">有效期至 {{ item.endTime }}</text>
            <text class="coupon-status-tag" v-if="item.status === 'USED'">已使用</text>
            <text class="coupon-status-tag expired" v-if="item.status === 'EXPIRED'">已过期</text>
          </view>
        </view>
      </template>
    </view>

    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>

    <view class="empty-state" v-if="!loading && coupons.length === 0">
      <text class="empty-icon">🎫</text>
      <text class="empty-text">暂无优惠券</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import * as api from '../../utils/api.js'

const coupons = ref([])
const loading = ref(true)
const currentTab = ref('AVAILABLE')
const tabs = ref([
  { key: 'AVAILABLE', name: '可使用' },
  { key: 'USED', name: '已使用' },
  { key: 'EXPIRED', name: '已过期' }
])

async function loadCoupons() {
  const app = getApp()
  if (!app.globalData.userInfo) {
    loading.value = false
    coupons.value = []
    return
  }
  loading.value = true
  try {
    const res = await api.getUserCouponsByUserId(app.globalData.userInfo.id)
    const userCoupons = res.data || []

    const couponIds = [...new Set(userCoupons.map(uc => uc.couponId).filter(Boolean))]
    const couponMap = {}
    await Promise.all(couponIds.map(id =>
      api.getCouponById(id).then(cRes => {
        if (cRes.data) couponMap[id] = cRes.data
      }).catch(() => {})
    ))

    coupons.value = userCoupons.map(uc => ({
      ...uc,
      couponName: couponMap[uc.couponId] ? couponMap[uc.couponId].name : '优惠券',
      discount: couponMap[uc.couponId] ? couponMap[uc.couponId].discount : 0,
      minAmount: couponMap[uc.couponId] ? couponMap[uc.couponId].minAmount : 0,
      endTime: couponMap[uc.couponId] ? couponMap[uc.couponId].endTime : ''
    }))
    loading.value = false
  } catch (e) {
    console.error('Failed to load coupons:', e)
    loading.value = false
  }
}

function switchTab(tab) {
  currentTab.value = tab
}

onShow(() => { loadCoupons() })
</script>

<style scoped>
.tabs {
  display: flex;
  background-color: #fff;
  padding: 0 10rpx;
  border-bottom: 1rpx solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: var(--color-text-secondary);
  position: relative;
}

.tab.active {
  color: var(--color-primary);
  font-weight: bold;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background-color: var(--color-primary);
  border-radius: 2rpx;
}

.coupon-list { padding: 20rpx; }

.coupon-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(60, 50, 38, 0.06);
  animation: slideUp 0.35s ease-out backwards;
}

.coupon-card.disabled { opacity: 0.5; }

.coupon-left {
  width: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30rpx 0;
  background: linear-gradient(135deg, #C9A96E, #B8956A);
  color: #fff;
}

.coupon-symbol { font-size: 24rpx; }
.coupon-amount { font-size: 56rpx; font-weight: bold; }

.coupon-divider {
  width: 2rpx;
  height: 80rpx;
  background: repeating-linear-gradient(to bottom, var(--color-border) 0, var(--color-border) 8rpx, transparent 8rpx, transparent 16rpx);
}

.coupon-right {
  flex: 1;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
}

.coupon-name {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 8rpx;
}

.coupon-condition {
  font-size: 22rpx;
  color: var(--color-text-secondary);
  margin-bottom: 4rpx;
}

.coupon-expire {
  font-size: 20rpx;
  color: #bbb;
}

.coupon-status-tag {
  font-size: 20rpx;
  color: var(--color-text-secondary);
  margin-top: 6rpx;
}

.coupon-status-tag.expired { color: #ee0a24; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: var(--color-text-secondary);
}
</style>
