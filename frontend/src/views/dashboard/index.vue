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
          <template #header>优惠券模板</template>
          <div class="stat-value">{{ stats.coupons }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>分类总数</template>
          <div class="stat-value">{{ stats.categories }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>支付记录</template>
          <div class="stat-value">{{ stats.payments }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>购物车项</template>
          <div class="stat-value">{{ stats.cartItems }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>用户优惠券</template>
          <div class="stat-value">{{ stats.userCoupons }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span style="color: #E6A23C; font-weight: bold;">⚠ 库存预警商品</span>
          </template>
          <el-table v-if="lowStockProducts.length" :data="lowStockProducts" stripe border>
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="stock" label="当前库存" width="100" />
            <el-table-column prop="stockAlertThreshold" label="预警阈值" width="100" />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无库存预警商品" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listUsers } from '../../api/user'
import { listProducts, getLowStockProducts } from '../../api/product'
import { listOrders } from '../../api/order'
import { listCoupons } from '../../api/coupon'
import { listCategories } from '../../api/category'
import { listPayments } from '../../api/payment'
import { listCartItems } from '../../api/cartItem'
import { listUserCoupons } from '../../api/userCoupon'
import type { Product } from '../../types'

const stats = reactive({ users: 0, products: 0, orders: 0, coupons: 0, categories: 0, payments: 0, cartItems: 0, userCoupons: 0 })
const lowStockProducts = ref<Product[]>([])

onMounted(async () => {
  try {
    const [u, p, o, c, cat, pay, cart, uc] = await Promise.all([
      listUsers(), listProducts(), listOrders(), listCoupons(),
      listCategories(), listPayments(), listCartItems(), listUserCoupons()
    ])
    stats.users = u.data?.length || 0
    stats.products = p.data?.length || 0
    stats.orders = o.data?.length || 0
    stats.coupons = c.data?.length || 0
    stats.categories = cat.data?.length || 0
    stats.payments = pay.data?.length || 0
    stats.cartItems = cart.data?.length || 0
    stats.userCoupons = uc.data?.length || 0
  } catch {
    // ignore
  }
  try {
    const res = await getLowStockProducts()
    lowStockProducts.value = res.data || []
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.dashboard { padding: 10px; }
.stat-value { font-size: 36px; font-weight: bold; text-align: center; color: #409eff; }
</style>
