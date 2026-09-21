<template>
  <main class="page">
    <div class="wrap">
      <div class="kicker">CONTACT</div>
      <h2 class="serif" style="font-size: 42px">联系我们</h2>
      <div class="contact-grid">
        <a-form layout="vertical" :model="form" @finish="submit">
          <a-form-item label="怎么称呼" required>
            <a-input v-model:value="form.name" />
          </a-form-item>
          <a-form-item label="微信或手机">
            <a-input v-model:value="form.mobile" placeholder="至少留一种联系方式" />
          </a-form-item>
          <a-form-item label="邮箱">
            <a-input v-model:value="form.email" />
          </a-form-item>
          <a-form-item label="公司或团队">
            <a-input v-model:value="form.company" />
          </a-form-item>
          <a-form-item label="想做哪一类">
            <a-select v-model:value="form.serviceType" :options="options" />
          </a-form-item>
          <a-form-item label="需求说明" required>
            <a-textarea v-model:value="form.content" :rows="5" placeholder="使用场景、希望上线的时间、有没有参考产品" />
          </a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading">提交</a-button>
        </a-form>
        <aside class="card qr-card">
          <img v-if="site.wechat_qr" :src="fileUrl(site.wechat_qr)" alt="微信二维码" />
          <div class="copy-line"><span>微信号</span><b>{{ site.wechat_id }}</b></div>
          <div class="copy-line"><span>邮箱</span><b>{{ site.email || '未配置' }}</b></div>
          <div class="copy-line"><span>GitHub</span><a :href="site.github_url" target="_blank" rel="noreferrer">{{ site.github_user }}</a></div>
          <div class="copy-line"><span>所在地</span><b>{{ site.location }}</b></div>
          <a-button style="margin-top: 12px" @click="copy">复制微信号</a-button>
        </aside>
      </div>
    </div>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import http, { fileUrl } from '../api/http'
import { site } from '../store/site'

const loading = ref(false)
const options = ['小程序', 'App', '网站', '企业系统', '还不确定'].map((item) => ({ value: item, label: item }))
const form = reactive({
  name: '',
  mobile: '',
  email: '',
  company: '',
  serviceType: '网站',
  content: ''
})

async function submit() {
  loading.value = true
  try {
    await http.post('/api/open/contact', form)
    message.success('已收到，我们会通过你留下的方式回复')
    form.name = ''
    form.mobile = ''
    form.email = ''
    form.company = ''
    form.content = ''
  } finally {
    loading.value = false
  }
}

async function copy() {
  if (!site.wechat_id) return
  await navigator.clipboard.writeText(site.wechat_id)
  message.success('微信号已复制')
}
</script>
