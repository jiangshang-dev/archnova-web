<template>
  <button class="chat-fab" type="button" aria-label="在线客服" @click="open = !open">
    <svg v-if="open" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M6 6l12 12M18 6L6 18" />
    </svg>
    <svg v-else viewBox="0 0 24 24" fill="currentColor">
      <path d="M12 3a8 8 0 0 0-8 8v5.2A2.8 2.8 0 0 0 6.8 19H8v-6H6v-2a6 6 0 1 1 12 0v2h-2v6h1.2A2.8 2.8 0 0 0 20 16.2V11a8 8 0 0 0-8-8Z" />
    </svg>
  </button>
  <section v-if="open" class="chat-panel">
    <header class="chat-head">
        <div>
          <strong>在线咨询</strong>
          <span v-if="account">账号 {{ account }}</span>
          <span>IP {{ clientIp || '获取中' }}</span>
        </div>
      <span class="chat-status">{{ statusText }}</span>
    </header>
    <div ref="bodyRef" class="chat-body">
      <div v-if="!messages.length" class="chat-status">可以直接说你想做的产品和时间。</div>
      <div v-for="item in messages" :key="item.id" class="bubble" :class="item.senderType">
        {{ item.content }}
      </div>
    </div>
    <form class="chat-input" @submit.prevent="send">
      <input v-model="text" maxlength="1000" placeholder="输入消息" />
      <button class="btn btn-ink" type="submit">发送</button>
    </form>
  </section>
</template>

<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { chatSocketUrl } from '../api/http'

const open = ref(false)
const text = ref('')
const messages = ref([])
const clientIp = ref('')
const account = ref('')
const statusText = ref('连接中')
const bodyRef = ref(null)
let socket
let timer
let manualClose = false
let connectedToken = null

function visitorToken() {
  let token = localStorage.getItem('archnova-visitor')
  if (!token) {
    token = crypto.randomUUID()
    localStorage.setItem('archnova-visitor', token)
  }
  return token
}

function connect() {
  const shopToken = localStorage.getItem('archnova-shop-token') || ''
  if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) && connectedToken === shopToken) return
  manualClose = true
  if (socket) {
    socket.onclose = null
    socket.close()
  }
  manualClose = false
  connectedToken = shopToken
  const query = new URLSearchParams({ role: 'visitor', visitorToken: visitorToken() })
  if (shopToken) query.set('shopToken', shopToken)
  if (!shopToken) account.value = ''
  socket = new WebSocket(chatSocketUrl(query.toString()))
  socket.onopen = () => { statusText.value = '已连接' }
  socket.onclose = () => {
    if (manualClose) return
    statusText.value = '已断开，正在重连'
    clearTimeout(timer)
    timer = setTimeout(connect, 2500)
  }
  socket.onmessage = (event) => {
    const data = JSON.parse(event.data)
    if (data.type === 'ready') {
      clientIp.value = data.clientIp || ''
      account.value = data.account || ''
      messages.value = data.messages || []
      scroll()
    }
    if (data.type === 'message' && data.message) {
      if (!messages.value.some((item) => item.id === data.message.id)) {
        messages.value.push(data.message)
        scroll()
      }
    }
    if (data.type === 'error') statusText.value = data.message
  }
}

function send() {
  const content = text.value.trim()
  if (!content || !socket || socket.readyState !== WebSocket.OPEN) return
  socket.send(JSON.stringify({ action: 'send', content }))
  text.value = ''
}

function scroll() {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  })
}

watch(open, (value) => {
  if (value) connect()
})

onBeforeUnmount(() => {
  manualClose = true
  clearTimeout(timer)
  if (socket) socket.close()
})
</script>
