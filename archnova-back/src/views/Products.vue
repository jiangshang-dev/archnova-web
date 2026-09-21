<template>
  <a-card title="源码商品">
    <a-button type="primary" style="margin-bottom: 12px" @click="open()">新增商品</a-button>
    <a-table :data-source="records" :columns="columns" row-key="id" :pagination="pagination" @change="onTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'price'">¥ {{ (record.priceCent / 100).toFixed(2) }} / {{ record.pointsPrice }} 积分</template>
        <template v-if="column.key === 'status'">{{ record.status === 1 ? '上架' : '下架' }}</template>
        <template v-if="column.key === 'action'">
          <a-button type="link" @click="open(record)">编辑</a-button>
          <a-button type="link" danger @click="remove(record)">删除</a-button>
        </template>
      </template>
    </a-table>
    <a-modal v-model:open="visible" title="商品" width="960px" @ok="save">
      <a-form layout="vertical">
        <a-form-item label="标题"><a-input v-model:value="form.title" /></a-form-item>
        <a-form-item label="摘要"><a-textarea v-model:value="form.summary" :rows="2" /></a-form-item>
        <a-form-item label="详情（Markdown）">
          <MdEditor v-model="form.contentMd" language="zh-CN" style="height: 360px" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="人民币（元）"><a-input-number v-model:value="form.priceYuan" :min="0" :step="1" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="积分价格"><a-input-number v-model:value="form.pointsPrice" :min="0" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="[{ value: 1, label: '上架' }, { value: 0, label: '下架' }]" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="封面">
          <img v-if="form.cover" :src="fileUrl(form.cover)" alt="" style="width: 140px; margin-bottom: 8px" />
          <a-upload :show-upload-list="false" accept="image/*" :custom-request="uploadCover"><a-button>上传封面</a-button></a-upload>
        </a-form-item>
        <a-form-item label="详情图片，点击缩略图可删除">
          <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 8px">
            <img v-for="(item, index) in detailImages" :key="item" :src="fileUrl(item)" alt="" style="width: 72px; height: 72px; object-fit: cover" @click="detailImages.splice(index, 1)" />
          </div>
          <a-upload :show-upload-list="false" accept="image/*" :custom-request="uploadDetail"><a-button>添加详情图</a-button></a-upload>
        </a-form-item>
        <a-form-item label="支付完成后的源码地址"><a-input v-model:value="form.sourceUrl" /></a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import http, { fileUrl } from '../api/http'

const records = ref([])
const visible = ref(false)
const detailImages = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const form = reactive(blank())
const columns = [
  { title: '标题', dataIndex: 'title' },
  { title: '价格', key: 'price' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action' }
]

function blank() {
  return { id: null, title: '', summary: '', contentMd: '', priceYuan: 0, pointsPrice: 0, cover: '', sourceUrl: '', status: 1, sortNum: 0 }
}

async function load() {
  const data = await http.get('/api/admin/shop/products', { params: { page: pagination.current, size: pagination.pageSize } })
  records.value = data.records
  pagination.total = data.total
}

function onTable(page) {
  pagination.current = page.current
  load()
}

function open(record) {
  Object.assign(form, record ? { ...record, priceYuan: (record.priceCent || 0) / 100 } : blank())
  detailImages.value = (record?.detailImages || '').split(',').map((item) => item.trim()).filter(Boolean)
  visible.value = true
}

async function uploadImage(file) {
  const data = new FormData()
  data.append('file', file)
  const result = await http.post('/api/admin/oss/upload', data)
  return result.url
}

async function uploadCover(options) {
  form.cover = await uploadImage(options.file)
  options.onSuccess?.({})
}

async function uploadDetail(options) {
  detailImages.value.push(await uploadImage(options.file))
  options.onSuccess?.({})
}

async function save() {
  await http.post('/api/admin/shop/products', {
    ...form,
    priceCent: Math.round(Number(form.priceYuan || 0) * 100),
    detailImages: detailImages.value.join(',')
  })
  visible.value = false
  message.success('已保存')
  load()
}

async function remove(record) {
  await http.post(`/api/admin/shop/products/${record.id}/delete`)
  message.success('已删除')
  load()
}

onMounted(load)
</script>
