<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <span>店铺商品管理</span>
            <div class="stats-badges">
              <el-tag type="info" size="small">共计: {{ stats.total }}</el-tag>
              <el-tag type="success" size="small">上架: {{ stats.onShelf }}</el-tag>
              <el-tag type="danger" size="small">下架: {{ stats.offShelf }}</el-tag>
            </div>
          </div>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="warning" :disabled="!selectedIds.length" @click="openCouponDialog">一键设置优惠券</el-button>
            <el-button type="success" @click="handleExport">导出Excel</el-button>
            <el-upload
              action=""
              :before-upload="handleImport"
              :show-file-list="false"
              accept=".xlsx,.xls"
              style="display: inline-block; margin-left: 12px;"
            >
              <el-button type="info">导入Excel</el-button>
            </el-upload>
            <el-button type="primary" @click="openDialog()" style="margin-left: 12px;">新增店铺商品</el-button>
          </div>
        </div>
      </template>
      <div class="search-bar">
        <el-select v-model="filterStoreId" placeholder="按店铺筛选" clearable style="width: 200px" @change="loadData">
          <el-option v-for="s in storeList" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-input v-model="searchProductName" placeholder="按商品名称搜索" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData">
          <template #append>
            <el-button @click="loadData">搜索</el-button>
          </template>
        </el-input>
      </div>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="storeId" label="店铺ID" width="80" align="center" />
        <el-table-column label="店铺名称" width="140">
          <template #default="{ row }">
            {{ row.storeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="productId" label="商品ID" width="80" align="center" />
        <el-table-column label="商品名称" width="140">
          <template #default="{ row }">
            {{ row.productName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="storePrice" label="店铺售价" width="100" align="center">
          <template #default="{ row }">¥{{ row.storePrice }}</template>
        </el-table-column>
        <el-table-column prop="storeStock" label="店铺库存" width="100" align="center" />
        <el-table-column prop="safetyStock" label="安全库存" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.storeStock < (row.safetyStock || 10)" type="danger" size="small">{{ row.safetyStock || 10 }}</el-tag>
            <span v-else>{{ row.safetyStock || 10 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="优惠券" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.couponId">{{ row.couponName || row.couponId }}</span>
            <span v-else style="color: #999;">无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="80" align="center">
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
        <el-form-item label="商品" :prop="isEdit ? '' : 'productName'">
          <template v-if="isEdit">
            <el-select v-model="form.productId" placeholder="请选择商品" style="width: 100%" disabled>
              <el-option v-for="p in productList" :key="p.id" :label="p.name" :value="p.id" />
            </el-select>
          </template>
          <template v-else>
            <el-autocomplete
              v-model="form.productName"
              :fetch-suggestions="queryProductSuggestions"
              placeholder="输入商品名称（可新建）"
              style="width: 100%"
              @select="handleProductSelect"
              clearable
            >
              <template #default="{ item }">
                <span>{{ item.value }}</span>
                <span style="float: right; color: #999; font-size: 12px">ID: {{ item.id }}</span>
              </template>
            </el-autocomplete>
            <div v-if="form.productName && !form.productId" style="color: #E6A23C; font-size: 12px; margin-top: 4px;">
              未找到该商品，提交后将自动创建
            </div>
          </template>
        </el-form-item>
        <el-form-item label="店铺售价" prop="storePrice">
          <el-input-number v-model="form.storePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="店铺库存" prop="storeStock">
          <el-input-number v-model="form.storeStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="安全库存" prop="safetyStock">
          <el-input-number v-model="form.safetyStock" :min="1" style="width: 100%" />
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

    <el-dialog v-model="couponDialogVisible" title="一键设置优惠券" width="500">
      <el-form label-width="120px">
        <el-form-item label="优惠券模板">
          <el-select v-model="selectedCouponId" placeholder="请选择优惠券模板" style="width: 100%" filterable>
            <el-option v-for="c in couponList" :key="c.id" :label="c.name" :value="c.id">
              <span>{{ c.name }}</span>
              <span style="float: right; color: #999; font-size: 12px">ID: {{ c.id }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="优惠券ID">
          <el-input :model-value="selectedCouponId ? String(selectedCouponId) : ''" disabled />
        </el-form-item>
        <el-form-item label="已选商品数">
          <el-input :model-value="String(selectedIds.length)" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="couponDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedCouponId" @click="handleBatchSetCoupon">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listStoreProducts, listStoreProductsPage, addStoreProduct, addStoreProductWithName, updateStoreProduct, deleteStoreProduct, deleteBatchStoreProducts, batchSetCoupon, getStoreProductsByStoreId } from '../../api/storeProduct'
import { listStores } from '../../api/store'
import { listProducts } from '../../api/product'
import { listCoupons } from '../../api/coupon'
import { BASE_URL } from '../../api/request'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadRawFile } from 'element-plus'

const tableData = ref<any[]>([])
const storeList = ref<any[]>([])
const productList = ref<any[]>([])
const couponList = ref<any[]>([])
const selectedIds = ref<number[]>([])
const filterStoreId = ref<number | undefined>(undefined)
const searchProductName = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const couponDialogVisible = ref(false)
const selectedCouponId = ref<number | undefined>(undefined)

const stats = reactive({ total: 0, onShelf: 0, offShelf: 0 })

const defaultForm = () => ({ storeId: undefined, productId: undefined, productName: '', storePrice: undefined as number | undefined, storeStock: 0, safetyStock: 10, status: 1 })
const form = reactive<any>(defaultForm())

const rules = {
  storeId: [{ required: true, message: '请选择店铺', trigger: 'change' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  storePrice: [{ required: true, message: '请输入店铺售价', trigger: 'blur' }],
  storeStock: [{ required: true, message: '请输入店铺库存', trigger: 'blur' }]
}

async function loadData() {
  const [res, storeRes, productRes, couponRes] = await Promise.all([
    listStoreProductsPage(pageNum.value, pageSize.value, filterStoreId.value, searchProductName.value.trim() || undefined),
    listStores(),
    listProducts(),
    listCoupons()
  ])
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0

  // Compute accurate stats using full data (not paginated)
  let allProducts: any[] = []
  if (filterStoreId.value) {
    const allRes = await getStoreProductsByStoreId(filterStoreId.value)
    allProducts = allRes.data || []
  } else {
    const allRes = await listStoreProducts()
    allProducts = allRes.data || []
  }
  stats.total = allProducts.length
  stats.onShelf = allProducts.filter((r: any) => r.status === 1).length
  stats.offShelf = allProducts.filter((r: any) => r.status !== 1).length

  storeList.value = storeRes.data || []
  productList.value = productRes.data || []
  couponList.value = couponRes.data || []
}

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openDialog(row?: any) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { ...row, productName: row.productName || '' })
  } else {
    Object.assign(form, defaultForm())
  }
  dialogVisible.value = true
}

function queryProductSuggestions(queryString: string, cb: (results: any[]) => void) {
  const results = productList.value
    .filter((p: any) => p.name.toLowerCase().includes(queryString.toLowerCase()))
    .map((p: any) => ({ value: p.name, id: p.id, price: p.price }))
  cb(results)
}

function handleProductSelect(item: any) {
  form.productId = item.id
  form.productName = item.value
  if (!form.storePrice && item.price) {
    form.storePrice = item.price
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateStoreProduct(form)
    ElMessage.success('更新成功')
  } else {
    if (form.productId) {
      await addStoreProduct(form)
    } else {
      await addStoreProductWithName({
        productName: form.productName,
        storeId: form.storeId,
        storePrice: form.storePrice,
        storeStock: form.storeStock,
        safetyStock: form.safetyStock,
        status: form.status
      })
    }
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

function handleExport() {
  const token = localStorage.getItem('satoken')
  const params: any = {}
  if (filterStoreId.value) params.storeId = filterStoreId.value
  if (searchProductName.value.trim()) params.productName = searchProductName.value.trim()
  axios.get(BASE_URL + '/api/excel/export/store-products', {
    params,
    responseType: 'blob',
    headers: token ? { satoken: token } : {}
  }).then(res => {
    const blob = new Blob([res.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '店铺商品数据.xlsx'
    link.click()
    URL.revokeObjectURL(link.href)
  })
}

function handleImport(file: UploadRawFile) {
  const token = localStorage.getItem('satoken')
  const formData = new FormData()
  formData.append('file', file)
  axios.post(BASE_URL + '/api/excel/import/store-products', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      ...(token ? { satoken: token } : {})
    }
  }).then(res => {
    ElMessage.success(res.data?.data || '导入成功')
    loadData()
  }).catch(() => {
    ElMessage.error('导入失败')
  })
  return false
}

function openCouponDialog() {
  selectedCouponId.value = undefined
  couponDialogVisible.value = true
}

async function handleBatchSetCoupon() {
  if (!selectedCouponId.value) return
  await batchSetCoupon(selectedIds.value, selectedCouponId.value)
  ElMessage.success('优惠券设置成功')
  couponDialogVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header-left { display: flex; align-items: center; gap: 12px; }
.stats-badges { display: flex; gap: 8px; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; }
</style>
