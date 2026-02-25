<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.totalUsers') }}</template>
          <div class="stat-value">{{ stats.users }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.totalProducts') }}</template>
          <div class="stat-value">{{ stats.products }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.totalOrders') }}</template>
          <div class="stat-value">{{ stats.orders }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.couponTemplates') }}</template>
          <div class="stat-value">{{ stats.coupons }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.totalCategories') }}</template>
          <div class="stat-value">{{ stats.categories }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.paymentRecords') }}</template>
          <div class="stat-value">{{ stats.payments }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.cartItems') }}</template>
          <div class="stat-value">{{ stats.cartItems }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.userCoupons') }}</template>
          <div class="stat-value">{{ stats.userCoupons }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Sales Trend Chart -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: bold;">📈 {{ $t('dashboard.salesTrend') }}</span>
          </template>
          <div ref="salesTrendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: bold;">👥 {{ $t('dashboard.userGrowth') }}</span>
          </template>
          <div ref="userGrowthChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Top Products Chart -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: bold;">🏆 {{ $t('dashboard.topProducts') }}</span>
          </template>
          <div ref="topProductsChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Low Stock Warning -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span style="color: #E6A23C; font-weight: bold;">{{ $t('dashboard.lowStockWarning') }}</span>
          </template>
          <el-table v-if="lowStockProducts.length" :data="lowStockProducts" stripe border>
            <el-table-column prop="name" :label="$t('dashboard.productName')" />
            <el-table-column prop="stock" :label="$t('dashboard.currentStock')" width="100" />
            <el-table-column prop="stockAlertThreshold" :label="$t('dashboard.alertThreshold')" width="100" />
            <el-table-column prop="price" :label="$t('dashboard.price')" width="100">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else :description="$t('dashboard.noLowStock')" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts'
import { listUsers } from '../../api/user'
import { listProducts, getLowStockProducts } from '../../api/product'
import { listOrders } from '../../api/order'
import { listCoupons } from '../../api/coupon'
import { listCategories } from '../../api/category'
import { listPayments } from '../../api/payment'
import { listCartItems } from '../../api/cartItem'
import { listUserCoupons } from '../../api/userCoupon'
import { getWeeklySalesTrend, getUserGrowth, getTopProducts } from '../../api/sales'
import type { Product } from '../../types'

const { t } = useI18n()
const stats = reactive({ users: 0, products: 0, orders: 0, coupons: 0, categories: 0, payments: 0, cartItems: 0, userCoupons: 0 })
const lowStockProducts = ref<Product[]>([])

const salesTrendChart = ref<HTMLElement | null>(null)
const userGrowthChart = ref<HTMLElement | null>(null)
const topProductsChart = ref<HTMLElement | null>(null)

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

  // Load chart data
  await nextTick()
  loadSalesTrend()
  loadUserGrowth()
  loadTopProducts()
})

async function loadSalesTrend() {
  if (!salesTrendChart.value) return
  try {
    const res = await getWeeklySalesTrend()
    const data = res.data || []
    const chart = echarts.init(salesTrendChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: [t('dashboard.amount'), t('dashboard.orders')] },
      xAxis: { type: 'category', data: data.map((d: any) => d.label) },
      yAxis: [
        { type: 'value', name: t('dashboard.amount') },
        { type: 'value', name: t('dashboard.orders') }
      ],
      series: [
        { name: t('dashboard.amount'), type: 'line', smooth: true, data: data.map((d: any) => Number(d.totalAmount)), areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409eff' } },
        { name: t('dashboard.orders'), type: 'bar', yAxisIndex: 1, data: data.map((d: any) => d.orderCount), itemStyle: { color: '#67c23a' } }
      ]
    })
    window.addEventListener('resize', () => chart.resize())
  } catch {
    // ignore
  }
}

async function loadUserGrowth() {
  if (!userGrowthChart.value) return
  try {
    const res = await getUserGrowth()
    const data = res.data || []
    const chart = echarts.init(userGrowthChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: [t('dashboard.newUsers'), t('dashboard.cumulativeUsers')] },
      xAxis: { type: 'category', data: data.map((d: any) => d.label) },
      yAxis: { type: 'value' },
      series: [
        { name: t('dashboard.newUsers'), type: 'bar', data: data.map((d: any) => d.newUsers), itemStyle: { color: '#e6a23c' } },
        { name: t('dashboard.cumulativeUsers'), type: 'line', smooth: true, data: data.map((d: any) => d.totalUsers), itemStyle: { color: '#409eff' } }
      ]
    })
    window.addEventListener('resize', () => chart.resize())
  } catch {
    // ignore
  }
}

async function loadTopProducts() {
  if (!topProductsChart.value) return
  try {
    const res = await getTopProducts()
    const data = res.data || []
    const chart = echarts.init(topProductsChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value', name: t('dashboard.salesQty') },
      yAxis: { type: 'category', data: data.map((d: any) => d.productName).reverse(), axisLabel: { width: 120, overflow: 'truncate' } },
      series: [{
        type: 'bar',
        data: data.map((d: any) => d.totalQuantity).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#67c23a' }
          ])
        },
        label: { show: true, position: 'right' }
      }],
      grid: { left: '15%', right: '10%' }
    })
    window.addEventListener('resize', () => chart.resize())
  } catch {
    // ignore
  }
}
</script>

<style scoped>
.dashboard { padding: 10px; }
.stat-value { font-size: 36px; font-weight: bold; text-align: center; color: #409eff; }
</style>
