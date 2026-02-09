<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="openDialog()">新增订单</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="productId" label="商品ID" width="80" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="priceAtPurchase" label="购买单价" width="100">
          <template #default="{ row }">¥{{ row.priceAtPurchase }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="couponId" label="优惠券ID" width="90" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑订单' : '新增订单'" width="500">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <template v-if="!isEdit">
          <el-form-item label="用户ID" prop="userId">
            <el-input-number v-model="form.userId" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="商品ID" prop="productId">
            <el-input-number v-model="form.productId" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="数量" prop="quantity">
            <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="优惠券ID">
            <el-input-number v-model="form.couponId" :min="0" style="width: 100%" />
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
import { ref, reactive, onMounted } from 'vue'
import { listOrders, addOrder, updateOrder, deleteOrder, deleteBatchOrders } from '../../api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { Order } from '../../types'

const statusMap: Record<string, string> = { PENDING: '待支付', PAID: '已支付', COMPLETED: '已完成', CANCELLED: '已取消' }
const statusType: Record<string, string> = { PENDING: 'warning', PAID: 'success', COMPLETED: '', CANCELLED: 'danger' }

const tableData = ref<Order[]>([])
const selectedIds = ref<number[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const defaultForm = () => ({ userId: 1, productId: 1, quantity: 1, couponId: undefined as number | undefined, status: 'PENDING' as any })
const form = reactive<any>(defaultForm())

const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  productId: [{ required: true, message: '请输入商品ID', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

async function loadData() {
  const res = await listOrders()
  tableData.value = res.data || []
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
    if (form.couponId) params.couponId = form.couponId
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

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
