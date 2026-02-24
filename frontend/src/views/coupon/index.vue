<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>优惠券模板管理</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="openDialog()">新增优惠券</el-button>
          </div>
        </div>
      </template>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索优惠券名称" clearable style="width: 300px" @clear="loadData" @keyup.enter="handleSearch">
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="discount" label="折扣金额" width="100" align="center">
          <template #default="{ row }">¥{{ row.discount }}</template>
        </el-table-column>
        <el-table-column prop="minAmount" label="最低消费" width="100" align="center">
          <template #default="{ row }">¥{{ row.minAmount }}</template>
        </el-table-column>
        <el-table-column prop="totalCount" label="总数量" width="90" align="center" />
        <el-table-column prop="remainingCount" label="剩余数量" width="90" align="center" />
        <el-table-column prop="startTime" label="开始时间" width="180" align="center" />
        <el-table-column prop="endTime" label="结束时间" width="180" align="center" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑优惠券' : '新增优惠券'" width="550">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="折扣金额" prop="discount">
          <el-input-number v-model="form.discount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最低消费" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总数量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="剩余数量" prop="remainingCount">
          <el-input-number v-model="form.remainingCount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
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
import { listCoupons, addCoupon, updateCoupon, deleteCoupon, deleteBatchCoupons, searchCoupons, listCouponsPage } from '../../api/coupon'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { Coupon } from '../../types'

const tableData = ref<Coupon[]>([])
const selectedIds = ref<number[]>([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = (): Coupon => ({ name: '', discount: 0, minAmount: 0, totalCount: 0, remainingCount: 0, startTime: '', endTime: '' })
const form = reactive<Coupon>(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  discount: [{ required: true, message: '请输入折扣金额', trigger: 'blur' }],
  minAmount: [{ required: true, message: '请输入最低消费', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入总数量', trigger: 'blur' }],
  remainingCount: [{ required: true, message: '请输入剩余数量', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

async function loadData() {
  const res = await listCouponsPage(pageNum.value, pageSize.value)
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handleSearch() {
  if (searchKeyword.value.trim()) {
    const res = await searchCoupons(searchKeyword.value.trim())
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

function handleSelectionChange(rows: Coupon[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openDialog(row?: Coupon) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : defaultForm())
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateCoupon(form)
    ElMessage.success('更新成功')
  } else {
    await addCoupon(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该优惠券？', '提示', { type: 'warning' })
  await deleteCoupon(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个优惠券？`, '提示', { type: 'warning' })
  await deleteBatchCoupons(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; }
</style>
