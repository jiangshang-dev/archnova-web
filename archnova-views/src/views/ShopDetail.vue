<template>
  <main class="page" v-if="product">
    <div class="wrap detail">
      <img v-if="product.cover" class="cover-photo" :src="fileUrl(product.cover)" alt="" />
      <h1>{{ product.title }}</h1>
      <p style="color: var(--muted)">{{ product.summary }}</p>
      <p class="price">¥ {{ yuan(product.priceCent) }}　或　{{ product.pointsPrice }} 积分</p>
      <div class="card markdown" v-html="html"></div>
      <div v-if="images.length" class="detail-images">
        <img v-for="item in images" :key="item" :src="fileUrl(item)" alt="" />
      </div>
      <div class="card" style="margin-top: 16px">
        <div style="margin-bottom: 12px">支付方式</div>
        <a-radio-group v-model:value="payType">
          <a-radio value="POINTS">积分</a-radio>
          <a-radio value="ALIPAY">支付宝</a-radio>
          <a-radio value="WECHAT">微信</a-radio>
        </a-radio-group>
        <div style="margin-top: 16px">
          <a-button type="primary" :loading="loading" @click="buy">立即购买</a-button>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import MarkdownIt from 'markdown-it'
import http, { fileUrl } from '../api/http'

const md = new MarkdownIt({ html: false, linkify: true, breaks: true })
const route = useRoute()
const router = useRouter()
const product = ref(null)
const payType = ref('POINTS')
const loading = ref(false)
const html = computed(() => md.render(product.value?.contentMd || ''))
const images = computed(() => (product.value?.detailImages || '').split(',').map((item) => item.trim()).filter(Boolean))

function yuan(cent) {
  return ((cent || 0) / 100).toFixed(2)
}

async function buy() {
  if (!localStorage.getItem('archnova-shop-token')) {
    router.push(`/shop/login?redirect=/shop/${route.params.id}`)
    return
  }
  loading.value = true
  try {
    const result = await http.post('/api/shop/orders', { productId: product.value.id, payType: payType.value })
    message.success(result.tip || '订单已创建')
    router.push(result.status === 1 ? '/shop/orders?status=1' : '/shop/orders?status=0')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  product.value = await http.get(`/api/open/shop/products/${route.params.id}`)
})
</script>
