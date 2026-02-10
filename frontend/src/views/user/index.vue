<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="success" @click="handleExport">导出Excel</el-button>
            <el-button type="primary" @click="openDialog()">新增用户</el-button>
          </div>
        </div>
      </template>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索用户名/手机号" clearable style="width: 300px" @clear="loadData" @keyup.enter="handleSearch">
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        <el-select v-model="filterRole" placeholder="按角色筛选" clearable style="width: 160px" @change="handleFilter">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="员工" value="EMPLOYEE" />
          <el-option label="顾客" value="CUSTOMER" />
        </el-select>
      </div>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar v-if="row.avatar" :src="BASE_URL + row.avatar" :size="40" />
            <el-avatar v-else :size="40">{{ row.username?.charAt(0).toUpperCase() }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'EMPLOYEE' ? 'warning' : 'success'">
              {{ roleMap[row.role] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isHotelEmployee" label="酒店员工" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isHotelEmployee ? 'success' : 'info'">{{ row.isHotelEmployee ? '是' : '否' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <el-avatar v-if="form.avatar" :src="BASE_URL + form.avatar" :size="80" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <el-button v-if="form.avatar" link type="danger" @click="form.avatar = ''">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="员工" value="EMPLOYEE" />
            <el-option label="顾客" value="CUSTOMER" />
          </el-select>
        </el-form-item>
        <el-form-item label="酒店员工">
          <el-switch v-model="form.isHotelEmployee" />
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
import { listUsers, addUser, updateUser, deleteUser, deleteBatchUsers, searchUsers, getUsersByRole, listUsersPage } from '../../api/user'
import { uploadImage } from '../../api/upload'
import { BASE_URL } from '../../api/request'
import { exportUsers } from '../../api/excel'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, UploadRequestOptions } from 'element-plus'
import type { User } from '../../types'

const roleMap: Record<string, string> = { ADMIN: '管理员', EMPLOYEE: '员工', CUSTOMER: '顾客' }
const tableData = ref<User[]>([])
const selectedIds = ref<number[]>([])
const searchKeyword = ref('')
const filterRole = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = (): User => ({ username: '', password: '', phone: '', role: 'ADMIN' as any, isHotelEmployee: false, avatar: '' })
const form = reactive<User>(defaultForm())

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function handleAvatarUpload(options: UploadRequestOptions) {
  try {
    const res = await uploadImage(options.file as File)
    form.avatar = res.data
    ElMessage.success('头像上传成功')
  } catch {
    ElMessage.error('头像上传失败')
  }
}

async function loadData() {
  const res = await listUsersPage(pageNum.value, pageSize.value)
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handleSearch() {
  if (searchKeyword.value.trim()) {
    const res = await searchUsers(searchKeyword.value.trim())
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

async function handleFilter() {
  if (filterRole.value) {
    const res = await getUsersByRole(filterRole.value)
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

function handleSelectionChange(rows: User[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openDialog(row?: User) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : defaultForm())
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateUser(form)
    ElMessage.success('更新成功')
  } else {
    await addUser(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该用户？', '提示', { type: 'warning' })
  await deleteUser(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个用户？`, '提示', { type: 'warning' })
  await deleteBatchUsers(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

function handleExport() {
  exportUsers()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; }
.avatar-upload { display: flex; align-items: center; gap: 12px; }
.avatar-uploader { border: 1px dashed var(--el-border-color); border-radius: 50%; cursor: pointer; overflow: hidden; width: 80px; height: 80px; display: flex; align-items: center; justify-content: center; }
.avatar-uploader:hover { border-color: var(--el-color-primary); }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; }
</style>
