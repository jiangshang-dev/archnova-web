<template>
  <header class="site-header" :class="{ scrolled }">
    <router-link to="/" class="brand">
      <span class="mark"></span>
      {{ site.company_name || '极构科技' }}
    </router-link>
    <nav class="nav">
      <router-link to="/services">服务</router-link>
      <router-link to="/cases">案例</router-link>
      <router-link to="/shop">商城</router-link>
      <router-link to="/github">GitHub</router-link>
      <router-link v-if="!shopUser.username" to="/shop/login">登录</router-link>
      <div v-else class="nav-user">
        <button type="button" @click="menuOpen = !menuOpen">{{ shopUser.nickname || shopUser.username }}</button>
        <div v-if="menuOpen" class="nav-menu">
          <router-link to="/account" @click="menuOpen = false">个人中心</router-link>
          <router-link to="/shop/orders" @click="menuOpen = false">我的订单</router-link>
          <button type="button" @click="logout">退出</button>
        </div>
      </div>
      <router-link to="/contact" class="nav-cta">联系我们</router-link>
    </nav>
  </header>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loadSite, site } from '../store/site'
import { clearShopUser, refreshShopUser, shopUser } from '../store/shopUser'

const router = useRouter()
const scrolled = ref(false)
const menuOpen = ref(false)

function onScroll() {
  scrolled.value = window.scrollY > 8
}

function logout() {
  menuOpen.value = false
  clearShopUser()
  router.push('/')
}

onMounted(() => {
  loadSite().catch(() => {})
  refreshShopUser().catch(() => {})
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>
