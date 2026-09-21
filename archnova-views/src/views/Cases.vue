<template>
  <main class="page">
    <div class="wrap">
      <div class="kicker">CASES</div>
      <h2 class="serif" style="font-size: 42px">项目案例</h2>
      <p style="color: var(--muted)">演示地址和仓库地址都可以在后台改。没有演示时，先看说明和代码。</p>
      <div class="filters">
        <button v-for="item in filters" :key="item" class="chip" :class="{ on: category === item }" @click="category = item">
          {{ item }}
        </button>
      </div>
      <div class="grid-3">
        <router-link v-for="item in shown" :key="item.id" :to="`/cases/${item.id}`" class="card case-card">
          <img v-if="item.cover" :src="fileUrl(item.cover)" alt="" class="case-cover" />
          <div class="cat">{{ item.category }}</div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary }}</p>
          <div class="tags">
            <span v-for="tag in tags(item.techStack)" :key="tag" class="tag">{{ tag }}</span>
          </div>
          <div class="more">查看案例</div>
        </router-link>
      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http, { fileUrl } from '../api/http'

const filters = ['全部', '小程序', 'App', '网站', '企业系统', '桌面应用']
const category = ref('全部')
const cases = ref([])
const shown = computed(() => category.value === '全部' ? cases.value : cases.value.filter((item) => item.category === category.value))

function tags(value) {
  return (value || '').split(',').map((item) => item.trim()).filter(Boolean).slice(0, 3)
}

onMounted(async () => {
  cases.value = await http.get('/api/open/cases')
})
</script>
