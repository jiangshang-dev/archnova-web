<template>
  <a-card title="联系方式">
    <a-form layout="vertical" :model="form" @finish="save">
      <a-row :gutter="16">
        <a-col :span="12"><a-form-item label="站点名称"><a-input v-model:value="form.company_name" /></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="所在地"><a-input v-model:value="form.location" /></a-form-item></a-col>
        <a-col :span="24"><a-form-item label="一句话介绍"><a-input v-model:value="form.slogan" /></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="邮箱"><a-input v-model:value="form.email" /></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="电话"><a-input v-model:value="form.phone" /></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="微信号"><a-input v-model:value="form.wechat_id" /></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="GitHub 用户名"><a-input v-model:value="form.github_user" /></a-form-item></a-col>
        <a-col :span="24"><a-form-item label="GitHub 地址"><a-input v-model:value="form.github_url" /></a-form-item></a-col>
        <a-col :span="24">
          <a-form-item label="微信二维码">
            <img v-if="form.wechat_qr" :src="fileUrl(form.wechat_qr)" alt="" style="width: 160px; margin-bottom: 8px" />
            <a-upload :show-upload-list="false" :custom-request="upload" accept="image/*">
              <a-button>更换二维码</a-button>
            </a-upload>
          </a-form-item>
        </a-col>
      </a-row>
      <a-button type="primary" html-type="submit" :loading="loading">保存</a-button>
    </a-form>
    <a-divider />
    <h3>修改密码</h3>
    <a-form layout="inline" :model="password" @finish="changePassword">
      <a-form-item label="原密码"><a-input-password v-model:value="password.oldPassword" /></a-form-item>
      <a-form-item label="新密码"><a-input-password v-model:value="password.newPassword" /></a-form-item>
      <a-button html-type="submit">更新密码</a-button>
    </a-form>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import http, { fileUrl } from '../api/http'

const loading = ref(false)
const form = reactive({
  company_name: '', slogan: '', location: '', email: '', phone: '',
  wechat_id: '', wechat_qr: '', github_url: '', github_user: ''
})
const password = reactive({ oldPassword: '', newPassword: '' })

onMounted(async () => Object.assign(form, await http.get('/api/admin/config')))

async function save() {
  loading.value = true
  try {
    await http.put('/api/admin/config', form)
    message.success('已保存')
  } finally {
    loading.value = false
  }
}

async function upload(options) {
  const data = new FormData()
  data.append('file', options.file)
  const result = await http.post('/api/admin/oss/upload', data)
  form.wechat_qr = result.url
  options.onSuccess?.(result)
  message.success('二维码已上传，记得保存')
}

async function changePassword() {
  await http.put('/api/admin/password', password)
  password.oldPassword = ''
  password.newPassword = ''
  message.success('密码已更新')
}
</script>
