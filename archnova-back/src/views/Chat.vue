<template>
  <a-card title="在线客服" :body-style="{ padding: '12px' }">
    <div class="chat-layout">
      <div style="overflow: auto; border-right: 1px solid #efe8dc">
        <div v-for="item in sessions" :key="item.id" class="session-item" :class="{ active: current === item.id }" @click="choose(item)">
          <div style="display: flex; justify-content: space-between">
            <b>{{ item.visitorName }}</b>
            <a-badge v-if="item.unreadCount" :count="item.unreadCount" />
          </div>
          <div class="ip">IP {{ item.clientIp || '未知' }}</div>
          <div style="color: #6f675e; font-size: 13px">{{ item.lastMessage || '还没有消息' }}</div>
        </div>
      </div>
      <div>
        <div class="messages" ref="box">
          <div v-if="current && activeSession" class="ip" style="margin-bottom: 8px">正在回复 {{ activeSession.visitorName }} · {{ activeSession.clientIp }}</div>
          <div v-for="item in messages" :key="item.id" class="bubble" :class="item.senderType">{{ item.content }}</div>
        </div>
        <a-input-search v-model:value="text" enter-button="发送" placeholder="回复访客" @search="send" />
        <div class="ip" style="margin-top: 8px">{{ statusText }}</div>
      </div>
    </div>
  </a-card>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { chatSocketUrl } from '../api/http'

const sessions = ref([])
const messages = ref([])
const current = ref(null)
const text = ref('')
const statusText = ref('连接中')
const box = ref(null)
let socket
let timer
let manualClose = false
const activeSession = computed(() => sessions.value.find((item) => item.id === current.value))

function connect() {
  const token = localStorage.getItem('archnova-token')
  socket = new WebSocket(chatSocketUrl(`role=admin&token=${encodeURIComponent(token || '')}`))
  socket.onopen = () => { statusText.value = '客服已连接' }
  socket.onclose = () => {
    if (manualClose) return
    statusText.value = '连接断开，正在重连'
    clearTimeout(timer)
    timer = setTimeout(connect, 2500)
  }
  socket.onmessage = (event) => {
    const data = JSON.parse(event.data)
    if (data.type === 'sessions') sessions.value = data.sessions || []
    if (data.type === 'history' && data.sessionId === current.value) {
      messages.value = data.messages || []
      scroll()
    }
    if (data.type === 'message' && data.message?.sessionId === current.value) {
      if (!messages.value.some((item) => item.id === data.message.id)) messages.value.push(data.message)
      scroll()
    }
    if (data.type === 'error') statusText.value = data.message
  }
}

function choose(item) {
  current.value = item.id
  messages.value = []
  if (socket?.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify({ action: 'history', sessionId: item.id }))
  }
}

function send() {
  const content = text.value.trim()
  if (!content || !current.value || socket?.readyState !== WebSocket.OPEN) return
  socket.send(JSON.stringify({ action: 'send', sessionId: current.value, content }))
  text.value = ''
}

function scroll() {
  nextTick(() => {
    if (box.value) box.value.scrollTop = box.value.scrollHeight
  })
}

onMounted(connect)
onBeforeUnmount(() => {
  manualClose = true
  clearTimeout(timer)
  socket?.close()
})
</script>
