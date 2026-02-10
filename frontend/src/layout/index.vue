<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <h2>无人超市</h2>
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
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Menu /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/order-items">
          <el-icon><List /></el-icon>
          <span>订单明细</span>
        </el-menu-item>
        <el-menu-item index="/coupons">
          <el-icon><Ticket /></el-icon>
          <span>优惠券模板</span>
        </el-menu-item>
        <el-menu-item index="/user-coupons">
          <el-icon><Present /></el-icon>
          <span>用户优惠券</span>
        </el-menu-item>
        <el-menu-item index="/payments">
          <el-icon><CreditCard /></el-icon>
          <span>支付管理</span>
        </el-menu-item>
        <el-menu-item index="/cart-items">
          <el-icon><ShoppingCart /></el-icon>
          <span>购物车管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="header-title">{{ currentTitle }}</span>
        <div class="header-right">
          <template v-if="userStore.currentUser">
            <el-avatar v-if="userStore.currentUser.avatar" :src="BASE_URL + userStore.currentUser.avatar" :size="32" />
            <el-avatar v-else :size="32">{{ userStore.currentUser.username?.charAt(0).toUpperCase() }}</el-avatar>
            <span class="username">{{ userStore.currentUser.username }}</span>
          </template>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { logout } from '../api/auth'
import { BASE_URL } from '../api/request'
import { ElMessage } from 'element-plus'
import {
  HomeFilled, User, Menu, Goods, Document, List, Ticket, CreditCard, Present, ShoppingCart
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta.title as string) || '首页')

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
