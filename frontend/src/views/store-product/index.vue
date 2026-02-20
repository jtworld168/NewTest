<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>店铺商品管理</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="openDialog()">新增店铺商品</el-button>
          </div>
        </div>
      </template>
      <div class="search-bar">
        <el-select v-model="filterStoreId" placeholder="按店铺筛选" clearable style="width: 200px" @change="loadData">
          <el-option v-for="s in storeList" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </div>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="storeId" label="店铺ID" width="80" />
        <el-table-column label="店铺名称" width="140">
          <template #default="{ row }">
            {{ storeMap[row.storeId] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="productId" label="商品ID" width="80" />
        <el-table-column label="商品名称" width="140">
          <template #default="{ row }">
            {{ productMap[row.productId] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="storePrice" label="店铺售价" width="100">
          <template #default="{ row }">¥{{ row.storePrice }}</template>
        </el-table-column>
        <el-table-column prop="storeStock" label="店铺库存" width="100" />
        <el-table-column label="状态" prop="status" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑店铺商品' : '新增店铺商品'" width="600">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="店铺" prop="storeId">
          <el-select v-model="form.storeId" placeholder="请选择店铺" style="width: 100%">
            <el-option v-for="s in storeList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" style="width: 100%">
            <el-option v-for="p in productList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="店铺售价" prop="storePrice">
          <el-input-number v-model="form.storePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="店铺库存" prop="storeStock">
          <el-input-number v-model="form.storeStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { listStoreProductsPage, addStoreProduct, updateStoreProduct, deleteStoreProduct, deleteBatchStoreProducts } from '../../api/storeProduct'
import { listStores } from '../../api/store'
import { listProducts } from '../../api/product'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const tableData = ref<any[]>([])
const storeList = ref<any[]>([])
const productList = ref<any[]>([])
const selectedIds = ref<number[]>([])
const filterStoreId = ref<number | undefined>(undefined)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const storeMap = computed(() => {
  const map: Record<number, string> = {}
  storeList.value.forEach((s: any) => { if (s.id) map[s.id] = s.name })
  return map
})

const productMap = computed(() => {
  const map: Record<number, string> = {}
  productList.value.forEach((p: any) => { if (p.id) map[p.id] = p.name })
  return map
})

const defaultForm = () => ({ storeId: undefined, productId: undefined, storePrice: 0, storeStock: 0, status: 1 })
const form = reactive<any>(defaultForm())

const rules = {
  storeId: [{ required: true, message: '请选择店铺', trigger: 'change' }],
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  storePrice: [{ required: true, message: '请输入店铺售价', trigger: 'blur' }],
  storeStock: [{ required: true, message: '请输入店铺库存', trigger: 'blur' }]
}

async function loadData() {
  const [res, storeRes, productRes] = await Promise.all([
    listStoreProductsPage(pageNum.value, pageSize.value, filterStoreId.value),
    listStores(),
    listProducts()
  ])
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
  storeList.value = storeRes.data || []
  productList.value = productRes.data || []
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
    await updateStoreProduct(form)
    ElMessage.success('更新成功')
  } else {
    await addStoreProduct(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该店铺商品？', '提示', { type: 'warning' })
  await deleteStoreProduct(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个店铺商品？`, '提示', { type: 'warning' })
  await deleteBatchStoreProducts(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; }
</style>
