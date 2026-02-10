<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="openDialog()">新增商品</el-button>
          </div>
        </div>
      </template>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索商品名称/描述" clearable style="width: 300px" @clear="loadData" @keyup.enter="handleSearch">
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        <el-select v-model="filterCategoryId" placeholder="按分类筛选" clearable style="width: 160px" @change="handleFilter">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
      </div>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="'http://localhost:8080' + row.image" :preview-src-list="['http://localhost:8080' + row.image]" fit="cover" style="width: 60px; height: 60px; border-radius: 4px;" />
            <span v-else style="color: #ccc;">无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="barcode" label="条形码" width="140">
          <template #default="{ row }">
            <span v-if="row.barcode">{{ row.barcode }}</span>
            <span v-else style="color: #ccc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="categoryId" label="分类" width="100">
          <template #default="{ row }">
            {{ categoryMap[row.categoryId] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="employeeDiscountRate" label="员工折扣" width="100">
          <template #default="{ row }">{{ row.employeeDiscountRate ? (row.employeeDiscountRate * 100).toFixed(0) + '%' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="180">
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="600">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="商品图片">
          <div class="image-upload">
            <el-upload
              class="image-uploader"
              :show-file-list="false"
              :http-request="handleImageUpload"
              accept="image/*"
            >
              <el-image v-if="form.image" :src="'http://localhost:8080' + form.image" fit="cover" style="width: 120px; height: 120px;" />
              <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <el-button v-if="form.image" link type="danger" @click="form.image = ''">移除图片</el-button>
          </div>
        </el-form-item>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="条形码" prop="barcode">
          <el-input v-model="form.barcode" placeholder="请输入商品条形码" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="员工折扣率">
          <el-input-number v-model="form.employeeDiscountRate" :min="0" :max="1" :step="0.05" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { listProducts, addProduct, updateProduct, deleteProduct, deleteBatchProducts, searchProducts, getProductsByCategoryId, listProductsPage } from '../../api/product'
import { listCategories } from '../../api/category'
import { uploadImage } from '../../api/upload'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, UploadRequestOptions } from 'element-plus'
import type { Product, Category } from '../../types'

const tableData = ref<Product[]>([])
const categories = ref<Category[]>([])
const selectedIds = ref<number[]>([])
const searchKeyword = ref('')
const filterCategoryId = ref<number | undefined>(undefined)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categoryMap = computed(() => {
  const map: Record<number, string> = {}
  categories.value.forEach(c => { if (c.id) map[c.id] = c.name })
  return map
})

const defaultForm = (): Product => ({ name: '', price: 0, stock: 0, description: '', categoryId: undefined, employeeDiscountRate: undefined, barcode: '', image: '' })
const form = reactive<Product>(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

async function handleImageUpload(options: UploadRequestOptions) {
  try {
    const res = await uploadImage(options.file as File)
    form.image = res.data
    ElMessage.success('图片上传成功')
  } catch {
    ElMessage.error('图片上传失败')
  }
}

async function loadData() {
  const [res, catRes] = await Promise.all([listProductsPage(pageNum.value, pageSize.value), listCategories()])
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
  categories.value = catRes.data || []
}

async function handleSearch() {
  if (searchKeyword.value.trim()) {
    const res = await searchProducts(searchKeyword.value.trim())
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

async function handleFilter() {
  if (filterCategoryId.value) {
    const res = await getProductsByCategoryId(filterCategoryId.value)
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

function handleSelectionChange(rows: Product[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openDialog(row?: Product) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : defaultForm())
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateProduct(form)
    ElMessage.success('更新成功')
  } else {
    await addProduct(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  await deleteProduct(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个商品？`, '提示', { type: 'warning' })
  await deleteBatchProducts(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; }
.image-upload { display: flex; flex-direction: column; align-items: flex-start; gap: 8px; }
.image-uploader { border: 1px dashed var(--el-border-color); border-radius: 6px; cursor: pointer; overflow: hidden; width: 120px; height: 120px; display: flex; align-items: center; justify-content: center; }
.image-uploader:hover { border-color: var(--el-color-primary); }
.image-uploader-icon { font-size: 28px; color: #8c939d; }
</style>
