<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="register-title">用户注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（选填）" prefix-icon="Phone" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleRegister">注 册</el-button>
        </el-form-item>
        <div class="login-link">
          已有账号？<el-link type="primary" @click="goLogin">返回登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../../api/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', phone: '', password: '', confirmPassword: '' })

const validateConfirm = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register({ username: form.username, password: form.password, phone: form.phone || undefined })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('https://images.unsplash.com/photo-1604719312566-8912e9227c6a?w=1920&q=80') center/cover no-repeat;
  position: relative;
}
.register-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
}
.register-card {
  width: 400px;
  padding: 20px;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
}
.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}
.login-link {
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style>
