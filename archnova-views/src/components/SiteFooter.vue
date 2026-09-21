<template>
  <footer class="site-footer">
    <div>{{ site.company_name }} · {{ site.location }}</div>
    <div>
      <a v-if="site.email" :href="`mailto:${site.email}`">{{ site.email }}</a>
      <span v-if="site.wechat_id"> · 微信 {{ site.wechat_id }}</span>
    </div>
  </footer>
</template>

<script setup>
import { site } from '../store/site'
</script>
