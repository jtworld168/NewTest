<template>
  <view class="container">
    <!-- Not logged in / Empty -->
    <view class="empty-state" v-if="!loading && !cartItems.length">
      <text class="empty-icon">🛒</text>
      <text class="empty-text">购物车空空如也</text>
      <button class="btn-primary go-shop-btn" @click="goShopping">去逛逛</button>
    </view>

    <!-- Cart Items -->
    <view v-if="cartItems.length > 0">
      <!-- Clear button -->
      <view class="cart-header">
        <text class="cart-title">购物车 ({{ cartItems.length }})</text>
        <text class="clear-btn" @click="doClearCart">清空</text>
      </view>

      <view class="cart-item" v-for="item in cartItems" :key="item.id">
        <!-- Checkbox -->
        <view class="checkbox" :class="{ checked: selectedIds.includes(item.id) }" @click="toggleSelect(item.id)">
          <text v-if="selectedIds.includes(item.id)">✓</text>
        </view>

        <!-- Product image -->
        <image class="item-image" :src="item._imageUrl || '/static/product-placeholder.png'" mode="aspectFill" />

        <!-- Info -->
        <view class="item-info">
          <text class="item-name">{{ item._productName || '商品' }}</text>
          <view class="item-price-row">
            <text class="price">¥{{ item._price || '0' }}</text>
            <text class="employee-price" v-if="isEmployee && item._employeePrice">
              员工价 ¥{{ item._employeePrice }}
            </text>
          </view>
          <view class="item-subtotal-row">
            <text class="subtotal-label">小计: </text>
            <text class="subtotal-price">¥{{ item._subtotal }}</text>
          </view>
          <view class="quantity-control">
            <view class="qty-btn" @click="changeQuantity(item.id, 'minus')">-</view>
            <text class="qty-num">{{ item.quantity }}</text>
            <view class="qty-btn" @click="changeQuantity(item.id, 'add')">+</view>
            <view class="delete-btn" @click="deleteItem(item.id)">删除</view>
          </view>
        </view>
      </view>

      <!-- Coupon Section -->
      <view class="coupon-section" v-if="!isEmployee && availableCoupons.length > 0">
        <view class="coupon-header" @click="toggleCouponPicker">
          <text class="coupon-label">🎫 优惠券</text>
          <text class="coupon-selected" v-if="selectedCouponId">已选 -¥{{ selectedCouponDiscount }}</text>
          <text class="coupon-selected" v-else>{{ availableCoupons.length }}张可用</text>
          <text class="menu-arrow">></text>
        </view>
        <view class="coupon-picker" v-if="showCouponPicker">
          <view class="coupon-option" v-for="c in availableCoupons" :key="c.id" @click="selectCoupon(c.id)">
            <view class="coupon-option-left">
              <text class="coupon-option-amount">¥{{ c.discount }}</text>
            </view>
            <view class="coupon-option-right">
              <text class="coupon-option-name">{{ c.couponName }}</text>
              <text class="coupon-option-condition">满{{ c.minAmount }}元可用</text>
            </view>
            <text class="coupon-check" v-if="selectedCouponId === c.id">✓</text>
          </view>
          <view class="coupon-option" @click="clearCoupon">
            <text class="coupon-option-name" style="color: #999;">不使用优惠券</text>
          </view>
        </view>
      </view>

      <!-- Employee discount notice -->
      <view class="employee-notice" v-if="isEmployee && hasEmployeeDiscount">
        <text>🏷️ 员工折扣已应用 | 原价 ¥{{ totalPrice }} → 折后 ¥{{ employeeTotal }}</text>
      </view>

      <!-- Cannot use coupon notice for employees -->
      <view class="employee-notice" v-if="isEmployee && availableCoupons.length > 0">
        <text>💡 员工折扣与优惠券不可叠加使用</text>
      </view>

      <!-- Bottom Bar -->
      <view class="bottom-bar">
        <view class="select-all" @click="toggleSelectAll">
          <view class="checkbox" :class="{ checked: selectedIds.length === cartItems.length }">
            <text v-if="selectedIds.length === cartItems.length">✓</text>
          </view>
          <text>全选</text>
        </view>
        <view class="total">
          <text>合计: </text>
          <text class="price">¥{{ finalPrice }}</text>
        </view>
        <button class="checkout-btn" @click="checkout">结算 ({{ selectedIds.length }})</button>
      </view>
    </view>

    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import * as api from '../../utils/api.js'

const cartItems = ref([])
const productMap = ref({})
const loading = ref(true)
const totalPrice = ref('0.00')
const selectedIds = ref([])
const isEmployee = ref(false)
const employeeTotal = ref('0.00')
const hasEmployeeDiscount = ref(false)
const availableCoupons = ref([])
const selectedCouponId = ref(null)
const selectedCouponDiscount = ref(0)
const showCouponPicker = ref(false)
const finalPrice = ref('0.00')

async function loadCart() {
  const app = getApp()
  if (!app.globalData.userInfo) {
    loading.value = false
    cartItems.value = []
    return
  }
  loading.value = true
  try {
    const userInfo = app.globalData.userInfo
    isEmployee.value = Boolean(userInfo.isHotelEmployee)

    const res = await api.getCartByUserId(userInfo.id)
    const rawCartItems = res.data || []

    const pMap = {}
    await Promise.all(rawCartItems.map(item =>
      api.getProductById(item.productId).then(pRes => {
        if (pRes.data) pMap[item.productId] = pRes.data
      }).catch(() => {})
    ))

    const items = rawCartItems.map(item => {
      const product = pMap[item.productId]
      const price = product ? product.price : 0
      const hasDiscount = isEmployee.value && product && product.employeeDiscountRate
      const effectivePrice = hasDiscount ? (price * product.employeeDiscountRate) : price
      return {
        ...item,
        _productName: product ? product.name : '商品',
        _price: price.toFixed(2),
        _employeePrice: hasDiscount ? (price * product.employeeDiscountRate).toFixed(2) : '',
        _subtotal: (effectivePrice * item.quantity).toFixed(2),
        _imageUrl: product && product.image ? api.getFileUrl(product.image) : ''
      }
    })

    const selIds = items.map(item => item.id)

    let coupons = []
    try {
      const couponRes = await api.getUserCouponsByUserIdAndStatus(userInfo.id, 'AVAILABLE')
      const userCoupons = couponRes.data || []
      const couponIds = [...new Set(userCoupons.map(uc => uc.couponId).filter(Boolean))]
      const couponMap = {}
      await Promise.all(couponIds.map(id =>
        api.getCouponById(id).then(cRes => {
          if (cRes.data) couponMap[id] = cRes.data
        }).catch(() => {})
      ))
      coupons = userCoupons.map(uc => ({
        ...uc,
        couponName: couponMap[uc.couponId] ? couponMap[uc.couponId].name : '优惠券',
        discount: couponMap[uc.couponId] ? couponMap[uc.couponId].discount : 0,
        minAmount: couponMap[uc.couponId] ? couponMap[uc.couponId].minAmount : 0
      }))
    } catch (e) {
      console.error('Failed to load coupons:', e)
    }

    cartItems.value = items
    productMap.value = pMap
    selectedIds.value = selIds
    availableCoupons.value = coupons
    selectedCouponId.value = null
    selectedCouponDiscount.value = 0
    loading.value = false
    calculateTotal()
  } catch (e) {
    console.error('Failed to load cart:', e)
    loading.value = false
  }
}

function toggleSelect(id) {
  const idx = selectedIds.value.indexOf(id)
  if (idx > -1) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
  calculateTotal()
}

function toggleSelectAll() {
  if (selectedIds.value.length === cartItems.value.length) {
    selectedIds.value = []
  } else {
    selectedIds.value = cartItems.value.map(item => item.id)
  }
  calculateTotal()
}

function calculateTotal() {
  let originalTotal = 0
  let empTotal = 0
  let hasEmpDiscount = false

  cartItems.value.forEach(item => {
    if (selectedIds.value.includes(item.id)) {
      const product = productMap.value[item.productId]
      if (product) {
        const itemOriginal = product.price * item.quantity
        originalTotal += itemOriginal
        if (isEmployee.value && product.employeeDiscountRate) {
          empTotal += product.price * product.employeeDiscountRate * item.quantity
          hasEmpDiscount = true
        } else {
          empTotal += itemOriginal
        }
      }
    }
  })

  let basePrice = isEmployee.value && hasEmpDiscount ? empTotal : originalTotal
  let couponDiscount = 0
  if (!isEmployee.value && selectedCouponId.value) {
    const coupon = availableCoupons.value.find(c => c.id === selectedCouponId.value)
    if (coupon && originalTotal >= coupon.minAmount) {
      couponDiscount = coupon.discount
    }
  }

  const final = Math.max(0, basePrice - couponDiscount)
  totalPrice.value = originalTotal.toFixed(2)
  employeeTotal.value = empTotal.toFixed(2)
  hasEmployeeDiscount.value = hasEmpDiscount
  selectedCouponDiscount.value = couponDiscount
  finalPrice.value = final.toFixed(2)
}

function toggleCouponPicker() {
  if (isEmployee.value) {
    uni.showToast({ title: '员工折扣与优惠券不可叠加', icon: 'none' })
    return
  }
  showCouponPicker.value = !showCouponPicker.value
}

function selectCoupon(couponId) {
  if (selectedCouponId.value === couponId) {
    selectedCouponId.value = null
  } else {
    selectedCouponId.value = couponId
  }
  showCouponPicker.value = false
  calculateTotal()
}

function clearCoupon() {
  selectedCouponId.value = null
  showCouponPicker.value = false
  calculateTotal()
}

async function changeQuantity(id, action) {
  const item = cartItems.value.find(i => i.id === id)
  if (!item) return
  let newQuantity = item.quantity
  if (action === 'add') {
    newQuantity++
  } else if (action === 'minus') {
    if (newQuantity <= 1) {
      deleteItem(id)
      return
    }
    newQuantity--
  }
  try {
    await api.updateCartItem({ id: item.id, userId: item.userId, productId: item.productId, quantity: newQuantity })
    loadCart()
  } catch (err) {
    uni.showToast({ title: '更新失败', icon: 'error' })
  }
}

function deleteItem(id) {
  uni.showModal({
    title: '提示',
    content: '确定要移除该商品吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await api.deleteCartItem(id)
          uni.showToast({ title: '已移除', icon: 'success' })
          loadCart()
        } catch (err) {
          uni.showToast({ title: '移除失败', icon: 'error' })
        }
      }
    }
  })
}

function doClearCart() {
  const app = getApp()
  if (!app.globalData.userInfo) return
  uni.showModal({
    title: '提示',
    content: '确定要清空购物车吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await api.clearCart(app.globalData.userInfo.id)
          uni.showToast({ title: '已清空', icon: 'success' })
          loadCart()
        } catch (err) {
          uni.showToast({ title: '清空失败', icon: 'error' })
        }
      }
    }
  })
}

function checkout() {
  const app = getApp()
  if (!app.globalData.userInfo) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  const selectedItems = cartItems.value.filter(item => selectedIds.value.includes(item.id))
  if (selectedItems.length === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }

  const totalForCoupon = parseFloat(totalPrice.value)
  if (!isEmployee.value && !selectedCouponId.value && availableCoupons.value.length > 0) {
    const usable = availableCoupons.value.filter(c => totalForCoupon >= c.minAmount)
    if (usable.length > 0) {
      uni.showModal({
        title: '提示',
        content: '您有' + usable.length + '张可用优惠券，是否使用？',
        confirmText: '去选券',
        cancelText: '不使用',
        success: (res) => {
          if (res.confirm) {
            showCouponPicker.value = true
          } else {
            doCheckout(selectedItems, null)
          }
        }
      })
      return
    }
  }
  doCheckout(selectedItems, (!isEmployee.value && selectedCouponId.value) ? selectedCouponId.value : null)
}

async function doCheckout(selectedItems, couponId) {
  const app = getApp()
  try {
    uni.showLoading({ title: '创建订单中...' })

    // Build multi-item order: one order with all selected products
    const items = selectedItems.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }))

    const res = await api.addMultiItemOrder(app.globalData.userInfo.id, items, couponId)
    const order = res.data

    // Delete cart items after order creation
    for (const item of selectedItems) {
      await api.deleteCartItem(item.id)
    }

    uni.hideLoading()

    if (selectedCouponId.value) {
      try {
        await api.updateUserCoupon({
          id: selectedCouponId.value,
          status: 'USED',
          useTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
        })
      } catch (err) {
        console.error('Failed to mark coupon as used:', err)
      }
    }

    if (order && order.id) {
      uni.navigateTo({ url: '/pages/payment/payment?orderId=' + order.id + '&amount=' + (order.totalAmount || finalPrice.value) })
    } else {
      uni.showToast({ title: '下单成功', icon: 'success' })
      setTimeout(() => {
        loadCart()
        uni.switchTab({ url: '/pages/orders/orders' })
      }, 1500)
    }
  } catch (err) {
    uni.hideLoading()
    uni.showToast({ title: '下单失败', icon: 'error' })
  }
}

function goShopping() {
  uni.switchTab({ url: '/pages/index/index' })
}

onShow(() => { loadCart() })
</script>

<style scoped>
.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
}

.cart-title {
  font-size: 32rpx;
  font-weight: bold;
}

.clear-btn {
  font-size: 26rpx;
  color: #ee0a24;
}

.cart-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 20rpx;
  margin: 0 20rpx 16rpx;
  border-radius: 12rpx;
  box-shadow: 0 4rpx 16rpx rgba(60, 50, 38, 0.06);
  animation: fadeIn 0.4s ease-out;
}

.checkbox {
  width: 44rpx;
  height: 44rpx;
  border: 2rpx solid var(--color-border);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
  font-size: 24rpx;
  color: #fff;
}

.checkbox.checked {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
}

.item-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  background-color: #F5EDE0;
  margin-right: 16rpx;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 8rpx;
}

.item-info .price {
  font-size: 30rpx;
  margin-bottom: 12rpx;
}

.item-price-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.item-price-row .price { font-size: 30rpx; }

.employee-price {
  font-size: 22rpx;
  color: #d48806;
  background-color: #fff9e6;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
}

.quantity-control {
  display: flex;
  align-items: center;
}

.qty-btn {
  width: 52rpx;
  height: 52rpx;
  border: 1rpx solid var(--color-border);
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: var(--color-text);
  background-color: var(--color-surface);
}

.qty-num {
  width: 60rpx;
  text-align: center;
  font-size: 28rpx;
}

.delete-btn {
  margin-left: auto;
  font-size: 24rpx;
  color: #ee0a24;
  padding: 8rpx 16rpx;
}

/* Item subtotal */
.item-subtotal-row {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.subtotal-label {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

.subtotal-price {
  font-size: 28rpx;
  color: var(--color-accent);
  font-weight: 500;
}

/* Bottom Bar */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(60, 50, 38, 0.06);
  z-index: 100;
}

.select-all {
  display: flex;
  align-items: center;
  margin-right: 24rpx;
  font-size: 26rpx;
}

.select-all .checkbox {
  margin-right: 8rpx;
}

.total {
  flex: 1;
  font-size: 26rpx;
}

.total .price { font-size: 34rpx; }

.checkout-btn {
  background: linear-gradient(135deg, #C9A96E, #B8956A);
  color: #fff;
  font-size: 28rpx;
  border-radius: 40rpx;
  padding: 16rpx 40rpx;
  border: none;
  line-height: 1.4;
}

.checkout-btn::after { border: none; }

.go-shop-btn {
  margin-top: 30rpx;
  width: 300rpx;
}

/* Coupon Section */
.coupon-section {
  margin: 16rpx 20rpx;
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(60, 50, 38, 0.06);
}

.coupon-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid var(--color-border);
}

.coupon-label {
  font-size: 28rpx;
  font-weight: 500;
  flex: 1;
}

.coupon-selected {
  font-size: 24rpx;
  color: var(--color-accent);
  margin-right: 8rpx;
}

.coupon-picker { padding: 0 24rpx; }

.coupon-option {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid var(--color-surface);
}

.coupon-option-left {
  width: 120rpx;
  text-align: center;
}

.coupon-option-amount {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--color-accent);
}

.coupon-option-right {
  flex: 1;
  padding-left: 16rpx;
}

.coupon-option-name {
  font-size: 26rpx;
  color: var(--color-text);
  display: block;
}

.coupon-option-condition {
  font-size: 22rpx;
  color: var(--color-text-secondary);
  display: block;
  margin-top: 4rpx;
}

.coupon-check {
  font-size: 32rpx;
  color: var(--color-primary);
  font-weight: bold;
}

/* Employee Notice */
.employee-notice {
  margin: 12rpx 20rpx;
  padding: 16rpx 20rpx;
  background-color: #fff9e6;
  border-radius: 8rpx;
  border: 1rpx solid #ffe58f;
  font-size: 24rpx;
  color: #d48806;
}

.menu-arrow {
  font-size: 28rpx;
  color: #ccc;
}
</style>
