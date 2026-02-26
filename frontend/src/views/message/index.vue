<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息管理</span>
          <div>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="warning" @click="openBroadcastDialog">一键发送</el-button>
            <el-button type="primary" @click="openSendDialog">发送消息</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type] || 'info'" size="small">{{ typeMap[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRead" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'success' : 'warning'" size="small">{{ row.isRead === 1 ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="180" align="center" />
        <el-table-column label="操作" fixed="right" align="right" width="120">
          <template #default="{ row }">
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

    <!-- Send Message Dialog -->
    <el-dialog v-model="sendDialogVisible" title="发送消息" width="500">
      <el-form :model="sendForm" :rules="sendRules" ref="sendFormRef" label-width="100px">
        <el-form-item label="接收用户" prop="userId">
          <el-select v-model="sendForm.userId" placeholder="请选择用户" style="width: 100%" filterable>
            <el-option v-for="u in userList" :key="u.id" :label="u.username" :value="u.id">
              <span>{{ u.username }}</span>
              <span style="float: right; color: #999; font-size: 12px">ID: {{ u.id }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="消息类型" prop="type">
          <el-select v-model="sendForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="系统消息" value="SYSTEM" />
            <el-option label="订单消息" value="ORDER" />
            <el-option label="优惠券消息" value="COUPON" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="sendForm.title" placeholder="请输入消息标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="sendForm.content" type="textarea" :rows="4" placeholder="请输入消息内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSend">发送</el-button>
      </template>
    </el-dialog>

    <!-- Broadcast Dialog -->
    <el-dialog v-model="broadcastDialogVisible" title="一键发送消息给所有用户" width="500">
      <el-form :model="broadcastForm" :rules="broadcastRules" ref="broadcastFormRef" label-width="100px">
        <el-form-item label="消息类型" prop="type">
          <el-select v-model="broadcastForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="系统消息" value="SYSTEM" />
            <el-option label="订单消息" value="ORDER" />
            <el-option label="优惠券消息" value="COUPON" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="broadcastForm.title" placeholder="请输入消息标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="broadcastForm.content" type="textarea" :rows="4" placeholder="请输入消息内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="broadcastDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBroadcast">发送给所有用户</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listMessagesPage, sendMessage, broadcastMessage, deleteMessage, deleteBatchMessages } from '../../api/message'
import { listUsers } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const typeMap: Record<string, string> = { SYSTEM: '系统消息', ORDER: '订单消息', COUPON: '优惠券消息' }
const typeTagMap: Record<string, string> = { SYSTEM: 'info', ORDER: 'warning', COUPON: 'success' }

const tableData = ref<any[]>([])
const userList = ref<any[]>([])
const selectedIds = ref<number[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const sendDialogVisible = ref(false)
const broadcastDialogVisible = ref(false)
const sendFormRef = ref<FormInstance>()
const broadcastFormRef = ref<FormInstance>()

const defaultSendForm = () => ({ userId: undefined as number | undefined, title: '', content: '', type: 'SYSTEM' })
const defaultBroadcastForm = () => ({ title: '', content: '', type: 'SYSTEM' })
const sendForm = reactive<any>(defaultSendForm())
const broadcastForm = reactive<any>(defaultBroadcastForm())

const sendRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}
const broadcastRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

async function loadData() {
  const res = await listMessagesPage(pageNum.value, pageSize.value)
  tableData.value = res.data?.records || []
  total.value = res.data?.total || 0
}

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id!)
}

function openSendDialog() {
  Object.assign(sendForm, defaultSendForm())
  sendDialogVisible.value = true
}

function openBroadcastDialog() {
  Object.assign(broadcastForm, defaultBroadcastForm())
  broadcastDialogVisible.value = true
}

async function handleSend() {
  const valid = await sendFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await sendMessage(sendForm)
  ElMessage.success('发送成功')
  sendDialogVisible.value = false
  loadData()
}

async function handleBroadcast() {
  const valid = await broadcastFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await broadcastMessage(broadcastForm)
  ElMessage.success('广播发送成功')
  broadcastDialogVisible.value = false
  loadData()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该消息？', '提示', { type: 'warning' })
  await deleteMessage(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条消息？`, '提示', { type: 'warning' })
  await deleteBatchMessages(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

onMounted(async () => {
  const userRes = await listUsers()
  userList.value = userRes.data || []
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
