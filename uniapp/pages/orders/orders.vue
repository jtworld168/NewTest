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

    <!-- Order List -->
    <view class="order-list" v-if="!loading">
      <template v-for="item in orders" :key="item.id">
        <view class="order-card" v-if="currentTab === 'all' || item.status === currentTab" @click="goToOrderDetail(item.id)">
          <view class="order-header">
            <text class="order-id">订单号: {{ item.id }}</text>
            <text class="order-store" v-if="item._storeName">{{ item._storeName }}</text>
            <text class="order-status" :class="'status-' + item.status">
              {{ statusText(item.status) }}
            </text>
          </view>

          <view class="order-body">
            <image class="order-product-image" :src="item._imageUrl || '/static/product-placeholder.png'" mode="aspectFill" />
            <view class="order-product-info">
              <text class="order-product-name">{{ item._productName || '商品' }}</text>
              <text class="order-quantity">x{{ item.quantity }}</text>
              <text class="order-price">单价: ¥{{ item.priceAtPurchase }}</text>
              <text class="order-barcode" v-if="item._barcode">条形码: {{ item._barcode }}</text>
            </view>
          </view>

          <view class="order-footer">
            <text class="order-time" v-if="item.createTime">{{ item.createTime }}</text>
            <view class="order-total">
              <text class="order-discount-tag" v-if="item._hasEmployeeDiscount">员工折扣</text>
              <text class="order-coupon-tag" v-if="item.userCouponId">已用券</text>
              <text>合计: </text>
              <text class="price">¥{{ item.totalAmount }}</text>
            </view>
          </view>

          <!-- Action buttons for pending orders -->
          <view class="order-actions" v-if="item.status === 'PENDING'" @click.stop>
            <button class="btn-cancel" @click.stop="cancelOrder(item.id)">取消订单</button>
            <button class="btn-pay" @click.stop="goToPay(item.id, item.totalAmount)">去支付</button>
          </view>
        </view>
      </template>
    </view>

    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>

    <view class="empty-state" v-if="!loading && orders.length === 0">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无订单</text>
    </view>
  </view>
</template>

<script>
import * as api from '../../utils/api.js'

export default {
  data() {
    return {
      orders: [],
      loading: true,
      currentTab: 'all',
      tabs: [
        { key: 'all', name: '全部' },
        { key: 'PENDING', name: '待支付' },
        { key: 'PAID', name: '已支付' },
        { key: 'COMPLETED', name: '已完成' },
        { key: 'CANCELLED', name: '已取消' }
      ]
    }
  },
  onShow() {
    this.loadOrders()
  },
  onPullDownRefresh() {
    this.loadOrders().then(() => uni.stopPullDownRefresh())
  },
  methods: {
    statusText(status) {
      const map = { PENDING: '待支付', PAID: '已支付', COMPLETED: '已完成', CANCELLED: '已取消' }
      return map[status] || status
    },
    async loadOrders() {
      const app = getApp()
      if (!app.globalData.userInfo) {
        this.loading = false
        this.orders = []
        return
      }
      this.loading = true
      try {
        const res = await api.getOrdersByUserId(app.globalData.userInfo.id)
        const orderList = res.data || []

        const productIds = [...new Set(orderList.map(o => o.productId).filter(Boolean))]
        const productMap = {}
        await Promise.all(productIds.map(id =>
          api.getProductById(id).then(pRes => {
            if (pRes.data) productMap[id] = pRes.data
          }).catch(() => {})
        ))

        const storeIds = [...new Set(orderList.map(o => o.storeId).filter(Boolean))]
        const storeMap = {}
        await Promise.all(storeIds.map(id =>
          api.getStoreById(id).then(sRes => {
            if (sRes.data) storeMap[id] = sRes.data
          }).catch(() => {})
        ))

        orderList.forEach(order => {
          const product = productMap[order.productId]
          order._productName = product ? product.name : '商品 #' + order.productId
          order._imageUrl = product && product.image ? api.getFileUrl(product.image) : ''
          order._barcode = product ? (product.barcode || '') : ''
          order._hasEmployeeDiscount = product && order.priceAtPurchase && order.priceAtPurchase < product.price
          const store = order.storeId ? storeMap[order.storeId] : null
          order._storeName = store ? store.name : ''
        })

        this.orders = orderList
        this.loading = false
      } catch (e) {
        console.error('Failed to load orders:', e)
        this.loading = false
      }
    },
    switchTab(tab) {
      this.currentTab = tab
    },
    goToOrderDetail(id) {
      uni.navigateTo({ url: '/pages/order-detail/order-detail?id=' + id })
    },
    goToPay(id, amount) {
      uni.navigateTo({ url: '/pages/payment/payment?orderId=' + id + '&amount=' + amount })
    },
    cancelOrder(id) {
      uni.showModal({
        title: '提示',
        content: '确定要取消该订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await api.updateOrder({ id, status: 'CANCELLED' })
              uni.showToast({ title: '已取消', icon: 'success' })
              this.loadOrders()
            } catch (err) {
              uni.showToast({ title: '取消失败', icon: 'error' })
            }
          }
        }
      })
    }
  }
}
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
  font-size: 26rpx;
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

.order-list { padding: 20rpx; }

.order-card {
  background-color: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(60, 50, 38, 0.06);
  animation: fadeIn 0.4s ease-out;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid var(--color-border);
}

.order-id {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

.order-store {
  font-size: 24rpx;
  color: var(--color-primary);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.order-status {
  font-size: 26rpx;
  font-weight: 500;
}

.order-body {
  display: flex;
  padding: 20rpx 24rpx;
}

.order-product-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  background-color: #F5EDE0;
  margin-right: 20rpx;
}

.order-product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.order-product-name {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text);
}

.order-quantity {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

.order-price {
  font-size: 24rpx;
  color: var(--color-text-secondary);
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 24rpx;
  border-top: 1rpx solid var(--color-border);
}

.order-time {
  font-size: 22rpx;
  color: var(--color-text-secondary);
}

.order-total { font-size: 26rpx; }
.order-total .price { font-size: 32rpx; }

.order-discount-tag {
  font-size: 20rpx;
  color: #d48806;
  background-color: #fff9e6;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  margin-right: 8rpx;
  border: 1rpx solid #ffe58f;
}

.order-coupon-tag {
  font-size: 20rpx;
  color: #ee0a24;
  background-color: #fff1f0;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  margin-right: 8rpx;
  border: 1rpx solid #ffccc7;
}

/* Status colors */
.status-PENDING { color: #ff9800; }
.status-PAID { color: #6BAF7B; }
.status-COMPLETED { color: var(--color-text-secondary); }
.status-CANCELLED { color: #ee0a24; }

/* Order action buttons */
.order-actions {
  display: flex;
  justify-content: flex-end;
  padding: 16rpx 24rpx;
  border-top: 1rpx solid var(--color-border);
  gap: 16rpx;
}

.btn-cancel {
  font-size: 24rpx;
  padding: 12rpx 32rpx;
  background-color: #fff;
  color: var(--color-text-secondary);
  border: 1rpx solid var(--color-border);
  border-radius: 32rpx;
  line-height: 1.4;
}

.btn-cancel::after { border: none; }

.btn-pay {
  font-size: 24rpx;
  padding: 12rpx 32rpx;
  background: linear-gradient(135deg, #C9A96E, #B8956A);
  color: #fff;
  border: none;
  border-radius: 32rpx;
  line-height: 1.4;
}

.btn-pay::after { border: none; }

.order-barcode {
  font-size: 22rpx;
  color: var(--color-text-secondary);
  margin-top: 4rpx;
}
</style>
