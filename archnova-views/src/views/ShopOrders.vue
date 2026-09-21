<template>
  <main class="page">
    <div class="wrap">
      <div class="section-head">
        <h2 class="serif" style="margin: 0">我的订单</h2>
        <span v-if="profile">剩余 {{ profile.points }} 积分</span>
      </div>
      <div class="filters">
        <button v-for="item in tabs" :key="item.label" class="chip" :class="{ on: status === item.value }" @click="change(item.value)">
          {{ item.label }}
        </button>
      </div>
      <article v-for="item in records" :key="item.id" class="card" style="margin-bottom: 12px">
        <div style="display: flex; justify-content: space-between; gap: 12px">
          <b>{{ item.productTitle }}</b>
          <span>{{ statusText(item.status) }}</span>
        </div>
        <p style="color: var(--muted)">{{ item.orderNo }} · {{ payText(item.payType) }} · {{ item.createTime }}</p>
        <p v-if="item.payType === 'POINTS'">{{ item.pointsCost }} 积分</p>
        <p v-else>¥ {{ yuan(item.amountCent) }}</p>
        <a v-if="item.status === 1 && item.sourceUrl" :href="item.sourceUrl" target="_blank" rel="noreferrer">查看源码</a>
        <p v-if="item.status === 0" style="color: var(--muted)">超过 24 小时未支付会自动取消。</p>
      </article>
      <p v-if="!records.length" style="color: var(--muted)">这个状态下还没有订单。</p>
    </div>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'

const route = useRoute()
const router = useRouter()
const records = ref([])
const profile = ref(null)
const status = ref(route.query.status === undefined ? null : Number(route.query.status))
const tabs = [
  { label: '全部', value: null },
  { label: '待支付', value: 0 },
  { label: '已完成', value: 1 },
  { label: '已取消', value: 2 }
]

function yuan(cent) {
  return ((cent || 0) / 100).toFixed(2)
}
function statusText(value) {
  return ['待支付', '已完成', '已取消'][value] || ''
}
function payText(value) {
  return { POINTS: '积分', ALIPAY: '支付宝', WECHAT: '微信' }[value] || value
}

async function load() {
  const params = { page: 1, size: 50 }
  if (status.value !== null && status.value !== undefined && !Number.isNaN(status.value)) params.status = status.value
  const data = await http.get('/api/shop/orders', { params })
  records.value = data.records || []
}

function change(value) {
  status.value = value
  router.replace({ path: '/shop/orders', query: value === null ? {} : { status: value } })
  load()
}

onMounted(async () => {
  profile.value = await http.get('/api/shop/me')
  await load()
})
</script>
