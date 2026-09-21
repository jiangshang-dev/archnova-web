<template>
  <a-config-provider :theme="theme">
    <SiteHeader />
    <router-view />
    <SiteFooter />
    <ChatWidget />
  </a-config-provider>
</template>

<script setup>
import { theme as antdTheme } from 'ant-design-vue'
import SiteHeader from './components/SiteHeader.vue'
import SiteFooter from './components/SiteFooter.vue'
import ChatWidget from './components/ChatWidget.vue'

const theme = {
  algorithm: antdTheme.defaultAlgorithm,
  token: {
    colorPrimary: '#d4531a',
    borderRadius: 10,
    fontFamily: '"Noto Sans SC", "PingFang SC", sans-serif'
  }
}
</script>
