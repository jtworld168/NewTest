<template>
  <div class="chart-page">
    <el-card shadow="hover">
      <template #header>
        <div class="chart-header">
          <span class="chart-title">🥧 分类商品统计 — 饼图</span>
          <el-select v-model="selectedStoreId" placeholder="选择店铺" clearable style="width: 200px" @change="loadData">
            <el-option label="全部店铺（总览）" :value="0" />
            <el-option v-for="s in stores" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </div>
      </template>
      <div ref="pieChartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getCategoryProductCount } from '../../api/sales'
import { listStores } from '../../api/store'

const pieChartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
const selectedStoreId = ref<number>(0)
const stores = ref<any[]>([])

const loadData = async () => {
  try {
    const storeId = selectedStoreId.value > 0 ? selectedStoreId.value : undefined
    const res = await getCategoryProductCount(storeId)
    const data = (res.data || []).map((d: any) => ({ name: d.categoryName, value: d.productCount }))
    if (pieChartRef.value) {
      if (!chart) chart = echarts.init(pieChartRef.value)
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
