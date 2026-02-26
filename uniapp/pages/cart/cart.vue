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

<script>
import * as api from '../../utils/api.js'

export default {
  data() {
    return {
      cartItems: [],
      productMap: {},
      loading: true,
      totalPrice: '0.00',
      selectedIds: [],
      isEmployee: false,
      employeeTotal: '0.00',
      hasEmployeeDiscount: false,
      availableCoupons: [],
      selectedCouponId: null,
      selectedCouponDiscount: 0,
      showCouponPicker: false,
      finalPrice: '0.00'
    }
  },
  onShow() {
    this.loadCart()
  },
  methods: {
    async loadCart() {
      const app = getApp()
      if (!app.globalData.userInfo) {
        this.loading = false
        this.cartItems = []
        return
      }
      this.loading = true
      try {
        const userInfo = app.globalData.userInfo
        this.isEmployee = Boolean(userInfo.isHotelEmployee)

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
          const hasDiscount = this.isEmployee && product && product.employeeDiscountRate
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

        this.cartItems = items
        this.productMap = pMap
        this.selectedIds = selIds
        this.availableCoupons = coupons
        this.selectedCouponId = null
        this.selectedCouponDiscount = 0
        this.loading = false
        this.calculateTotal()
      } catch (e) {
        console.error('Failed to load cart:', e)
        this.loading = false
      }
    },

    toggleSelect(id) {
      const idx = this.selectedIds.indexOf(id)
      if (idx > -1) {
        this.selectedIds.splice(idx, 1)
      } else {
        this.selectedIds.push(id)
      }
      this.calculateTotal()
    },

    toggleSelectAll() {
      if (this.selectedIds.length === this.cartItems.length) {
        this.selectedIds = []
      } else {
        this.selectedIds = this.cartItems.map(item => item.id)
      }
      this.calculateTotal()
    },

    calculateTotal() {
      let originalTotal = 0
      let empTotal = 0
      let hasEmpDiscount = false

      this.cartItems.forEach(item => {
        if (this.selectedIds.includes(item.id)) {
          const product = this.productMap[item.productId]
          if (product) {
            const itemOriginal = product.price * item.quantity
            originalTotal += itemOriginal
            if (this.isEmployee && product.employeeDiscountRate) {
              empTotal += product.price * product.employeeDiscountRate * item.quantity
              hasEmpDiscount = true
            } else {
              empTotal += itemOriginal
            }
          }
        }
      })

      let basePrice = this.isEmployee && hasEmpDiscount ? empTotal : originalTotal
      let couponDiscount = 0
      if (!this.isEmployee && this.selectedCouponId) {
        const coupon = this.availableCoupons.find(c => c.id === this.selectedCouponId)
        if (coupon && originalTotal >= coupon.minAmount) {
          couponDiscount = coupon.discount
        }
      }

      const final = Math.max(0, basePrice - couponDiscount)
      this.totalPrice = originalTotal.toFixed(2)
      this.employeeTotal = empTotal.toFixed(2)
      this.hasEmployeeDiscount = hasEmpDiscount
      this.selectedCouponDiscount = couponDiscount
      this.finalPrice = final.toFixed(2)
    },

    toggleCouponPicker() {
      if (this.isEmployee) {
        uni.showToast({ title: '员工折扣与优惠券不可叠加', icon: 'none' })
        return
      }
      this.showCouponPicker = !this.showCouponPicker
    },

    selectCoupon(couponId) {
      if (this.selectedCouponId === couponId) {
        this.selectedCouponId = null
      } else {
        this.selectedCouponId = couponId
      }
      this.showCouponPicker = false
      this.calculateTotal()
    },

    clearCoupon() {
      this.selectedCouponId = null
      this.showCouponPicker = false
      this.calculateTotal()
    },

    async changeQuantity(id, action) {
      const item = this.cartItems.find(i => i.id === id)
      if (!item) return
      let newQuantity = item.quantity
      if (action === 'add') {
        newQuantity++
      } else if (action === 'minus') {
        if (newQuantity <= 1) {
          this.deleteItem(id)
          return
        }
        newQuantity--
      }
      try {
        await api.updateCartItem({ id: item.id, userId: item.userId, productId: item.productId, quantity: newQuantity })
        this.loadCart()
      } catch (err) {
        uni.showToast({ title: '更新失败', icon: 'error' })
      }
    },

    deleteItem(id) {
      uni.showModal({
        title: '提示',
        content: '确定要移除该商品吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await api.deleteCartItem(id)
              uni.showToast({ title: '已移除', icon: 'success' })
              this.loadCart()
            } catch (err) {
              uni.showToast({ title: '移除失败', icon: 'error' })
            }
          }
        }
      })
    },

    doClearCart() {
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
              this.loadCart()
            } catch (err) {
              uni.showToast({ title: '清空失败', icon: 'error' })
            }
          }
        }
      })
    },

    checkout() {
      const app = getApp()
      if (!app.globalData.userInfo) {
        uni.navigateTo({ url: '/pages/login/login' })
        return
      }
      const selectedItems = this.cartItems.filter(item => this.selectedIds.includes(item.id))
      if (selectedItems.length === 0) {
        uni.showToast({ title: '请选择商品', icon: 'none' })
        return
      }

      // Require store selection for checkout
      const storeId = app.globalData.selectedStoreId
      if (!storeId) {
        uni.showToast({ title: '请先在首页选择店铺', icon: 'none' })
        return
      }

      const totalForCoupon = parseFloat(this.totalPrice)
      if (!this.isEmployee && !this.selectedCouponId && this.availableCoupons.length > 0) {
        const usable = this.availableCoupons.filter(c => totalForCoupon >= c.minAmount)
        if (usable.length > 0) {
          uni.showModal({
            title: '提示',
            content: '您有' + usable.length + '张可用优惠券，是否使用？',
            confirmText: '去选券',
            cancelText: '不使用',
            success: (res) => {
              if (res.confirm) {
                this.showCouponPicker = true
              } else {
                this.doCheckout(selectedItems, null)
              }
            }
          })
          return
        }
      }
      this.doCheckout(selectedItems, (!this.isEmployee && this.selectedCouponId) ? this.selectedCouponId : null)
    },

    async doCheckout(selectedItems, couponId) {
      const app = getApp()
      try {
        uni.showLoading({ title: '创建订单中...' })

        // Build multi-item order: one order with all selected products
        const items = selectedItems.map(item => ({
          productId: item.productId,
          quantity: item.quantity
        }))

        const res = await api.addMultiItemOrder(app.globalData.userInfo.id, items, couponId, app.globalData.selectedStoreId || null)
        const order = res.data

        // Delete cart items after order creation
        for (const item of selectedItems) {
          await api.deleteCartItem(item.id)
        }

        uni.hideLoading()

        if (this.selectedCouponId) {
          try {
            await api.updateUserCoupon({
              id: this.selectedCouponId,
              status: 'USED',
              useTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
            })
          } catch (err) {
            console.error('Failed to mark coupon as used:', err)
          }
        }

        if (order && order.id) {
          uni.navigateTo({ url: '/pages/payment/payment?orderId=' + order.id + '&amount=' + (order.totalAmount || this.finalPrice) })
        } else {
          uni.showToast({ title: '下单成功', icon: 'success' })
          setTimeout(() => {
            this.loadCart()
            uni.switchTab({ url: '/pages/orders/orders' })
          }, 1500)
        }
      } catch (err) {
        uni.hideLoading()
        uni.showToast({ title: '下单失败', icon: 'error' })
      }
    },

    goShopping() {
      uni.switchTab({ url: '/pages/index/index' })
    }
  }
}
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
