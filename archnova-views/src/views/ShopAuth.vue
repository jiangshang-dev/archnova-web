<template>
  <main class="page">
    <div class="wrap" style="max-width: 480px">
      <h2 class="serif">{{ mode === 'login' ? '买家登录' : '注册买家' }}</h2>
      <p style="color: var(--muted)">浏览网站不需要账号。购买源码和查看订单时才登录。</p>
      <a-form layout="vertical" :model="form" @finish="submit">
        <a-form-item label="账号"><a-input v-model:value="form.username" /></a-form-item>
        <a-form-item v-if="mode === 'register'" label="昵称"><a-input v-model:value="form.nickname" /></a-form-item>
        <a-form-item label="密码"><a-input-password v-model:value="form.password" /></a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" block>{{ mode === 'login' ? '登录' : '注册并登录' }}</a-button>
      </a-form>
      <a-button type="link" style="margin-top: 8px" @click="mode = mode === 'login' ? 'register' : 'login'">
        {{ mode === 'login' ? '没有账号，去注册' : '已有账号，去登录' }}
      </a-button>
    </div>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'

const route = useRoute()
const router = useRouter()
const mode = ref('login')
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '' })

async function submit() {
  loading.value = true
  try {
    const path = mode.value === 'login' ? '/api/shop/login' : '/api/shop/register'
    const data = await http.post(path, form)
    localStorage.setItem('archnova-shop-token', data.token)
    router.push(route.query.redirect || '/shop')
  } finally {
    loading.value = false
  }
}
</script>
