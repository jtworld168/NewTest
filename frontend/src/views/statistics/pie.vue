<template>
  <div class="chart-page">
    <el-card shadow="hover">
      <template #header><span class="chart-title">🥧 数据统计 — 饼图</span></template>
      <div ref="pieChartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { listUsers } from '../../api/user'
import { listProducts } from '../../api/product'
import { listOrders } from '../../api/order'
import { listCoupons } from '../../api/coupon'
import { listCategories } from '../../api/category'
import { listPayments } from '../../api/payment'
import { listCartItems } from '../../api/cartItem'
import { listUserCoupons } from '../../api/userCoupon'

const pieChartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

onMounted(async () => {
  try {
    const [u, p, o, c, cat, pay, cart, uc] = await Promise.all([
      listUsers(), listProducts(), listOrders(), listCoupons(),
      listCategories(), listPayments(), listCartItems(), listUserCoupons()
    ])
    const data = [
      { name: '用户', value: u.data?.length || 0 },
      { name: '商品', value: p.data?.length || 0 },
      { name: '订单', value: o.data?.length || 0 },
      { name: '优惠券', value: c.data?.length || 0 },
      { name: '分类', value: cat.data?.length || 0 },
      { name: '支付', value: pay.data?.length || 0 },
      { name: '购物车', value: cart.data?.length || 0 },
      { name: '用户券', value: uc.data?.length || 0 }
    ]
    if (pieChartRef.value) {
      chart = echarts.init(pieChartRef.value)
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: true,
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
          label: { show: true, formatter: '{b}: {c}' },
          data
        }]
      })
    }
  } catch {
    // Data loading failed, chart will remain empty
  }
})

onUnmounted(() => { chart?.dispose() })
</script>

<style scoped>
.chart-page { padding: 10px; }
.chart-container { width: 100%; height: 450px; }
.chart-title { font-weight: bold; font-size: 16px; }
</style>
