<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户优惠券</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="warning" @click="handleDistribute">一键发放</el-button>
            <el-button type="primary" @click="openDialog()">新增</el-button>
          </div>
        </div>
      </template>
      <div class="search-bar">
        <el-select v-model="filterStatus" placeholder="按状态筛选" clearable style="width: 200px" @change="handleFilter">
          <el-option label="可用" value="AVAILABLE" />
          <el-option label="已使用" value="USED" />
          <el-option label="已过期" value="EXPIRED" />
        </el-select>
      </div>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户" width="120">
          <template #default="{ row }">{{ userMap[row.userId] || '用户' + row.userId }}</template>
        </el-table-column>
        <el-table-column prop="couponId" label="优惠券" width="140">
          <template #default="{ row }">{{ couponMap[row.couponId] || '优惠券' + row.couponId }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'AVAILABLE' ? 'success' : row.status === 'USED' ? 'info' : 'danger'">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="useTime" label="使用时间" width="180" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户优惠券' : '新增用户优惠券'" width="500">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="form.userId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="优惠券ID" prop="couponId">
          <el-input-number v-model="form.couponId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="已使用" value="USED" />
            <el-option label="已过期" value="EXPIRED" />
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
import { listUserCoupons, getUserCouponsByStatus, addUserCoupon, updateUserCoupon, deleteUserCoupon, deleteBatchUserCoupons, listUserCouponsPage, distributeToAllUsers } from '../../api/userCoupon'
import { listUsers } from '../../api/user'
import { listCoupons } from '../../api/coupon'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { UserCoupon, User, Coupon } from '../../types'

const statusMap: Record<string, string> = { AVAILABLE: '可用', USED: '已使用', EXPIRED: '已过期' }
const tableData = ref<UserCoupon[]>([])
const selectedIds = ref<number[]>([])
const users = ref<User[]>([])
const coupons = ref<Coupon[]>([])
const filterStatus = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const userMap = computed(() => {
  const map: Record<number, string> = {}
  users.value.forEach(u => { if (u.id) map[u.id] = u.username })
  return map
})

const couponMap = computed(() => {
  const map: Record<number, string> = {}
  coupons.value.forEach(c => { if (c.id) map[c.id] = c.name })
  return map
})

const defaultForm = (): UserCoupon => ({ userId: 1, couponId: 1, status: 'AVAILABLE' as any })
const form = reactive<UserCoupon>(defaultForm())

const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  couponId: [{ required: true, message: '请输入优惠券ID', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

async function loadData() {
  const res = await listUserCouponsPage(pageNum.value, pageSize.value)
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handleFilter() {
  if (filterStatus.value) {
    const res = await getUserCouponsByStatus(filterStatus.value)
    tableData.value = res.data || []
  } else {
    loadData()
  }
}

function handleSelectionChange(rows: UserCoupon[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openDialog(row?: UserCoupon) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : defaultForm())
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) {
    await updateUserCoupon(form)
    ElMessage.success('更新成功')
  } else {
    await addUserCoupon(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该用户优惠券？', '提示', { type: 'warning' })
  await deleteUserCoupon(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条用户优惠券？`, '提示', { type: 'warning' })
  await deleteBatchUserCoupons(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

async function handleDistribute() {
  if (!coupons.value.length) {
    ElMessage.warning('暂无优惠券模板')
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请选择要发放的优惠券ID', '一键发放优惠券给所有用户', {
      inputValue: coupons.value[0]?.id?.toString() || '',
      inputPlaceholder: '优惠券ID',
      confirmButtonText: '确定发放',
      cancelButtonText: '取消',
      inputValidator: (v: string) => {
        if (!v || isNaN(Number(v))) return '请输入有效的优惠券ID'
        return true
      }
    })
    await distributeToAllUsers(Number(value))
    ElMessage.success('发放成功')
    loadData()
  } catch {
    // cancelled
  }
}

onMounted(async () => {
  try {
    const [userRes, couponRes] = await Promise.all([listUsers(), listCoupons()])
    users.value = userRes.data || []
    coupons.value = couponRes.data || []
  } catch {}
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; }
</style>
