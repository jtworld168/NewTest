<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>购物车管理</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="openDialog()">新增</el-button>
          </div>
        </div>
      </template>
      <div class="search-bar">
        <el-input-number v-model="filterUserId" placeholder="按用户ID筛选" :min="1" controls-position="right" style="width: 200px">
        </el-input-number>
        <el-button @click="handleFilterByUser">筛选</el-button>
        <el-button @click="filterUserId = undefined; loadData()">重置</el-button>
      </div>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑购物车项' : '新增购物车项'" width="500">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="form.userId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品ID" prop="productId">
          <el-input-number v-model="form.productId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
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
import { listCartItems, getCartItemsByUserId, addCartItem, updateCartItem, deleteCartItem, deleteBatchCartItems } from '../../api/cartItem'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { CartItem } from '../../types'

const tableData = ref<CartItem[]>([])
const selectedIds = ref<number[]>([])
const filterUserId = ref<number | undefined>(undefined)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const defaultForm = (): CartItem => ({ userId: 1, productId: 1, quantity: 1 })
const form = reactive<CartItem>(defaultForm())

const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  productId: [{ required: true, message: '请输入商品ID', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

async function loadData() {
  const res = await listCartItems()
  tableData.value = res.data || []
}

async function handleFilterByUser() {
  if (filterUserId.value) {
    const res = await getCartItemsByUserId(filterUserId.value)
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

function handleSelectionChange(rows: CartItem[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openDialog(row?: CartItem) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : defaultForm())
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateCartItem(form)
    ElMessage.success('更新成功')
  } else {
    await addCartItem(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该购物车项？', '提示', { type: 'warning' })
  await deleteCartItem(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个购物车项？`, '提示', { type: 'warning' })
  await deleteBatchCartItems(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; }
</style>
