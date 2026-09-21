<template>
  <main class="page">
    <div class="wrap">
      <div class="section-head">
        <div>
          <div class="kicker">SOURCE</div>
          <h2 class="serif" style="font-size: 42px; margin: 8px 0">源码商城</h2>
        </div>
        <div>
          <router-link to="/shop/orders">我的订单</router-link>
          <span v-if="profile"> · {{ profile.nickname }} · {{ profile.points }} 积分</span>
          <router-link v-else to="/shop/login"> 登录</router-link>
        </div>
      </div>
      <div class="grid-3">
        <router-link v-for="item in products" :key="item.id" :to="`/shop/${item.id}`" class="card case-card">
          <img v-if="item.cover" :src="fileUrl(item.cover)" alt="" class="case-cover" />
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary }}</p>
          <div class="more price">¥ {{ yuan(item.priceCent) }} · {{ item.pointsPrice }} 积分</div>
        </router-link>
      </div>
    </div>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http, { fileUrl } from '../api/http'

const products = ref([])
const profile = ref(null)

function yuan(cent) {
  return ((cent || 0) / 100).toFixed(2)
}

onMounted(async () => {
  products.value = await http.get('/api/open/shop/products')
  if (localStorage.getItem('archnova-shop-token')) {
    profile.value = await http.get('/api/shop/me').catch(() => null)
  }
})
</script>
