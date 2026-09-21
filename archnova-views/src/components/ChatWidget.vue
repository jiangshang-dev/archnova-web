<template>
  <button class="chat-fab" type="button" @click="open = !open">{{ open ? '关' : '聊' }}</button>
  <section v-if="open" class="chat-panel">
    <header class="chat-head">
      <div>
        <strong>在线咨询</strong>
        <span>当前 IP {{ clientIp || '获取中' }}</span>
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
const statusText = ref('连接中')
const bodyRef = ref(null)
let socket
let timer
let manualClose = false

function visitorToken() {
  let token = localStorage.getItem('archnova-visitor')
  if (!token) {
    token = crypto.randomUUID()
    localStorage.setItem('archnova-visitor', token)
  }
  return token
}

function connect() {
  if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) return
  socket = new WebSocket(chatSocketUrl(`role=visitor&visitorToken=${encodeURIComponent(visitorToken())}`))
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
