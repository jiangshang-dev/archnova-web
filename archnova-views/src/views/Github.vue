<template>
  <main class="page">
    <div class="wrap">
      <div class="kicker">GITHUB</div>
      <h2 class="serif" style="font-size: 42px">我们的开源项目</h2>
      <p style="color: var(--muted)">
        这些是可以打开看的代码。
<!--        <a :href="site.github_url" target="_blank" rel="noreferrer">{{ site.github_user }}</a>-->
      </p>
      <div class="grid-3">
        <article v-for="item in repos" :key="item.name" class="card repo">
          <div class="lang">{{ item.language || '其他' }} · {{ item.githubUpdated }}</div>
          <h3>{{ item.name }}</h3>
          <p>{{ item.description || '暂无简介' }}</p>
          <a :href="item.htmlUrl" target="_blank" rel="noreferrer">在 GitHub 打开</a>
        </article>
      </div>
      <p v-if="!repos.length" style="color: var(--muted)">还没有对外展示的开源项目。</p>
    </div>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'
import { site } from '../store/site'

const repos = ref([])
onMounted(async () => {
  repos.value = await http.get('/api/open/github')
})
</script>
