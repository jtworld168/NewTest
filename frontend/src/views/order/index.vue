<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="success" @click="handleExport">导出Excel</el-button>
            <el-button type="primary" @click="openDialog()">新增订单</el-button>
          </div>
        </div>
      </template>
      <div class="search-bar">
        <el-select v-model="filterStoreId" placeholder="按店铺筛选" clearable style="width: 160px" @change="loadData">
          <el-option v-for="s in stores" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="按状态筛选" clearable style="width: 160px" @change="handleFilter">
          <el-option label="待支付" value="PENDING" />
          <el-option label="已支付" value="PAID" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户" width="120">
          <template #default="{ row }">
            {{ userMap[row.userId] || '用户' + row.userId }}
          </template>
        </el-table-column>
        <el-table-column label="店铺" width="120">
          <template #default="{ row }">
            {{ row.storeId ? (storeMap[row.storeId] || row.storeId) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="productId" label="商品" width="160">
          <template #default="{ row }">
            <template v-if="row.productId && productMap[row.productId]">
              {{ productMap[row.productId].name }}
              <div style="font-size: 12px; color: #999;" v-if="productMap[row.productId].barcode">条码: {{ productMap[row.productId].barcode }}</div>
            </template>
            <template v-else>{{ row.productId ? '商品' + row.productId : '-' }}</template>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="priceAtPurchase" label="购买单价" width="100">
          <template #default="{ row }">{{ row.priceAtPurchase ? '¥' + row.priceAtPurchase : '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" align="right" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑订单' : '新增订单'" width="500">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <template v-if="!isEdit">
          <el-form-item label="用户ID" prop="userId">
            <el-input-number v-model="form.userId" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="店铺">
            <el-select v-model="form.storeId" placeholder="选择店铺（可选）" clearable style="width: 100%">
              <el-option v-for="s in stores" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="商品ID" prop="productId">
            <el-input-number v-model="form.productId" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="数量" prop="quantity">
            <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="优惠券ID">
            <el-input-number v-model="form.userCouponId" :min="0" style="width: 100%" />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="待支付" value="PENDING" />
              <el-option label="已支付" value="PAID" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { listOrders, addOrder, updateOrder, deleteOrder, deleteBatchOrders, getOrdersByStatus, listOrdersPage, addMultiItemOrder } from '../../api/order'
import { listStores } from '../../api/store'
import { listUsers } from '../../api/user'
import { listProducts } from '../../api/product'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { Order, Store, User, Product } from '../../types'
import axios from 'axios'
import { BASE_URL } from '../../api/request'

const statusMap: Record<string, string> = { PENDING: '待支付', PAID: '已支付', COMPLETED: '已完成', CANCELLED: '已取消' }
const statusType: Record<string, string> = { PENDING: 'warning', PAID: 'success', COMPLETED: '', CANCELLED: 'danger' }

const tableData = ref<Order[]>([])
const selectedIds = ref<number[]>([])
const stores = ref<Store[]>([])
const users = ref<User[]>([])
const products = ref<Product[]>([])
const filterStatus = ref('')
const filterStoreId = ref<number | undefined>(undefined)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const storeMap = computed(() => {
  const map: Record<number, string> = {}
  stores.value.forEach(s => { if (s.id) map[s.id] = s.name })
  return map
})

const userMap = computed(() => {
  const map: Record<number, string> = {}
  users.value.forEach(u => { if (u.id) map[u.id] = u.username })
  return map
})

const productMap = computed(() => {
  const map: Record<number, { name: string; barcode?: string }> = {}
  products.value.forEach(p => { if (p.id) map[p.id] = { name: p.name, barcode: p.barcode } })
  return map
})

const defaultForm = () => ({ userId: 1, storeId: undefined as number | undefined, productId: 1, quantity: 1, userCouponId: undefined as number | undefined, status: 'PENDING' as any })
const form = reactive<any>(defaultForm())

const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  productId: [{ required: true, message: '请输入商品ID', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

async function loadStores() {
  try {
    const [storeRes, userRes, productRes] = await Promise.all([
      listStores(),
      listUsers(),
      listProducts()
    ])
    stores.value = storeRes.data || []
    users.value = userRes.data || []
    products.value = productRes.data || []
  } catch { /* ignore */ }
}

async function loadData() {
  const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
  if (filterStoreId.value) params.storeId = filterStoreId.value
  const res = await listOrdersPage(params.pageNum, params.pageSize, params.storeId)
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handleFilter() {
  if (filterStatus.value) {
    const res = await getOrdersByStatus(filterStatus.value)
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

async function handleExport() {
  try {
    const token = localStorage.getItem('satoken')
    const response = await axios.get(`${BASE_URL}/api/excel/export/orders`, {
      responseType: 'blob',
      headers: token ? { satoken: token } : {}
    })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '订单数据.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

function handleSelectionChange(rows: Order[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openDialog(row?: Order) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : defaultForm())
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateOrder(form)
    ElMessage.success('更新成功')
  } else {
    const params: any = { userId: form.userId, productId: form.productId, quantity: form.quantity }
    if (form.storeId) params.storeId = form.storeId
    if (form.userCouponId) params.userCouponId = form.userCouponId
    await addOrder(params)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该订单？', '提示', { type: 'warning' })
  await deleteOrder(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个订单？`, '提示', { type: 'warning' })
  await deleteBatchOrders(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

onMounted(() => {
  loadStores()
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; }
</style>
