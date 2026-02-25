<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单明细</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="openDialog()">新增明细</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="orderId" label="订单ID" width="80" align="center" />
        <el-table-column prop="productId" label="商品" width="160">
          <template #default="{ row }">
            <template v-if="row.productName">
              {{ row.productName }}
              <div style="font-size: 12px; color: #999;" v-if="row.productBarcode">条码: {{ row.productBarcode }}</div>
            </template>
            <template v-else>商品{{ row.productId }}</template>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column prop="priceAtPurchase" label="购买单价" width="120" align="center">
          <template #default="{ row }">¥{{ row.priceAtPurchase }}</template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="120" align="center">
          <template #default="{ row }">¥{{ row.subtotal }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑明细' : '新增明细'" width="500">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="订单ID" prop="orderId">
          <el-input-number v-model="form.orderId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品ID" prop="productId">
          <el-input-number v-model="form.productId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="购买单价" prop="priceAtPurchase">
          <el-input-number v-model="form.priceAtPurchase" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { addOrderItem, updateOrderItem, deleteOrderItem, deleteBatchOrderItems, listOrderItemsPage } from '../../api/orderItem'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { OrderItem } from '../../types'

const tableData = ref<any[]>([])
const selectedIds = ref<number[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = (): OrderItem => ({ orderId: 1, productId: 1, quantity: 1, priceAtPurchase: 0 })
const form = reactive<OrderItem>(defaultForm())

const rules = {
  orderId: [{ required: true, message: '请输入订单ID', trigger: 'blur' }],
  productId: [{ required: true, message: '请输入商品ID', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  priceAtPurchase: [{ required: true, message: '请输入购买单价', trigger: 'blur' }]
}

async function loadData() {
  const res = await listOrderItemsPage(pageNum.value, pageSize.value)
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

function handleSelectionChange(rows: OrderItem[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openDialog(row?: OrderItem) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : defaultForm())
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateOrderItem(form)
    ElMessage.success('更新成功')
  } else {
    await addOrderItem(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该明细？', '提示', { type: 'warning' })
  await deleteOrderItem(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条明细？`, '提示', { type: 'warning' })
  await deleteBatchOrderItems(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
