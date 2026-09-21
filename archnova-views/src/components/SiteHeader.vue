<template>
  <header class="site-header" :class="{ scrolled }">
    <router-link to="/" class="brand">
      <span class="mark"></span>
      {{ site.company_name || '极构科技' }}
    </router-link>
    <nav class="nav">
      <router-link to="/services">服务</router-link>
      <router-link to="/cases">案例</router-link>
      <router-link to="/shop">源码</router-link>
      <router-link to="/github">GitHub</router-link>
      <router-link to="/contact" class="nav-cta">联系我们</router-link>
    </nav>
  </header>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { loadSite, site } from '../store/site'

const scrolled = ref(false)
function onScroll() {
  scrolled.value = window.scrollY > 8
}
onMounted(() => {
  loadSite().catch(() => {})
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>
