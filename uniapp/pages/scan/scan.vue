<template>
  <view class="scan-page">
    <text class="scan-msg">正在打开摄像头...</text>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import * as api from '../../utils/api.js'

function doScan() {
  uni.scanCode({
    onlyFromCamera: true,
    success: (res) => {
      const code = res.result
      uni.showLoading({ title: '查找商品...' })
      api.searchProducts(code).then(searchRes => {
        uni.hideLoading()
        const products = searchRes.data || []
        if (products.length > 0) {
          uni.showToast({ title: '找到: ' + products[0].name, icon: 'none', duration: 2000 })
        } else {
          uni.showToast({ title: '未找到该商品', icon: 'none' })
        }
      }).catch(() => {
        uni.hideLoading()
        uni.showToast({ title: '查找失败', icon: 'error' })
      })
    },
    fail: () => {
      uni.switchTab({ url: '/pages/index/index' })
    }
  })
}

onShow(() => { doScan() })
</script>

<style scoped>
.scan-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #F5EDE0;
}

.scan-msg {
  font-size: 32rpx;
  color: var(--color-text-secondary);
}
</style>
