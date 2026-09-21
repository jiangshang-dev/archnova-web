<template>
  <main class="page">
    <div class="auth-shell">
      <section class="auth-side">
        <div class="kicker">ACCOUNT</div>
        <h1>登录后购买源码、查看订单</h1>
        <p>浏览网站不需要账号。同一个表单可以用用户名或邮箱登录。</p>
      </section>
      <section class="auth-card">
        <div class="auth-tabs">
          <button type="button" :class="{ on: mode === 'login' }" @click="mode = 'login'">登录</button>
          <button type="button" :class="{ on: mode === 'register' }" @click="mode = 'register'">注册</button>
        </div>
        <form v-if="mode === 'login'" @submit.prevent="login">
          <label>账号</label>
          <input v-model="loginForm.account" placeholder="用户名或邮箱" autocomplete="username" />
          <label>密码</label>
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" autocomplete="current-password" />
          <button class="auth-submit" type="submit" :disabled="loading">{{ loading ? '登录中' : '登录' }}</button>
        </form>
        <form v-else @submit.prevent="register">
          <label>邮箱</label>
          <input v-model="registerForm.email" type="email" placeholder="用于接收验证码" autocomplete="email" />
          <label>验证码</label>
          <div class="auth-code">
            <input v-model="registerForm.code" maxlength="6" placeholder="6 位验证码" />
            <button type="button" :disabled="seconds > 0 || sending" @click="sendCode">{{ seconds > 0 ? `${seconds}s` : '获取验证码' }}</button>
          </div>
          <label>用户名</label>
          <input v-model="registerForm.username" placeholder="2 到 32 位，登录时可用" autocomplete="username" />
          <label>密码</label>
          <input v-model="registerForm.password" type="password" placeholder="至少 6 位" autocomplete="new-password" />
          <button class="auth-submit" type="submit" :disabled="loading">{{ loading ? '提交中' : '注册并登录' }}</button>
        </form>
      </section>
    </div>
  </main>
</template>

<script setup>
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { saveShopUser } from '../store/shopUser'

const route = useRoute()
const router = useRouter()
const mode = ref(route.query.mode === 'register' ? 'register' : 'login')
const loading = ref(false)
const sending = ref(false)
const seconds = ref(0)
const loginForm = reactive({ account: '', password: '' })
const registerForm = reactive({ email: '', code: '', username: '', password: '' })
let timer

async function login() {
  loading.value = true
  try {
    saveShopUser(await http.post('/api/shop/login', loginForm))
    router.push(route.query.redirect || '/shop')
  } finally {
    loading.value = false
  }
}

async function sendCode() {
  sending.value = true
  try {
    await http.post('/api/shop/email-code', { email: registerForm.email })
    seconds.value = 60
    timer = setInterval(() => {
      seconds.value -= 1
      if (seconds.value <= 0) clearInterval(timer)
    }, 1000)
  } finally {
    sending.value = false
  }
}

async function register() {
  loading.value = true
  try {
    saveShopUser(await http.post('/api/shop/register', registerForm))
    router.push(route.query.redirect || '/account')
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => clearInterval(timer))
</script>
