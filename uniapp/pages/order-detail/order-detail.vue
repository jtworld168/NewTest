<template>
  <view class="container" v-if="order">
    <!-- Status banner -->
    <view class="status-banner" :class="'status-' + order.status">
      <text class="status-icon">{{ statusIcon }}</text>
      <text class="status-text">{{ statusLabel }}</text>
    </view>

    <!-- Product info -->
    <view class="detail-card">
      <text class="card-title">商品信息</text>
      <view class="store-info" v-if="order._storeName">
        <text class="store-label">店铺: </text>
        <text class="store-name">{{ order._storeName }}</text>
      </view>
      <view class="product-row">
        <image class="product-image" :src="order._imageUrl || '/static/product-placeholder.png'" mode="aspectFill" />
        <view class="product-info">
          <text class="product-name">{{ order._productName || '商品' }}</text>
          <text class="product-price">单价: ¥{{ order.priceAtPurchase }}</text>
          <text class="product-qty">数量: x{{ order.quantity }}</text>
          <text class="product-barcode" v-if="order._barcode">条形码: {{ order._barcode }}</text>
        </view>
      </view>
    </view>

    <!-- Price breakdown -->
    <view class="detail-card">
      <text class="card-title">价格明细</text>
      <view class="price-row" v-if="order._originalPrice">
        <text class="price-label">商品原价</text>
        <text class="price-value">¥{{ order._originalPrice }}</text>
      </view>
      <view class="price-row" v-if="order._hasEmployeeDiscount">
        <text class="price-label">员工折扣</text>
        <text class="price-value discount">-¥{{ order._discountAmount }}</text>
      </view>
      <view class="price-row" v-if="order.userCouponId">
        <text class="price-label">优惠券抵扣</text>
        <text class="price-value discount">-¥{{ order._couponDiscount }}</text>
      </view>
      <view class="price-row total-row">
        <text class="price-label">实付金额</text>
        <text class="price-value total">¥{{ order.totalAmount }}</text>
      </view>
    </view>

    <!-- Order info -->
    <view class="detail-card">
      <text class="card-title">订单信息</text>
      <view class="info-row">
        <text class="info-label">订单编号</text>
        <text class="info-value">{{ order.id }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">创建时间</text>
        <text class="info-value">{{ order.createTime || '-' }}</text>
      </view>
      <view class="info-row" v-if="payment">
        <text class="info-label">支付方式</text>
        <text class="info-value">{{ paymentMethodText }}</text>
      </view>
      <view class="info-row" v-if="payment">
        <text class="info-label">支付时间</text>
        <text class="info-value">{{ payment.paymentTime || '-' }}</text>
      </view>
      <view class="info-row" v-if="payment">
        <text class="info-label">交易号</text>
        <text class="info-value">{{ payment.transactionNo || '-' }}</text>
      </view>
    </view>

    <!-- Action buttons -->
    <view class="actions" v-if="order.status === 'PENDING'">
      <button class="btn-cancel" @click="cancelOrder">取消订单</button>
      <button class="btn-pay" @click="goToPay">去支付</button>
    </view>
    <view class="actions" v-if="order.status === 'PAID'">
      <button class="btn-confirm" @click="confirmReceive">确认收货</button>
    </view>
  </view>

  <view class="loading" v-if="loading">
    <text>加载中...</text>
  </view>
</template>

<script>
import * as api from '../../utils/api.js'

export default {
  data() {
    return {
      order: null,
      payment: null,
      loading: true
    }
  },
  computed: {
    statusIcon() {
      if (!this.order) return ''
      const map = { PENDING: '⏳', PAID: '✅', COMPLETED: '🎉', CANCELLED: '❌' }
      return map[this.order.status] || ''
    },
    statusLabel() {
      if (!this.order) return ''
      const map = { PENDING: '待支付', PAID: '已支付', COMPLETED: '已完成', CANCELLED: '已取消' }
      return map[this.order.status] || this.order.status
    },
    paymentMethodText() {
      if (!this.payment) return ''
      const map = { WECHAT: '微信支付', ALIPAY: '支付宝', CARD: '银行卡', CASH: '现金' }
      return map[this.payment.paymentMethod] || this.payment.paymentMethod
    }
  },
  onLoad(options) {
    if (options.id) this.loadOrderDetail(Number(options.id))
  },
  methods: {
    async loadOrderDetail(orderId) {
      this.loading = true
      try {
        const res = await api.getOrderById(orderId)
        const o = res.data
        if (!o) {
          uni.showToast({ title: '订单不存在', icon: 'none' })
          this.loading = false
          return
        }

        let product = null
        if (o.productId) {
          try { product = (await api.getProductById(o.productId)).data } catch (e) {}
        }

        let store = null
        if (o.storeId) {
          try { store = (await api.getStoreById(o.storeId)).data } catch (e) {}
        }

        o._productName = product ? product.name : '商品 #' + o.productId
        o._imageUrl = product && product.image ? api.getFileUrl(product.image) : ''
        o._barcode = product ? (product.barcode || '') : ''
        o._storeName = store ? store.name : ''
        o._hasEmployeeDiscount = product && o.priceAtPurchase && o.priceAtPurchase < product.price
        o._originalPrice = product ? (product.price * o.quantity).toFixed(2) : ''
        o._discountAmount = o._hasEmployeeDiscount
          ? ((product.price * o.quantity) - (o.priceAtPurchase * o.quantity)).toFixed(2)
          : '0.00'
        o._couponDiscount = o.userCouponId ? ((o.priceAtPurchase * o.quantity) - o.totalAmount).toFixed(2) : '0.00'

        let paymentData = null
        try {
          const payRes = await api.getPaymentsByOrderId(orderId)
          const payments = payRes.data || []
          if (payments.length > 0) paymentData = payments[0]
        } catch (e) {}

        this.order = o
        this.payment = paymentData
        this.loading = false
      } catch (e) {
        console.error('Failed to load order detail:', e)
        this.loading = false
        uni.showToast({ title: '加载失败', icon: 'error' })
      }
    },
    goToPay() {
      uni.navigateTo({ url: '/pages/payment/payment?orderId=' + this.order.id + '&amount=' + this.order.totalAmount })
    },
    cancelOrder() {
      uni.showModal({
        title: '提示',
        content: '确定要取消该订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await api.updateOrder({ id: this.order.id, status: 'CANCELLED' })
              uni.showToast({ title: '已取消', icon: 'success' })
              this.loadOrderDetail(this.order.id)
            } catch (err) {
              uni.showToast({ title: '取消失败', icon: 'error' })
            }
          }
        }
      })
    },
    confirmReceive() {
      uni.showModal({
        title: '提示',
        content: '确认已收到商品？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await api.updateOrder({ id: this.order.id, status: 'COMPLETED' })
              uni.showToast({ title: '已确认收货', icon: 'success' })
              this.loadOrderDetail(this.order.id)
            } catch (err) {
              uni.showToast({ title: '操作失败', icon: 'error' })
            }
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.container {
  padding: 0 0 120rpx;
  background-color: var(--color-bg);
  min-height: 100vh;
}

/* Status Banner */
.status-banner {
  padding: 40rpx 30rpx;
  display: flex;
  align-items: center;
  animation: fadeIn 0.4s ease-out;
}

.status-PENDING { background: linear-gradient(135deg, #ff9800, #f57c00); }
.status-PAID { background: linear-gradient(135deg, #6BAF7B, #5A9E6A); }
.status-COMPLETED { background: linear-gradient(135deg, #999, #777); }
.status-CANCELLED { background: linear-gradient(135deg, #ee0a24, #c7001a); }

.status-icon {
  font-size: 48rpx;
  margin-right: 16rpx;
}

.status-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

/* Detail Card */
.detail-card {
  background-color: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(60, 50, 38, 0.06);
  animation: slideUp 0.35s ease-out backwards;
}

.card-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: var(--color-text);
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid var(--color-border);
}

.store-info {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.store-label {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

.store-name {
  font-size: 26rpx;
  color: var(--color-primary);
  font-weight: 500;
}

/* Product Row */
.product-row {
  display: flex;
  align-items: center;
}

.product-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
  background-color: #F5EDE0;
  margin-right: 20rpx;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.product-name {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text);
}

.product-price {
  font-size: 26rpx;
  color: var(--color-text-secondary);
}

.product-qty {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

.product-barcode {
  font-size: 24rpx;
  color: var(--color-text-secondary);
  margin-top: 8rpx;
}

/* Price Rows */
.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
}

.price-label {
  font-size: 26rpx;
  color: var(--color-text-secondary);
}

.price-value {
  font-size: 26rpx;
  color: var(--color-text);
}

.price-value.discount { color: #ee0a24; }

.total-row {
  border-top: 1rpx solid var(--color-border);
  padding-top: 20rpx;
  margin-top: 8rpx;
}

.price-value.total {
  font-size: 36rpx;
  font-weight: bold;
  color: var(--color-accent);
}

/* Info Rows */
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
}

.info-label {
  font-size: 26rpx;
  color: var(--color-text-secondary);
}

.info-value {
  font-size: 26rpx;
  color: var(--color-text);
  max-width: 400rpx;
  text-align: right;
  word-break: break-all;
}

/* Actions */
.actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(60, 50, 38, 0.06);
  gap: 20rpx;
}

.btn-cancel {
  flex: 1;
  font-size: 28rpx;
  padding: 20rpx;
  background-color: #fff;
  color: var(--color-text-secondary);
  border: 1rpx solid var(--color-border);
  border-radius: 40rpx;
  line-height: 1.4;
}

.btn-cancel::after { border: none; }

.btn-pay, .btn-confirm {
  flex: 1;
  font-size: 28rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #C9A96E, #B8956A);
  color: #fff;
  border: none;
  border-radius: 40rpx;
  line-height: 1.4;
}

.btn-pay::after, .btn-confirm::after { border: none; }
</style>
