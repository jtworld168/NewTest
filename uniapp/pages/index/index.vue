<template>
  <view class="container">
    <!-- Store Selector -->
    <view class="store-selector">
      <picker mode="selector" :range="storeNames" @change="onStoreChange">
        <view class="store-picker">
          <text class="store-icon">🏪</text>
          <text class="store-name">{{ selectedStoreName }}</text>
          <text class="store-arrow">▼</text>
        </view>
      </picker>
    </view>

    <!-- Search Bar with Scan Button -->
    <view class="search-bar">
      <view class="search-input" @click="goToCategory">
        <text class="search-icon">🔍</text>
        <text class="search-placeholder">搜索商品</text>
      </view>
      <view class="scan-btn" @click="onScan">
        <text class="scan-icon">📷</text>
      </view>
    </view>

    <!-- Swiper Banner (uses product images) -->
    <view class="swiper-section">
      <swiper class="banner-swiper" indicator-dots autoplay :interval="3000" circular>
        <swiper-item v-for="item in bannerProducts" :key="item.id">
          <view class="banner-item" @click="goToProductDetail(item.id)">
            <image class="banner-image" :src="item._imageUrl || '/static/product-placeholder.png'" mode="aspectFill" />
            <view class="banner-overlay">
              <text class="banner-text">{{ item.name }}</text>
              <text class="banner-sub">¥{{ item._displayPrice }}</text>
            </view>
          </view>
        </swiper-item>
        <swiper-item v-if="bannerProducts.length === 0">
          <view class="banner-item banner-1">
            <text class="banner-text">智慧零售 · 智能购物</text>
            <text class="banner-sub">随时随地，扫码即购</text>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <!-- Category Quick Links -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">商品分类</text>
        <text class="section-more" @click="goToCategory">查看全部 ></text>
      </view>
      <scroll-view class="category-scroll" scroll-x>
        <view class="category-item" v-for="item in categories" :key="item.id" @click="goToCategoryDetail(item.id)">
          <view class="category-icon">📦</view>
          <text class="category-name">{{ item.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- Product List (Single Column) -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">热门商品</text>
      </view>
      <view class="product-list" v-if="!loading">
        <view class="product-row" v-for="item in products" :key="item.id">
          <image class="product-row-image" :src="item._imageUrl || '/static/product-placeholder.png'" mode="aspectFill" @click="goToProductDetail(item.id)" />
          <view class="product-row-info">
            <text class="product-name" @click="goToProductDetail(item.id)">{{ item.name }}</text>
            <text class="product-desc" v-if="item.description">{{ item.description }}</text>
            <view class="product-bottom">
              <view class="price-area">
                <text class="price">¥{{ item._displayPrice }}</text>
                <text class="original-price" v-if="item._isEmployee">¥{{ item.price }}</text>
                <text class="employee-tag" v-if="item._isEmployee">内购价</text>
              </view>
              <view class="product-qty-control">
                <view class="product-qty-btn" @click="decreaseQty(item.id)">-</view>
                <text class="product-qty-num">{{ item._cartQty || 0 }}</text>
                <view class="product-qty-btn" :class="{ 'cart-bounce': item._bouncing }" @click="increaseQty(item.id)">+</view>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>
      <view class="empty-state" v-if="!loading && products.length === 0">
        <text class="empty-icon">🛒</text>
        <text class="empty-text">暂无商品</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import * as api from '../../utils/api.js'

const products = ref([])
const bannerProducts = ref([])
const categories = ref([])
const loading = ref(true)
const cartMap = ref({})
const stores = ref([])
const storeNames = ref(['全部店铺'])
const selectedStoreName = ref('全部店铺')

async function loadStores() {
  try {
    const res = await api.getStoreList()
    const storeList = res.data || []
    stores.value = storeList
    storeNames.value = ['全部店铺'].concat(storeList.map(s => s.name))
  } catch (e) {
    console.error('Failed to load stores:', e)
  }
}

function onStoreChange(e) {
  const index = parseInt(e.detail.value)
  const app = getApp()
  if (index === 0) {
    selectedStoreName.value = '全部店铺'
    app.globalData.selectedStoreId = 0
    app.globalData.selectedStoreName = '全部店铺'
  } else {
    const store = stores.value[index - 1]
    selectedStoreName.value = store.name
    app.globalData.selectedStoreId = store.id
    app.globalData.selectedStoreName = store.name
  }
}

function loadData() {
  loading.value = true
  Promise.all([
    api.getProductList(),
    api.getCategoryList()
  ]).then(async ([productRes, categoryRes]) => {
    const rawProducts = (productRes.data || []).slice(0, 8)
    const app = getApp()
    const isEmployee = app.globalData.userInfo && Boolean(app.globalData.userInfo.isHotelEmployee)

    rawProducts.forEach(p => {
      p._imageUrl = p.image ? api.getFileUrl(p.image) : ''
      if (isEmployee && p.employeeDiscountRate) {
        p._isEmployee = true
        p._displayPrice = (p.price * p.employeeDiscountRate).toFixed(2)
      } else {
        p._isEmployee = false
        p._displayPrice = p.price.toFixed(2)
      }
      p._bouncing = false
    })

    let map = {}
    if (app.globalData.userInfo) {
      try {
        const cartRes = await api.getCartByUserId(app.globalData.userInfo.id)
        const cartItems = cartRes.data || []
        cartItems.forEach(item => {
          map[item.productId] = item
        })
      } catch (e) {
        console.error('Failed to load cart:', e)
      }
    }

    rawProducts.forEach(p => {
      p._cartQty = map[p.id] ? map[p.id].quantity : 0
    })

    products.value = rawProducts
    bannerProducts.value = rawProducts.filter(p => p._imageUrl).slice(0, 3)
    categories.value = categoryRes.data || []
    cartMap.value = map
    loading.value = false
  }).catch(e => {
    console.error('Failed to load data:', e)
    loading.value = false
  })
}

function goToCategory() {
  uni.navigateTo({ url: '/pages/category/category' })
}

function onScan() {
  uni.scanCode({
    onlyFromCamera: true,
    success: (res) => {
      const code = res.result
      uni.showLoading({ title: '查找商品...' })
      api.searchProducts(code).then(searchRes => {
        uni.hideLoading()
        const found = searchRes.data || []
        if (found.length > 0) {
          const product = found[0]
          uni.showActionSheet({
            itemList: ['加入购物车', '立即购买'],
            success: (actionRes) => {
              if (actionRes.tapIndex === 0) addToCart(product)
              else if (actionRes.tapIndex === 1) buyNow(product)
            }
          })
        } else {
          uni.showToast({ title: '未找到该商品', icon: 'none' })
        }
      }).catch(() => {
        uni.hideLoading()
        uni.showToast({ title: '查找失败', icon: 'error' })
      })
    },
    fail: () => {}
  })
}

function goToCategoryDetail(id) {
  uni.navigateTo({ url: '/pages/category/category?categoryId=' + id })
}

function goToProductDetail(id) {
  uni.navigateTo({ url: '/pages/product-detail/product-detail?id=' + id })
}

async function increaseQty(productId) {
  const app = getApp()
  if (!app.globalData.userInfo) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  const cartItem = cartMap.value[productId]
  try {
    if (cartItem) {
      await api.updateCartItem({ id: cartItem.id, userId: cartItem.userId, productId: cartItem.productId, quantity: cartItem.quantity + 1 })
    } else {
      await api.addCartItem({ userId: app.globalData.userInfo.id, productId, quantity: 1 })
    }
    uni.showToast({ title: '已添加', icon: 'success', duration: 800 })
    const idx = products.value.findIndex(p => p.id === productId)
    if (idx !== -1) {
      products.value[idx]._bouncing = true
      setTimeout(() => {
        products.value[idx]._bouncing = false
      }, 500)
    }
    loadData()
  } catch (err) {
    uni.showToast({ title: '操作失败', icon: 'error' })
  }
}

async function decreaseQty(productId) {
  const app = getApp()
  if (!app.globalData.userInfo) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  const cartItem = cartMap.value[productId]
  if (!cartItem || cartItem.quantity <= 0) return
  try {
    if (cartItem.quantity <= 1) {
      await api.deleteCartItem(cartItem.id)
    } else {
      await api.updateCartItem({ id: cartItem.id, userId: cartItem.userId, productId: cartItem.productId, quantity: cartItem.quantity - 1 })
    }
    loadData()
  } catch (err) {
    uni.showToast({ title: '操作失败', icon: 'error' })
  }
}

async function addToCart(product) {
  const app = getApp()
  try {
    await api.addCartItem({ userId: app.globalData.userInfo.id, productId: product.id, quantity: 1 })
    uni.showToast({ title: '已加入购物车', icon: 'success' })
    loadData()
  } catch (err) {
    uni.showToast({ title: '添加失败', icon: 'error' })
  }
}

async function buyNow(product) {
  const app = getApp()
  const storeId = app.globalData.selectedStoreId
  if (!storeId) {
    uni.showToast({ title: '请先选择店铺', icon: 'none' })
    return
  }
  try {
    const items = [{ productId: product.id, quantity: 1 }]
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

onShow(() => {
  if (stores.value.length === 0) loadStores()
  loadData()
})
</script>

<style scoped>
/* Store Selector */
.store-selector {
  padding: 16rpx 20rpx 0;
  background-color: var(--color-primary);
}

.store-picker {
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 16rpx;
  padding: 12rpx 20rpx;
}

.store-icon {
  font-size: 32rpx;
  margin-right: 10rpx;
}

.store-name {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.store-arrow {
  font-size: 20rpx;
  color: #999;
}

.search-bar {
  padding: 20rpx;
  background-color: var(--color-primary);
  display: flex;
  align-items: center;
}

.search-input {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 32rpx;
  padding: 16rpx 24rpx;
}

.search-icon {
  margin-right: 12rpx;
  font-size: 28rpx;
}

.search-placeholder {
  color: var(--color-text-secondary);
  font-size: 26rpx;
}

.scan-btn {
  margin-left: 16rpx;
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.scan-icon {
  font-size: 36rpx;
}

/* Swiper Banner */
.swiper-section {
  padding: 0 20rpx;
  margin-top: 20rpx;
}

.banner-swiper {
  height: 260rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.banner-item {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  position: relative;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
}

.banner-overlay {
  position: relative;
  z-index: 1;
  background: linear-gradient(transparent, rgba(0,0,0,0.5));
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 20rpx 24rpx;
  box-sizing: border-box;
}

.banner-1 { background: linear-gradient(135deg, #C9A96E, #B8956A); }
.banner-2 { background: linear-gradient(135deg, #1890ff, #096dd9); }
.banner-3 { background: linear-gradient(135deg, #faad14, #d48806); }

.banner-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.banner-sub {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
}

/* Section */
.section { margin-top: 20rpx; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--color-text);
}

.section-more {
  font-size: 24rpx;
  color: var(--color-primary);
}

/* Category Scroll */
.category-scroll {
  white-space: nowrap;
  padding: 0 24rpx 20rpx;
}

.category-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 140rpx;
  margin-right: 20rpx;
}

.category-icon {
  width: 100rpx;
  height: 100rpx;
  background-color: #F5EDE0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  margin-bottom: 10rpx;
}

.category-name {
  font-size: 24rpx;
  color: var(--color-text-secondary);
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 140rpx;
}

/* Product List */
.product-list { padding: 0 20rpx; }

.product-row {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(60, 50, 38, 0.06);
  animation: slideUp 0.35s ease-out backwards;
}

.product-row-image {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
  background-color: #F5EDE0;
}

.product-row-info {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.product-desc {
  font-size: 22rpx;
  color: var(--color-text-secondary);
  margin-top: 6rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12rpx;
}

.product-bottom .price { font-size: 32rpx; }

.price-area {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.price-area .original-price {
  font-size: 22rpx;
  color: var(--color-text-secondary);
  text-decoration: line-through;
  margin-left: 8rpx;
}

.price-area .employee-tag {
  font-size: 20rpx;
  color: #fff;
  background-color: #ff6034;
  border-radius: 4rpx;
  padding: 2rpx 8rpx;
  margin-left: 8rpx;
}

/* Product Quantity Control */
.product-qty-control {
  display: flex;
  align-items: center;
}

.product-qty-btn {
  width: 48rpx;
  height: 48rpx;
  border: 1rpx solid var(--color-border);
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: var(--color-text);
  background-color: var(--color-surface);
}

.cart-bounce {
  animation: cartBounce 0.5s ease;
}

.product-qty-num {
  width: 52rpx;
  text-align: center;
  font-size: 26rpx;
  color: var(--color-text);
}
</style>
