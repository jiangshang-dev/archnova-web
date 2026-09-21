<template>
  <main class="page" v-if="detail">
    <div class="wrap detail">
      <div class="cat">{{ detail.category }}</div>
      <h1>{{ detail.title }}</h1>
      <p style="font-size: 18px; color: var(--muted)">{{ detail.summary }}</p>
      <div class="tags">
        <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
      <img v-if="detail.cover" class="cover-photo" :src="fileUrl(detail.cover)" alt="" />
      <div class="hero-actions" style="margin-bottom: 24px">
        <a v-if="detail.demoUrl" class="btn btn-ink" :href="detail.demoUrl" target="_blank" rel="noreferrer">打开演示</a>
        <a v-if="detail.githubUrl" class="btn btn-line" :href="detail.githubUrl" target="_blank" rel="noreferrer">查看代码</a>
        <router-link class="btn btn-line" to="/contact">想做类似的</router-link>
      </div>
      <article class="card prose">{{ detail.content }}</article>
      <div v-if="images.length" class="detail-images">
        <img v-for="item in images" :key="item" :src="fileUrl(item)" alt="" />
      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import http, { fileUrl } from '../api/http'

const route = useRoute()
const detail = ref(null)
const tags = computed(() => (detail.value?.techStack || '').split(',').map((item) => item.trim()).filter(Boolean))
const images = computed(() => (detail.value?.detailImages || '').split(',').map((item) => item.trim()).filter(Boolean))

onMounted(async () => {
  detail.value = await http.get(`/api/open/cases/${route.params.id}`)
})
</script>
