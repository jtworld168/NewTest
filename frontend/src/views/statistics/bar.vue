<template>
  <div class="chart-page">
    <el-card shadow="hover">
      <template #header>
        <div class="chart-header">
          <span class="chart-title">📊 分类商品统计 — 柱状图</span>
          <el-select v-model="selectedStoreId" placeholder="选择店铺" clearable style="width: 200px" @change="loadData">
            <el-option label="全部店铺（总览）" :value="0" />
            <el-option v-for="s in stores" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </div>
      </template>
      <div ref="barChartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getCategoryProductCount } from '../../api/sales'
import { listStores } from '../../api/store'

const barChartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
const selectedStoreId = ref<number>(0)
const stores = ref<any[]>([])

const loadData = async () => {
  try {
    const storeId = selectedStoreId.value > 0 ? selectedStoreId.value : undefined
    const res = await getCategoryProductCount(storeId)
    const data = res.data || []
    if (barChartRef.value) {
      if (!chart) chart = echarts.init(barChartRef.value)
      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        xAxis: { type: 'category', data: data.map((d: any) => d.categoryName), axisLabel: { interval: 0, rotate: 15 } },
        yAxis: { type: 'value', name: '商品数量', minInterval: 1 },
        series: [{
          type: 'bar',
          name: '商品数量',
          data: data.map((d: any) => d.productCount),
          itemStyle: { color: '#409eff', borderRadius: [6, 6, 0, 0] },
          barWidth: '40%'
        }],
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }
      }, true)
    }
  } catch { /* chart remains empty */ }
}

onMounted(async () => {
  try {
    const res = await listStores()
    stores.value = res.data || []
  } catch { /* no stores */ }
  loadData()
})

onUnmounted(() => { chart?.dispose() })
</script>

<style scoped>
.chart-page { padding: 10px; }
.chart-container { width: 100%; height: 450px; }
.chart-title { font-weight: bold; font-size: 16px; }
.chart-header { display: flex; justify-content: space-between; align-items: center; }
</style>
