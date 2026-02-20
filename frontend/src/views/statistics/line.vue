<template>
  <div class="chart-page">
    <el-card shadow="hover">
      <template #header>
        <div class="chart-header">
          <span class="chart-title">📈 销量趋势 — 折线图</span>
          <div class="chart-controls">
            <el-select v-model="selectedStoreId" placeholder="选择店铺" clearable style="width: 180px" @change="loadData">
              <el-option label="全部店铺（总览）" :value="0" />
              <el-option v-for="s in stores" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
            <el-select v-model="selectedYear" style="width: 120px; margin-left: 10px" @change="loadData">
              <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
            </el-select>
          </div>
        </div>
      </template>
      <div ref="lineChartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getMonthlySales } from '../../api/sales'
import { listStores } from '../../api/store'

const lineChartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
const selectedStoreId = ref<number>(0)
const selectedYear = ref<number>(new Date().getFullYear())
const stores = ref<any[]>([])

const yearOptions: number[] = []
const currentYear = new Date().getFullYear()
for (let y = currentYear - 3; y <= currentYear; y++) yearOptions.push(y)

const loadData = async () => {
  try {
    const storeId = selectedStoreId.value > 0 ? selectedStoreId.value : undefined
    const res = await getMonthlySales(selectedYear.value, storeId)
    const data = res.data || []
    if (lineChartRef.value) {
      if (!chart) chart = echarts.init(lineChartRef.value)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['销售额', '订单数'] },
        xAxis: { type: 'category', data: data.map((d: any) => d.monthLabel), boundaryGap: false },
        yAxis: [
          { type: 'value', name: '销售额(元)', position: 'left' },
          { type: 'value', name: '订单数', position: 'right', minInterval: 1 }
        ],
        series: [
          {
            name: '销售额',
            type: 'line',
            data: data.map((d: any) => d.totalAmount),
            smooth: true,
            areaStyle: { opacity: 0.2 },
            itemStyle: { color: '#E6A23C' },
            lineStyle: { width: 3 }
          },
          {
            name: '订单数',
            type: 'line',
            yAxisIndex: 1,
            data: data.map((d: any) => d.orderCount),
            smooth: true,
            itemStyle: { color: '#67C23A' },
            lineStyle: { width: 2, type: 'dashed' }
          }
        ],
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
.chart-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; }
.chart-controls { display: flex; align-items: center; }
</style>
