<template>
  <view class="scan-page">
    <!-- 扫码界面 (native scanner will open, this is the fallback/result UI) -->
    <view v-if="!scanResult" class="scanner-view">
      <!-- 模拟扫码界面 -->
      <view class="scan-overlay">
        <view class="overlay-top"></view>
        <view class="overlay-middle">
          <view class="overlay-side"></view>
          <view class="scan-frame">
            <view class="corner corner-tl"></view>
            <view class="corner corner-tr"></view>
            <view class="corner corner-bl"></view>
            <view class="corner corner-br"></view>
            <view class="scan-line"></view>
          </view>
          <view class="overlay-side"></view>
        </view>
        <view class="overlay-bottom">
          <text class="scan-tip">将二维码/条形码放入框内，即可自动扫描</text>
          <view class="action-btns">
            <view class="action-btn" @click="doScan">
              <text class="action-icon">📷</text>
              <text class="action-text">开始扫码</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 扫码结果 -->
    <view v-if="scanResult" class="result-container">
      <view v-if="foundProduct" class="result-card">
        <image v-if="foundProduct.imageUrl" :src="foundProduct.imageUrl" class="product-img" mode="aspectFit" />
        <view v-else class="product-placeholder">📦</view>
        <text class="product-name">{{ foundProduct.name }}</text>
        <text class="product-price">¥{{ foundProduct.price }}</text>
        <text class="product-barcode">条码：{{ scanCode }}</text>
      </view>
      <view v-else class="result-card not-found">
        <text class="not-found-icon">❌</text>
        <text class="not-found-text">未找到商品</text>
        <text class="not-found-code">扫码结果：{{ scanCode }}</text>
      </view>
      <view class="result-actions">
        <button class="btn-rescan" @click="rescan">重新扫码</button>
        <button class="btn-back" @click="goBack">返回首页</button>
      </view>
    </view>
  </view>
</template>

<script>
import * as api from '../../utils/api.js'

export default {
  data() {
    return {
      scanResult: false,
      scanCode: '',
      foundProduct: null
    }
  },
  onShow() {
    this.scanResult = false
    this.scanCode = ''
    this.foundProduct = null
    this.doScan()
  },
  methods: {
    doScan() {
      this.scanResult = false
      uni.scanCode({
        onlyFromCamera: true,
        success: (res) => {
          const code = res.result
          this.scanCode = code
          uni.showLoading({ title: '查找商品...' })
          api.searchProducts(code).then(searchRes => {
            uni.hideLoading()
            const products = searchRes.data || []
            if (products.length > 0) {
              this.foundProduct = products[0]
            } else {
              this.foundProduct = null
            }
            this.scanResult = true
          }).catch(() => {
            uni.hideLoading()
            this.foundProduct = null
            this.scanResult = true
          })
        },
        fail: () => {
          uni.switchTab({ url: '/pages/index/index' })
        }
      })
    },
    rescan() {
      this.scanResult = false
      this.scanCode = ''
      this.foundProduct = null
      this.doScan()
    },
    goBack() {
      uni.switchTab({ url: '/pages/index/index' })
    }
  }
}
</script>

<style scoped>
.scan-page {
  width: 100%;
  height: 100vh;
  background: #000;
}

.scanner-view {
  width: 100%;
  height: 100vh;
  position: relative;
}

.scan-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.overlay-top {
  flex: 1;
  background: rgba(0, 0, 0, 0.6);
}

.overlay-middle {
  display: flex;
  flex-direction: row;
  height: 500rpx;
}

.overlay-side {
  flex: 1;
  background: rgba(0, 0, 0, 0.6);
}

.scan-frame {
  width: 500rpx;
  height: 500rpx;
  position: relative;
  background: rgba(255, 255, 255, 0.05);
}

.corner {
  position: absolute;
  width: 40rpx;
  height: 40rpx;
}
.corner-tl { top: 0; left: 0; border-top: 6rpx solid #C9A96E; border-left: 6rpx solid #C9A96E; }
.corner-tr { top: 0; right: 0; border-top: 6rpx solid #C9A96E; border-right: 6rpx solid #C9A96E; }
.corner-bl { bottom: 0; left: 0; border-bottom: 6rpx solid #C9A96E; border-left: 6rpx solid #C9A96E; }
.corner-br { bottom: 0; right: 0; border-bottom: 6rpx solid #C9A96E; border-right: 6rpx solid #C9A96E; }

.scan-line {
  position: absolute;
  left: 10rpx;
  right: 10rpx;
  height: 4rpx;
  background: linear-gradient(90deg, transparent, #C9A96E, transparent);
  animation: scanMove 2s linear infinite;
}

@keyframes scanMove {
  0% { top: 10rpx; }
  100% { top: 480rpx; }
}

.overlay-bottom {
  flex: 1;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40rpx;
}

.scan-tip {
  color: rgba(255, 255, 255, 0.8);
  font-size: 26rpx;
}

.action-btns {
  margin-top: 50rpx;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.action-icon {
  font-size: 64rpx;
}

.action-text {
  color: rgba(255, 255, 255, 0.7);
  font-size: 24rpx;
  margin-top: 12rpx;
}

/* 结果页 */
.result-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: #F5EDE0;
  padding: 40rpx;
}

.result-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 80%;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.product-img {
  width: 300rpx;
  height: 300rpx;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
}

.product-placeholder {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.product-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.product-price {
  font-size: 40rpx;
  color: #C9A96E;
  font-weight: bold;
  margin-bottom: 12rpx;
}

.product-barcode {
  font-size: 24rpx;
  color: #999;
}

.not-found { padding: 80rpx 40rpx; }
.not-found-icon { font-size: 100rpx; margin-bottom: 20rpx; }
.not-found-text { font-size: 32rpx; color: #999; margin-bottom: 12rpx; }
.not-found-code { font-size: 24rpx; color: #ccc; }

.result-actions {
  display: flex;
  gap: 30rpx;
  margin-top: 60rpx;
  width: 80%;
}

.btn-rescan {
  flex: 1;
  background: #C9A96E;
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
  height: 80rpx;
  line-height: 80rpx;
}

.btn-back {
  flex: 1;
  background: #eee;
  color: #666;
  border-radius: 40rpx;
  font-size: 28rpx;
  height: 80rpx;
  line-height: 80rpx;
}
</style>
