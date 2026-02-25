<template>
  <view class="container">
    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>

    <template v-if="!loading && product">
      <!-- Product Image -->
      <view class="product-image-section">
        <image class="product-main-image" :src="product._imageUrl || '/static/product-placeholder.png'" mode="aspectFill" />
      </view>

      <!-- Product Info -->
      <view class="product-info-card">
        <view class="price-row">
          <text class="price">¥{{ product._displayPrice }}</text>
          <text class="original-price" v-if="product._isEmployee">¥{{ product.price }}</text>
          <text class="employee-tag" v-if="product._isEmployee">员工内购价</text>
        </view>
        <text class="product-title">{{ product.name }}</text>
        <text class="product-barcode" v-if="product.barcode">条码: {{ product.barcode }}</text>
        <view class="product-stock-row">
          <text class="stock-text">库存: {{ product.stock }}件</text>
          <text class="status-tag" :class="product.status === 1 ? 'on-shelf' : 'off-shelf'">{{ product.status === 1 ? '在售' : '已下架' }}</text>
        </view>
      </view>

      <!-- Description -->
      <view class="detail-card" v-if="product.description">
        <text class="detail-title">商品介绍</text>
        <text class="detail-content">{{ product.description }}</text>
      </view>

      <!-- Category -->
      <view class="detail-card" v-if="product.categoryName">
        <text class="detail-title">商品分类</text>
        <text class="detail-content">{{ product.categoryName }}</text>
      </view>

      <!-- Bottom Action Bar -->
      <view class="bottom-action-bar">
        <button class="action-btn cart-btn" @click="addToCart">🛒 加入购物车</button>
        <button class="action-btn buy-btn" @click="buyNow">立即购买</button>
      </view>
    </template>

    <view class="empty-state" v-if="!loading && !product">
      <text class="empty-icon">😕</text>
      <text class="empty-text">商品不存在</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import * as api from '../../utils/api.js'

const product = ref(null)
const loading = ref(true)

async function loadProduct(id) {
  loading.value = true
  try {
    const res = await api.getProductById(id)
    const p = res.data
    if (p) {
      const app = getApp()
      const isEmployee = app.globalData.userInfo && Boolean(app.globalData.userInfo.isHotelEmployee)
      p._imageUrl = p.image ? api.getFileUrl(p.image) : ''
      if (isEmployee && p.employeeDiscountRate) {
        p._isEmployee = true
        p._displayPrice = (p.price * p.employeeDiscountRate).toFixed(2)
      } else {
        p._isEmployee = false
        p._displayPrice = p.price ? p.price.toFixed(2) : '0.00'
      }
      product.value = p
    }
  } catch (e) {
    console.error('Failed to load product:', e)
  }
  loading.value = false
}

async function addToCart() {
  const app = getApp()
  if (!app.globalData.userInfo) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  try {
    await api.addCartItem({ userId: app.globalData.userInfo.id, productId: product.value.id, quantity: 1 })
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (err) {
    uni.showToast({ title: '添加失败', icon: 'error' })
  }
}

async function buyNow() {
  const app = getApp()
  if (!app.globalData.userInfo) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  const storeId = app.globalData.selectedStoreId
  if (!storeId) {
    uni.showToast({ title: '请先在首页选择店铺', icon: 'none' })
    return
  }
  try {
    const items = [{ productId: product.value.id, quantity: 1 }]
    const res = await api.addMultiItemOrder(app.globalData.userInfo.id, items, null, storeId)
    if (res.data && res.data.id) {
      uni.navigateTo({ url: '/pages/payment/payment?orderId=' + res.data.id + '&amount=' + res.data.totalAmount })
    } else {
      uni.showToast({ title: '下单成功', icon: 'success' })
      setTimeout(() => uni.switchTab({ url: '/pages/orders/orders' }), 1500)
    }
  } catch (err) {
    uni.showToast({ title: '下单失败', icon: 'error' })
  }
}

onLoad((options) => {
  if (options.id) {
    loadProduct(options.id)
  } else {
    loading.value = false
  }
})
</script>

<style scoped>
.product-image-section {
  width: 100%;
  background-color: #fff;
}

.product-main-image {
  width: 100%;
  height: 600rpx;
  background-color: #F5EDE0;
}

.product-info-card {
  background-color: #fff;
  padding: 24rpx;
  margin: 0 0 16rpx;
}

.price-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 12rpx;
}

.price-row .price {
  font-size: 44rpx;
  font-weight: bold;
  color: var(--color-accent);
}

.price-row .original-price {
  font-size: 26rpx;
  color: var(--color-text-secondary);
  text-decoration: line-through;
  margin-left: 12rpx;
}

.price-row .employee-tag {
  font-size: 20rpx;
  color: #fff;
  background-color: #ff6034;
  border-radius: 4rpx;
  padding: 4rpx 12rpx;
  margin-left: 12rpx;
}

.product-title {
  font-size: 34rpx;
  font-weight: 500;
  color: var(--color-text);
  display: block;
  margin-bottom: 12rpx;
  line-height: 1.4;
}

.product-barcode {
  font-size: 24rpx;
  color: var(--color-text-secondary);
  display: block;
  margin-bottom: 8rpx;
}

.product-stock-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.stock-text {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

.status-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
}

.on-shelf {
  color: #52c41a;
  background-color: #f6ffed;
  border: 1rpx solid #b7eb8f;
}

.off-shelf {
  color: #ff4d4f;
  background-color: #fff2f0;
  border: 1rpx solid #ffccc7;
}

.detail-card {
  background-color: #fff;
  padding: 24rpx;
  margin: 0 0 16rpx;
}

.detail-title {
  font-size: 28rpx;
  font-weight: bold;
  color: var(--color-text);
  display: block;
  margin-bottom: 12rpx;
}

.detail-content {
  font-size: 26rpx;
  color: var(--color-text-secondary);
  line-height: 1.6;
  display: block;
}

.bottom-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  border-radius: 40rpx;
  font-size: 28rpx;
  padding: 20rpx 0;
  text-align: center;
  border: none;
  line-height: 1.4;
}

.action-btn::after {
  border: none;
}

.cart-btn {
  background-color: #fff;
  color: var(--color-primary);
  border: 2rpx solid var(--color-primary);
}

.buy-btn {
  background: linear-gradient(135deg, #C9A96E, #B8956A);
  color: #fff;
}
</style>
