<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>店铺管理</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="success" @click="handleExport">导出Excel</el-button>
            <el-button type="primary" @click="openDialog()">新增店铺</el-button>
          </div>
        </div>
      </template>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索店铺名称/地址" clearable style="width: 300px" @clear="loadData" @keyup.enter="handleSearch">
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="店铺名称" />
        <el-table-column prop="address" label="店铺地址" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column label="状态" prop="status" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '营业' : '关闭' }}</el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑店铺' : '新增店铺'" width="600">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="店铺名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="店铺地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="营业" :value="1" />
            <el-option label="关闭" :value="0" />
          </el-select>
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
import { listStoresPage, addStore, updateStore, deleteStore, deleteBatchStores, searchStores } from '../../api/store'
import { BASE_URL } from '../../api/request'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const tableData = ref<any[]>([])
const selectedIds = ref<number[]>([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = () => ({ name: '', address: '', phone: '', status: 1 })
const form = reactive<any>(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入店铺地址', trigger: 'blur' }]
}

async function loadData() {
  const res = await listStoresPage(pageNum.value, pageSize.value)
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handleSearch() {
  if (searchKeyword.value.trim()) {
    const res = await searchStores(searchKeyword.value.trim())
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openDialog(row?: any) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : defaultForm())
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateStore(form)
    ElMessage.success('更新成功')
  } else {
    await addStore(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该店铺？', '提示', { type: 'warning' })
  await deleteStore(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个店铺？`, '提示', { type: 'warning' })
  await deleteBatchStores(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

function handleExport() {
  const token = localStorage.getItem('satoken')
  axios.get(BASE_URL + '/api/excel/export/stores', {
    responseType: 'blob',
    headers: token ? { satoken: token } : {}
  }).then(res => {
    const blob = new Blob([res.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '店铺数据.xlsx'
    link.click()
    URL.revokeObjectURL(link.href)
  })
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; }
</style>
