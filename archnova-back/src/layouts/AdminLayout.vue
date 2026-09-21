<template>
  <a-layout class="admin-shell">
    <a-layout-sider theme="dark" width="220" :style="{ background: '#1c2b27' }">
      <div class="brand-side">极构后台</div>
      <a-menu theme="dark" mode="inline" :selected-keys="[selected]" :style="{ background: '#1c2b27' }">
        <a-menu-item key="/"><router-link to="/">概览</router-link></a-menu-item>
        <a-menu-item key="/cases"><router-link to="/cases">项目案例</router-link></a-menu-item>
        <a-menu-item key="/repos"><router-link to="/repos">开源项目</router-link></a-menu-item>
        <a-menu-item key="/shop-products"><router-link to="/shop-products">源码商品</router-link></a-menu-item>
        <a-menu-item key="/shop-orders"><router-link to="/shop-orders">商城订单</router-link></a-menu-item>
        <a-menu-item key="/contacts"><router-link to="/contacts">客户留言</router-link></a-menu-item>
        <a-menu-item key="/chat"><router-link to="/chat">在线客服</router-link></a-menu-item>
        <a-menu-item key="/config"><router-link to="/config">联系方式</router-link></a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header :style="{ background: '#fffdf9', display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }">
        <span style="margin-right: 12px">{{ realName }}</span>
        <a-button type="link" @click="logout">退出</a-button>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'

const route = useRoute()
const router = useRouter()
const realName = ref('管理员')
const selected = computed(() => route.path === '/' ? '/' : `/${route.path.split('/')[1]}`)

onMounted(async () => {
  const me = await http.get('/api/admin/me')
  realName.value = me.realName || me.username
})

function logout() {
  localStorage.removeItem('archnova-token')
  router.push('/login')
}
</script>
