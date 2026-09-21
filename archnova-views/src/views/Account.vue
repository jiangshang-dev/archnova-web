<template>
  <main class="page">
    <div class="wrap account">
      <aside class="card account-side">
        <div class="account-avatar">{{ (shopUser.nickname || shopUser.username || '买').slice(0, 1) }}</div>
        <strong>{{ shopUser.nickname || shopUser.username }}</strong>
        <p>{{ shopUser.username }}</p>
        <p>{{ shopUser.points }} 积分</p>
        <router-link to="/shop/orders">查看订单</router-link>
      </aside>
      <section class="card">
        <h2>个人中心</h2>
        <form class="account-form" @submit.prevent="saveNickname">
          <h3>改昵称</h3>
          <input v-model="nickname" maxlength="32" />
          <button class="btn btn-ink" type="submit" :disabled="savingName">保存昵称</button>
        </form>
        <form class="account-form" @submit.prevent="saveEmail">
          <h3>设置邮箱</h3>
          <p>当前邮箱：{{ shopUser.email || '未设置' }}</p>
          <input v-model="email" type="email" placeholder="新邮箱" />
          <div class="auth-code">
            <input v-model="code" maxlength="6" placeholder="验证码" />
            <button type="button" :disabled="seconds > 0 || sending" @click="sendCode">{{ seconds > 0 ? `${seconds}s` : '获取验证码' }}</button>
          </div>
          <button class="btn btn-ink" type="submit" :disabled="savingEmail">保存邮箱</button>
        </form>
      </section>
    </div>
  </main>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import http from '../api/http'
import { refreshShopUser, saveShopUser, shopUser } from '../store/shopUser'

const nickname = ref('')
const email = ref('')
const code = ref('')
const savingName = ref(false)
const savingEmail = ref(false)
const sending = ref(false)
const seconds = ref(0)
let timer

async function saveNickname() {
  savingName.value = true
  try {
    saveShopUser(await http.post('/api/shop/profile/nickname', { nickname: nickname.value }))
    message.success('昵称已更新')
  } finally {
    savingName.value = false
  }
}

async function sendCode() {
  sending.value = true
  try {
    await http.post('/api/shop/profile/email-code', { email: email.value })
    seconds.value = 60
    timer = setInterval(() => {
      seconds.value -= 1
      if (seconds.value <= 0) clearInterval(timer)
    }, 1000)
  } finally {
    sending.value = false
  }
}

async function saveEmail() {
  savingEmail.value = true
  try {
    saveShopUser(await http.post('/api/shop/profile/email', { email: email.value, code: code.value }))
    code.value = ''
    message.success('邮箱已更新')
  } finally {
    savingEmail.value = false
  }
}

onMounted(async () => {
  await refreshShopUser()
  nickname.value = shopUser.nickname || ''
})
onBeforeUnmount(() => clearInterval(timer))
</script>
