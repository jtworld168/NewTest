<template>
  <view class="container">
    <view class="pay-card">
      <view class="pay-icon">💳</view>
      <text class="pay-title">订单支付</text>
      <text class="pay-order-id">订单号: {{ orderId }}</text>
      <text class="pay-amount">¥{{ amount }}</text>

      <!-- Payment method selection -->
      <view class="pay-methods">
        <text class="pay-methods-title">选择支付方式</text>
        <view class="pay-method" :class="{ selected: paymentMethod === 'WECHAT' }" @click="selectMethod('WECHAT')">
          <text class="method-icon">💚</text>
          <text class="method-name">微信支付</text>
          <text class="method-check" v-if="paymentMethod === 'WECHAT'">✓</text>
        </view>
        <view class="pay-method" :class="{ selected: paymentMethod === 'ALIPAY' }" @click="selectMethod('ALIPAY')">
          <text class="method-icon">💙</text>
          <text class="method-name">支付宝</text>
          <text class="method-check" v-if="paymentMethod === 'ALIPAY'">✓</text>
        </view>
        <view class="pay-method" :class="{ selected: paymentMethod === 'CARD' }" @click="selectMethod('CARD')">
          <text class="method-icon">💳</text>
          <text class="method-name">银行卡</text>
          <text class="method-check" v-if="paymentMethod === 'CARD'">✓</text>
        </view>
        <view class="pay-method" :class="{ selected: paymentMethod === 'CASH' }" @click="selectMethod('CASH')">
          <text class="method-icon">💰</text>
          <text class="method-name">现金</text>
          <text class="method-check" v-if="paymentMethod === 'CASH'">✓</text>
        </view>
      </view>
    </view>

    <button class="confirm-btn" @click="confirmPay" :disabled="paying">
      {{ paying ? '支付中...' : '确认支付 ¥' + amount }}
    </button>
  </view>
</template>

<script>
import * as api from '../../utils/api.js'

export default {
  data() {
    return {
      orderId: null,
      amount: '0.00',
      paymentMethod: 'WECHAT',
      paying: false
    }
  },
  onLoad(options) {
    this.orderId = options.orderId ? Number(options.orderId) : null
    this.amount = options.amount || '0.00'
  },
  methods: {
    selectMethod(method) {
      this.paymentMethod = method
    },
    async confirmPay() {
      if (this.paying) return
      if (!this.orderId) {
        uni.showToast({ title: '订单信息错误', icon: 'none' })
        return
      }

      this.paying = true
      try {
        const transactionNo = 'TXN' + Date.now() + Math.floor(Math.random() * 1000)
        await api.addPayment({
          orderId: this.orderId,
          amount: parseFloat(this.amount),
          paymentMethod: this.paymentMethod,
          paymentStatus: 'SUCCESS',
          transactionNo,
          paymentTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
        })

        await api.updateOrder({ id: this.orderId, status: 'PAID' })

        uni.showToast({ title: '支付成功', icon: 'success' })
        setTimeout(() => {
          uni.switchTab({ url: '/pages/orders/orders' })
        }, 1500)
      } catch (err) {
        console.error('Payment failed:', err)
        uni.showToast({ title: '支付失败', icon: 'error' })
      } finally {
        this.paying = false
      }
    }
  }
}
</script>

<style scoped>
.container {
  padding: 40rpx 30rpx;
  min-height: 100vh;
  background-color: var(--color-bg);
}

.pay-card {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  text-align: center;
  box-shadow: 0 4rpx 20rpx rgba(60, 50, 38, 0.06);
  animation: scaleIn 0.4s ease-out;
}

.pay-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.pay-title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: var(--color-text);
  margin-bottom: 12rpx;
}

.pay-order-id {
  display: block;
  font-size: 24rpx;
  color: var(--color-text-secondary);
  margin-bottom: 30rpx;
}

.pay-amount {
  display: block;
  font-size: 72rpx;
  font-weight: bold;
  color: var(--color-accent);
  margin-bottom: 40rpx;
}

/* Payment methods */
.pay-methods {
  text-align: left;
  border-top: 1rpx solid var(--color-border);
  padding-top: 30rpx;
}

.pay-methods-title {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 20rpx;
}

.pay-method {
  display: flex;
  align-items: center;
  padding: 24rpx 20rpx;
  border: 1rpx solid var(--color-border);
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}

.pay-method.selected {
  border-color: var(--color-primary);
  background-color: #FDF8F0;
}

.method-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.method-name {
  flex: 1;
  font-size: 28rpx;
  color: var(--color-text);
}

.method-check {
  font-size: 32rpx;
  color: var(--color-primary);
  font-weight: bold;
}

.confirm-btn {
  margin-top: 60rpx;
  background: linear-gradient(135deg, #C9A96E, #B8956A);
  color: #fff;
  font-size: 32rpx;
  border-radius: 44rpx;
  padding: 24rpx 0;
  border: none;
  width: 100%;
}

.confirm-btn::after { border: none; }

.confirm-btn[disabled] {
  background: #ccc;
  color: #fff;
}
</style>
