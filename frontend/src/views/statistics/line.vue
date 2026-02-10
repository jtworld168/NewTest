<template>
  <div class="chart-page">
    <el-card shadow="hover">
      <template #header><span class="chart-title">📈 数据统计 — 折线图</span></template>
      <div ref="lineChartRef" class="chart-container"></div>
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

const lineChartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

onMounted(async () => {
  const [u, p, o, c, cat, pay, cart, uc] = await Promise.all([
    listUsers(), listProducts(), listOrders(), listCoupons(),
    listCategories(), listPayments(), listCartItems(), listUserCoupons()
  ])
  const names = ['用户', '商品', '订单', '优惠券', '分类', '支付', '购物车', '用户券']
  const values = [
    u.data?.length || 0, p.data?.length || 0, o.data?.length || 0, c.data?.length || 0,
    cat.data?.length || 0, pay.data?.length || 0, cart.data?.length || 0, uc.data?.length || 0
  ]
  if (lineChartRef.value) {
    chart = echarts.init(lineChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: names },
      yAxis: { type: 'value' },
      series: [{
        type: 'line',
        data: values,
        smooth: true,
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#67C23A' }
      }]
    })
  }
})

onUnmounted(() => { chart?.dispose() })
</script>

<style scoped>
.chart-page { padding: 10px; }
.chart-container { width: 100%; height: 450px; }
.chart-title { font-weight: bold; font-size: 16px; }
</style>
