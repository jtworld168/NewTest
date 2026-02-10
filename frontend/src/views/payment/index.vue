<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>支付管理</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="openDialog()">新增支付</el-button>
          </div>
        </div>
      </template>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索交易号" clearable style="width: 300px" @clear="loadData" @keyup.enter="handleSearch">
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        <el-select v-model="filterStatus" placeholder="按状态筛选" clearable style="width: 160px" @change="handleFilter">
          <el-option label="待支付" value="PENDING" />
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="已退款" value="REFUNDED" />
        </el-select>
      </div>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderId" label="订单ID" width="80" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">{{ methodMap[row.paymentMethod] }}</template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="payStatusType[row.paymentStatus]">{{ payStatusMap[row.paymentStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="transactionNo" label="交易号" width="180" />
        <el-table-column prop="paymentTime" label="支付时间" width="180" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑支付' : '新增支付'" width="550">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="订单ID" prop="orderId">
          <el-input-number v-model="form.orderId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="form.paymentMethod" style="width: 100%">
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="现金" value="CASH" />
            <el-option label="银行卡" value="CARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态" prop="paymentStatus">
          <el-select v-model="form.paymentStatus" style="width: 100%">
            <el-option label="待支付" value="PENDING" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易号">
          <el-input v-model="form.transactionNo" />
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
import { listPayments, addPayment, updatePayment, deletePayment, deleteBatchPayments, searchPayments, getPaymentsByStatus, listPaymentsPage } from '../../api/payment'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { Payment } from '../../types'

const methodMap: Record<string, string> = { WECHAT: '微信支付', ALIPAY: '支付宝', CASH: '现金', CARD: '银行卡' }
const payStatusMap: Record<string, string> = { PENDING: '待支付', SUCCESS: '成功', FAILED: '失败', REFUNDED: '已退款' }
const payStatusType: Record<string, string> = { PENDING: 'warning', SUCCESS: 'success', FAILED: 'danger', REFUNDED: 'info' }

const tableData = ref<Payment[]>([])
const selectedIds = ref<number[]>([])
const searchKeyword = ref('')
const filterStatus = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = (): Payment => ({ orderId: 1, amount: 0, paymentMethod: 'WECHAT' as any, paymentStatus: 'PENDING' as any, transactionNo: '' })
const form = reactive<Payment>(defaultForm())

const rules = {
  orderId: [{ required: true, message: '请输入订单ID', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }],
  paymentStatus: [{ required: true, message: '请选择支付状态', trigger: 'change' }]
}

async function loadData() {
  const res = await listPaymentsPage(pageNum.value, pageSize.value)
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handleSearch() {
  if (searchKeyword.value.trim()) {
    const res = await searchPayments(searchKeyword.value.trim())
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

async function handleFilter() {
  if (filterStatus.value) {
    const res = await getPaymentsByStatus(filterStatus.value)
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

function handleSelectionChange(rows: Payment[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openDialog(row?: Payment) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : defaultForm())
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updatePayment(form)
    ElMessage.success('更新成功')
  } else {
    await addPayment(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该支付记录？', '提示', { type: 'warning' })
  await deletePayment(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条支付记录？`, '提示', { type: 'warning' })
  await deleteBatchPayments(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; }
</style>
