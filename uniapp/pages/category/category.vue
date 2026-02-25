<template>
  <view class="category-page">
    <!-- Search Bar -->
    <view class="search-bar">
      <input class="search-input" placeholder="搜索商品" v-model="searchKeyword" @confirm="doSearch" />
      <view class="search-btn" @click="doSearch">搜索</view>
    </view>

    <view class="content">
      <!-- Category Sidebar -->
      <scroll-view class="category-sidebar" scroll-y>
        <view
          class="sidebar-item"
          :class="{ active: selectedCategoryId === item.id }"
          v-for="item in categories"
          :key="item.id"
          @click="selectCategory(item.id)"
        >
          <text>{{ item.name }}</text>
        </view>
      </scroll-view>

      <!-- Product List -->
      <scroll-view class="product-list" scroll-y>
        <view v-if="!loading && products.length > 0">
          <view class="product-item" v-for="item in products" :key="item.id" @click="goToProductDetail(item.id)">
            <image class="item-image" :src="item._imageUrl || '/static/product-placeholder.png'" mode="aspectFill" />
            <view class="item-info">
              <text class="item-name">{{ item.name }}</text>
              <text class="item-desc" v-if="item.description">{{ item.description }}</text>
              <view class="item-bottom">
                <view class="price-area">
                  <text class="price">¥{{ item._displayPrice }}</text>
                  <text class="original-price" v-if="item._isEmployee">¥{{ item.price }}</text>
                  <text class="employee-tag" v-if="item._isEmployee">内购价</text>
                </view>
                <text class="item-stock">库存: {{ item.stock }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="loading" v-if="loading">
          <text>加载中...</text>
        </view>
        <view class="empty-state" v-if="!loading && products.length === 0">
          <text class="empty-icon">📦</text>
          <text class="empty-text">该分类暂无商品</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import * as api from '../../utils/api.js'

export default {
  data() {
    return {
      categories: [],
      products: [],
      selectedCategoryId: null,
      loading: true,
      searchKeyword: ''
    }
  },
  onLoad(options) {
    if (options && options.categoryId) {
      this.selectedCategoryId = parseInt(options.categoryId)
    }
    this.loadCategories()
  },
  onShow() {
    if (this.selectedCategoryId && this.categories.length > 0) {
      this.loadProductsByCategory(this.selectedCategoryId)
    }
  },
  methods: {
    enrichProducts(list) {
      const app = getApp()
      const isEmployee = app.globalData.userInfo && Boolean(app.globalData.userInfo.isHotelEmployee)
      return list.map(p => {
        p._imageUrl = p.image ? api.getFileUrl(p.image) : ''
        if (isEmployee && p.employeeDiscountRate) {
          p._isEmployee = true
          p._displayPrice = (p.price * p.employeeDiscountRate).toFixed(2)
        } else {
          p._isEmployee = false
          p._displayPrice = p.price.toFixed(2)
        }
        return p
      })
    },
    async loadCategories() {
      try {
        const res = await api.getCategoryList()
        const cats = res.data || []
        this.categories = cats
        if (cats.length > 0) {
          const selId = this.selectedCategoryId || cats[0].id
          this.selectedCategoryId = selId
          this.loadProductsByCategory(selId)
        } else {
          this.loading = false
        }
      } catch (e) {
        console.error('Failed to load categories:', e)
        this.loading = false
      }
    },
    async loadProductsByCategory(categoryId) {
      this.loading = true
      try {
        const res = await api.getProductsByCategoryId(categoryId)
        this.products = this.enrichProducts(res.data || [])
        this.loading = false
      } catch (e) {
        console.error('Failed to load products:', e)
        this.loading = false
      }
    },
    selectCategory(id) {
      this.selectedCategoryId = id
      this.loadProductsByCategory(id)
    },
    async doSearch() {
      const keyword = this.searchKeyword.trim()
      if (!keyword) {
        if (this.selectedCategoryId) {
          this.loadProductsByCategory(this.selectedCategoryId)
        }
        return
      }
      this.loading = true
      try {
        const res = await api.searchProducts(keyword)
        this.products = this.enrichProducts(res.data || [])
        this.selectedCategoryId = null
        this.loading = false
      } catch (e) {
        this.loading = false
      }
    },
    goToProductDetail(id) {
      const product = this.products.find(p => p.id === id)
      if (!product) return
      const app = getApp()
      if (!app.globalData.userInfo) {
        uni.navigateTo({ url: '/pages/login/login' })
        return
      }
      uni.showActionSheet({
        itemList: ['加入购物车', '立即购买'],
        success: (res) => {
          if (res.tapIndex === 0) this.addToCart(product)
          else if (res.tapIndex === 1) this.buyNow(product)
        }
      })
    },
    async addToCart(product) {
      const app = getApp()
      try {
        await api.addCartItem({ userId: app.globalData.userInfo.id, productId: product.id, quantity: 1 })
        uni.showToast({ title: '已加入购物车', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '添加失败', icon: 'error' })
      }
    },
    async buyNow(product) {
      const app = getApp()
      try {
        const res = await api.addOrder(app.globalData.userInfo.id, product.id, 1)
        if (res.data && res.data.id) {
          uni.navigateTo({ url: '/pages/payment/payment?orderId=' + res.data.id + '&amount=' + res.data.totalAmount })
        } else {
          uni.showToast({ title: '下单成功', icon: 'success' })
          setTimeout(() => uni.switchTab({ url: '/pages/orders/orders' }), 1500)
        }
      } catch (e) {
        uni.showToast({ title: '下单失败', icon: 'error' })
      }
    }
  }
}
</script>

<style scoped>
.category-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  background-color: var(--color-primary);
}

.search-bar .search-input {
  flex: 1;
  height: 64rpx;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 32rpx;
  padding: 0 24rpx;
  font-size: 26rpx;
}

.search-btn {
  color: #fff;
  font-size: 26rpx;
  margin-left: 16rpx;
  white-space: nowrap;
}

.content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* Sidebar */
.category-sidebar {
  width: 200rpx;
  background-color: #F5EDE0;
  height: 100%;
}

.sidebar-item {
  padding: 28rpx 20rpx;
  font-size: 26rpx;
  color: var(--color-text-secondary);
  text-align: center;
  border-left: 6rpx solid transparent;
}

.sidebar-item.active {
  background-color: #fff;
  color: var(--color-primary);
  font-weight: bold;
  border-left-color: var(--color-primary);
}

/* Product List */
.product-list {
  flex: 1;
  height: 100%;
  padding: 16rpx;
}

.product-item {
  display: flex;
  background-color: #fff;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(60, 50, 38, 0.06);
  animation: slideUp 0.35s ease-out backwards;
}

.item-image {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
  background-color: #F5EDE0;
}

.item-info {
  flex: 1;
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-name {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.item-desc {
  font-size: 22rpx;
  color: var(--color-text-secondary);
  margin-top: 8rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8rpx;
}

.item-bottom .price { font-size: 30rpx; }

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

.item-stock {
  font-size: 22rpx;
  color: var(--color-text-secondary);
}
</style>
