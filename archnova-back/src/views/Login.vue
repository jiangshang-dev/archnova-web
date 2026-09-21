<template>
  <div class="login-page">
    <section class="login-side">
      <div>ARCHNOVA</div>
      <h1>极构科技<br />管理后台</h1>
      <p>维护案例、联系方式和在线客服。</p>
    </section>
    <section class="login-form">
      <div class="login-card">
        <h2>登录</h2>
        <a-form layout="vertical" :model="form" @finish="submit">
          <a-form-item label="账号" name="username" :rules="[{ required: true, message: '请输入账号' }]">
            <a-input v-model:value="form.username" />
          </a-form-item>
          <a-form-item label="密码" name="password" :rules="[{ required: true, message: '请输入密码' }]">
            <a-input-password v-model:value="form.password" />
          </a-form-item>
          <a-button type="primary" html-type="submit" block :loading="loading">进入后台</a-button>
        </a-form>
        <p style="color: #6f675e; margin-top: 16px">首次启动默认账号 admin / admin123</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '' })

async function submit() {
  loading.value = true
  try {
    const data = await http.post('/api/admin/login', form)
    localStorage.setItem('archnova-token', data.token)
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>
