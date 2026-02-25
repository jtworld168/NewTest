<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>销量管理</span>
          <div style="display: flex; gap: 12px; align-items: center;">
            <el-select v-model="filterStoreId" placeholder="全部店铺" clearable style="width: 160px" @change="loadData">
              <el-option v-for="s in storeList" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
            <el-radio-group v-model="viewMode" @change="loadData">
              <el-radio-button value="daily">按天</el-radio-button>
              <el-radio-button value="monthly">按月</el-radio-button>
            </el-radio-group>
            <el-date-picker v-if="viewMode === 'daily'" v-model="selectedMonth" type="month" placeholder="选择月份" @change="loadData" style="width: 160px" />
            <el-date-picker v-else v-model="selectedYear" type="year" placeholder="选择年份" @change="loadData" style="width: 140px" />
          </div>
        </div>
      </template>

      <!-- Summary cards -->
      <div class="summary-row">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">总销售额</div>
          <div class="summary-value">¥{{ totalAmount }}</div>
        </el-card>
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">总订单数</div>
          <div class="summary-value">{{ totalOrders }}</div>
        </el-card>
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">{{ filterStoreId ? '店铺' : '全部' }}统计</div>
          <div class="summary-value">{{ storeName }}</div>
        </el-card>
      </div>

      <!-- Sales data table -->
      <el-table :data="tableData" stripe border style="margin-top: 20px;">
        <el-table-column :label="viewMode === 'daily' ? '日期' : '月份'" align="center">
          <template #default="{ row }">{{ row.label }}</template>
        </el-table-column>
        <el-table-column label="销售额 (¥)" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-highlight': row.totalAmount > 0 }">¥{{ Number(row.totalAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单数" align="center">
          <template #default="{ row }">{{ row.orderCount }}</template>
        </el-table-column>
        <el-table-column label="平均订单金额 (¥)" align="center">
          <template #default="{ row }">
            {{ row.orderCount > 0 ? (row.totalAmount / row.orderCount).toFixed(2) : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getDailySales, getMonthlySales, getTotalSales } from '../../api/sales'
import { listStores } from '../../api/store'

const viewMode = ref<'daily' | 'monthly'>('daily')
const filterStoreId = ref<number | undefined>(undefined)
const storeList = ref<any[]>([])
const tableData = ref<any[]>([])
const selectedMonth = ref(new Date())
const selectedYear = ref(new Date())
const totalAmount = ref('0.00')
const totalOrders = ref(0)

const storeName = computed(() => {
  if (!filterStoreId.value) return '全部店铺'
  const store = storeList.value.find((s: any) => s.id === filterStoreId.value)
  return store ? store.name : '未知'
})

async function loadData() {
  try {
    const [storeRes, totalRes] = await Promise.all([
      listStores(),
      getTotalSales(filterStoreId.value)
    ])
    storeList.value = storeRes.data || []
    totalAmount.value = Number(totalRes.data?.totalAmount || 0).toFixed(2)
    totalOrders.value = totalRes.data?.orderCount || 0

    if (viewMode.value === 'daily') {
      const d = selectedMonth.value || new Date()
      const year = d.getFullYear()
      const month = d.getMonth() + 1
      const res = await getDailySales(year, month, filterStoreId.value)
      tableData.value = (res.data || []).map((item: any) => ({
        label: item.dayLabel,
        totalAmount: Number(item.totalAmount || 0),
        orderCount: item.orderCount || 0
      }))
    } else {
      const d = selectedYear.value || new Date()
      const year = d.getFullYear()
      const res = await getMonthlySales(year, filterStoreId.value)
      tableData.value = (res.data || []).map((item: any) => ({
        label: item.monthLabel,
        totalAmount: Number(item.totalAmount || 0),
        orderCount: item.orderCount || 0
      }))
    }
  } catch (e) {
    console.error('Failed to load sales data:', e)
  }
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.summary-row {
  display: flex;
  gap: 20px;
}
.summary-card {
  flex: 1;
  text-align: center;
}
.summary-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}
.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}
.text-highlight {
  color: #e74c3c;
  font-weight: 600;
}
</style>
