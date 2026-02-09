<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>用户总数</template>
          <div class="stat-value">{{ stats.users }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>商品总数</template>
          <div class="stat-value">{{ stats.products }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>订单总数</template>
          <div class="stat-value">{{ stats.orders }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>优惠券总数</template>
          <div class="stat-value">{{ stats.coupons }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { listUsers } from '../../api/user'
import { listProducts } from '../../api/product'
import { listOrders } from '../../api/order'
import { listCoupons } from '../../api/coupon'

const stats = reactive({ users: 0, products: 0, orders: 0, coupons: 0 })

onMounted(async () => {
  try {
    const [u, p, o, c] = await Promise.all([listUsers(), listProducts(), listOrders(), listCoupons()])
    stats.users = u.data?.length || 0
    stats.products = p.data?.length || 0
    stats.orders = o.data?.length || 0
    stats.coupons = c.data?.length || 0
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.dashboard { padding: 10px; }
.stat-value { font-size: 36px; font-weight: bold; text-align: center; color: #409eff; }
</style>
