<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <h2>{{ $t('common.systemName') }}</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>{{ $t('menu.home') }}</span>
        </el-menu-item>
        <el-sub-menu index="statistics">
          <template #title>
            <el-icon><DataLine /></el-icon>
            <span>{{ $t('menu.statistics') }}</span>
          </template>
          <el-menu-item index="/statistics/bar">
            <el-icon><Histogram /></el-icon>
            <span>{{ $t('menu.barChart') }}</span>
          </el-menu-item>
          <el-menu-item index="/statistics/pie">
            <el-icon><PieChart /></el-icon>
            <span>{{ $t('menu.pieChart') }}</span>
          </el-menu-item>
          <el-menu-item index="/statistics/line">
            <el-icon><TrendCharts /></el-icon>
            <span>{{ $t('menu.lineChart') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>{{ $t('menu.userMgmt') }}</span>
        </el-menu-item>
        <el-sub-menu index="product-mgmt">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>{{ $t('menu.productMgmt') }}</span>
          </template>
          <el-menu-item index="/categories">
            <el-icon><Menu /></el-icon>
            <span>{{ $t('menu.categoryMgmt') }}</span>
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><Goods /></el-icon>
            <span>{{ $t('menu.productList') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="order-mgmt">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>{{ $t('menu.orderMgmt') }}</span>
          </template>
          <el-menu-item index="/orders">
            <el-icon><Document /></el-icon>
            <span>{{ $t('menu.orderList') }}</span>
          </el-menu-item>
          <el-menu-item index="/order-items">
            <el-icon><List /></el-icon>
            <span>{{ $t('menu.orderDetail') }}</span>
          </el-menu-item>
          <el-menu-item index="/payments">
            <el-icon><CreditCard /></el-icon>
            <span>{{ $t('menu.paymentMgmt') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="coupon-mgmt">
          <template #title>
            <el-icon><Ticket /></el-icon>
            <span>{{ $t('menu.couponMgmt') }}</span>
          </template>
          <el-menu-item index="/coupons">
            <el-icon><Ticket /></el-icon>
            <span>{{ $t('menu.couponTemplate') }}</span>
          </el-menu-item>
          <el-menu-item index="/user-coupons">
            <el-icon><Present /></el-icon>
            <span>{{ $t('menu.userCoupon') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/cart-items">
          <el-icon><ShoppingCart /></el-icon>
          <span>{{ $t('menu.cartMgmt') }}</span>
        </el-menu-item>
        <el-sub-menu index="store-mgmt">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>{{ $t('menu.storeMgmt') }}</span>
          </template>
          <el-menu-item index="/stores">
            <el-icon><OfficeBuilding /></el-icon>
            <span>{{ $t('menu.storeList') }}</span>
          </el-menu-item>
          <el-menu-item index="/store-products">
            <el-icon><Goods /></el-icon>
            <span>{{ $t('menu.storeProduct') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/messages">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ $t('menu.messageMgmt') }}</span>
        </el-menu-item>
        <el-menu-item index="/sales">
          <el-icon><TrendCharts /></el-icon>
          <span>{{ $t('menu.salesMgmt') }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="header-title">{{ currentTitle }}</span>
        <div class="header-right">
          <el-select v-model="currentLocale" size="small" style="width: 100px" @change="switchLocale">
            <el-option label="中文" value="zh-CN" />
            <el-option label="English" value="en" />
          </el-select>
          <template v-if="userStore.currentUser">
            <el-avatar v-if="userStore.currentUser.avatar" :src="'/api' + userStore.currentUser.avatar" :size="32" />
            <el-avatar v-else :size="32">{{ userStore.currentUser.username?.charAt(0).toUpperCase() }}</el-avatar>
            <span class="username">{{ userStore.currentUser.username }}</span>
          </template>
          <el-button type="danger" size="small" @click="handleLogout">{{ $t('common.logout') }}</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../stores/user'
import { logout } from '../api/auth'
import { ElMessage } from 'element-plus'
import {
  HomeFilled, User, Menu, Goods, Document, List, Ticket, CreditCard, Present, ShoppingCart,
  DataLine, PieChart, TrendCharts, Histogram, OfficeBuilding, ChatDotRound
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { locale } = useI18n()

const currentLocale = ref(localStorage.getItem('locale') || 'zh-CN')

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta.title as string) || '')

function switchLocale(val: string) {
  locale.value = val
  localStorage.setItem('locale', val)
  location.reload()
}

async function handleLogout() {
  try {
    await logout()
    userStore.clearUser()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch {
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #304156;
  overflow-y: auto;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.logo h2 {
  margin: 0;
  font-size: 18px;
}
.header {
  background: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.header-title {
  font-size: 18px;
  font-weight: bold;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.username {
  color: #606266;
}
.main {
  background-color: #f0f2f5;
}
</style>
