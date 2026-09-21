<template>
  <main class="page" v-if="detail">
    <article class="wrap story">
      <router-link class="story-back" to="/cases">全部案例</router-link>
      <div class="story-kicker">{{ detail.category }}</div>
      <h1>{{ detail.title }}</h1>
      <p class="story-lead">{{ detail.summary }}</p>
      <div class="story-bar">
        <div class="tags">
          <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
        </div>
        <div class="hero-actions">
          <a v-if="detail.demoUrl" class="btn btn-ink" :href="detail.demoUrl" target="_blank" rel="noreferrer">打开演示</a>
          <a v-if="detail.githubUrl" class="btn btn-line" :href="detail.githubUrl" target="_blank" rel="noreferrer">查看代码</a>
          <router-link class="btn btn-line" to="/contact">想做类似的</router-link>
        </div>
      </div>
      <figure v-if="detail.cover" class="story-cover">
        <img :src="fileUrl(detail.cover)" alt="" />
      </figure>
      <div class="story-body markdown" v-html="html"></div>
      <div v-if="images.length" class="story-shots">
        <img v-for="item in images" :key="item" :src="fileUrl(item)" alt="" />
      </div>
    </article>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import MarkdownIt from 'markdown-it'
import http, { fileUrl } from '../api/http'

const md = new MarkdownIt({ html: false, linkify: true, breaks: true })
const route = useRoute()
const detail = ref(null)
const tags = computed(() => (detail.value?.techStack || '').split(',').map((item) => item.trim()).filter(Boolean))
const images = computed(() => (detail.value?.detailImages || '').split(',').map((item) => item.trim()).filter(Boolean))
const html = computed(() => md.render(detail.value?.content || ''))

onMounted(async () => {
  detail.value = await http.get(`/api/open/cases/${route.params.id}`)
})
</script>
