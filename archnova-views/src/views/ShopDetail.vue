<template>
  <main class="page" v-if="product">
    <div class="wrap">
      <div class="goods">
        <div>
          <div class="gallery-main">
            <img v-if="currentImage" :src="fileUrl(currentImage)" alt="" />
            <span v-else class="gallery-empty">暂无图片</span>
          </div>
          <div v-if="gallery.length" class="thumbs">
            <button v-for="item in gallery" :key="item" type="button" :class="{ on: item === currentImage }" @click="currentImage = item">
              <img :src="fileUrl(item)" alt="" />
            </button>
          </div>
        </div>
        <div class="buybox">
          <h1>{{ product.title }}</h1>
          <p style="color: var(--muted); margin: 0">{{ product.summary }}</p>
          <div class="price-board">
            <div class="yen">¥ {{ yuan(product.priceCent) }}</div>
            <div class="points">或 {{ product.pointsPrice }} 积分兑换</div>
          </div>
          <div>支付方式</div>
          <div class="pay-options">
            <button type="button" :class="{ on: payType === 'POINTS' }" @click="payType = 'POINTS'">积分</button>
            <button type="button" :class="{ on: payType === 'ALIPAY' }" @click="payType = 'ALIPAY'">支付宝</button>
            <button type="button" :class="{ on: payType === 'WECHAT' }" @click="payType = 'WECHAT'">微信</button>
          </div>
          <button class="buy-now" type="button" :disabled="loading" @click="buy">{{ loading ? '提交中' : '立即购买' }}</button>
          <p style="color: var(--muted); font-size: 13px">支付完成后在「我的订单」查看源码地址。支付宝、微信超过 24 小时未支付会自动取消。</p>
        </div>
      </div>

      <div class="detail-tabs"><span>图文详情</span></div>
      <div class="card markdown" v-html="html"></div>
      <div v-if="images.length" class="detail-images">
        <img v-for="item in images" :key="item" :src="fileUrl(item)" alt="" />
      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
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
const currentImage = ref('')
const html = computed(() => md.render(product.value?.contentMd || ''))
const images = computed(() => (product.value?.detailImages || '').split(',').map((item) => item.trim()).filter(Boolean))
const gallery = computed(() => [product.value?.cover, ...images.value].filter(Boolean))

watch(gallery, (list) => {
  if (!currentImage.value && list.length) currentImage.value = list[0]
})

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
  currentImage.value = gallery.value[0] || ''
})
</script>
