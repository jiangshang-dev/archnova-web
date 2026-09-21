<template>
  <div v-if="!editing">
    <a-card title="源码商品">
      <a-button type="primary" style="margin-bottom: 12px" @click="open()">发布商品</a-button>
      <a-table :data-source="records" :columns="columns" row-key="id" :pagination="pagination" @change="onTable">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'cover'">
            <img v-if="record.cover" :src="fileUrl(record.cover)" alt="" style="width: 56px; height: 56px; object-fit: cover; border-radius: 6px" />
          </template>
          <template v-if="column.key === 'price'">¥ {{ (record.priceCent / 100).toFixed(2) }} / {{ record.pointsPrice }} 积分</template>
          <template v-if="column.key === 'status'">{{ record.status === 1 ? '上架' : '下架' }}</template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="open(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
  <a-card v-else :title="form.id ? '编辑商品' : '发布商品'">
    <template #extra><a-button @click="editing = false">返回列表</a-button></template>
    <div class="editor">
      <section class="editor-media">
        <div class="editor-label">商品图片</div>
        <div class="editor-cover" @click="pickCover">
          <img v-if="form.cover" :src="fileUrl(form.cover)" alt="" />
          <span v-else>上传主图</span>
        </div>
        <div class="editor-thumbs">
          <div v-for="(item, index) in detailImages" :key="item" class="editor-thumb">
            <img :src="fileUrl(item)" alt="" />
            <button type="button" @click="detailImages.splice(index, 1)">×</button>
          </div>
          <button class="editor-add" type="button" @click="pickDetail">+</button>
        </div>
        <input ref="coverInput" type="file" accept="image/*" hidden @change="onCover" />
        <input ref="detailInput" type="file" accept="image/*" hidden @change="onDetail" />
      </section>
      <section>
        <a-form layout="vertical">
          <a-form-item label="商品标题"><a-input v-model:value="form.title" /></a-form-item>
          <a-form-item label="卖点摘要"><a-textarea v-model:value="form.summary" :rows="2" /></a-form-item>
          <a-row :gutter="12">
            <a-col :span="8"><a-form-item label="售价（元）"><a-input-number v-model:value="form.priceYuan" :min="0" :step="1" style="width: 100%" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="积分价"><a-input-number v-model:value="form.pointsPrice" :min="0" style="width: 100%" /></a-form-item></a-col>
            <a-col :span="8">
              <a-form-item label="上架状态">
                <a-select v-model:value="form.status" :options="[{ value: 1, label: '上架' }, { value: 0, label: '下架' }]" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-form-item label="支付完成后的源码地址"><a-input v-model:value="form.sourceUrl" /></a-form-item>
        </a-form>
      </section>
    </div>
    <div class="editor-label" style="margin-top: 8px">图文详情</div>
    <MdEditor v-model="form.contentMd" language="zh-CN" style="height: 420px" />
    <div style="margin-top: 16px">
      <a-button type="primary" :loading="saving" @click="save">保存商品</a-button>
    </div>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import http, { fileUrl } from '../api/http'

const records = ref([])
const editing = ref(false)
const saving = ref(false)
const detailImages = ref([])
const coverInput = ref(null)
const detailInput = ref(null)
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const form = reactive(blank())
const columns = [
  { title: '主图', key: 'cover', width: 80 },
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
  editing.value = true
}

function pickCover() {
  coverInput.value?.click()
}
function pickDetail() {
  detailInput.value?.click()
}

async function onCover(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  form.cover = await uploadImage(file)
}

async function onDetail(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  detailImages.value.push(await uploadImage(file))
}

async function uploadImage(file) {
  const data = new FormData()
  data.append('file', file)
  const result = await http.post('/api/admin/oss/upload', data)
  return result.url
}

async function save() {
  saving.value = true
  try {
    await http.post('/api/admin/shop/products', {
      ...form,
      priceCent: Math.round(Number(form.priceYuan || 0) * 100),
      detailImages: detailImages.value.join(',')
    })
    message.success('已保存')
    editing.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(record) {
  await http.post(`/api/admin/shop/products/${record.id}/delete`)
  message.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.editor { display: grid; grid-template-columns: 280px 1fr; gap: 28px; }
.editor-label { margin-bottom: 8px; font-weight: 600; }
.editor-cover {
  width: 240px;
  height: 240px;
  border: 1px dashed #d9d0c3;
  border-radius: 8px;
  display: grid;
  place-items: center;
  overflow: hidden;
  cursor: pointer;
  background: #faf7f2;
  color: #8c8378;
}
.editor-cover img { width: 100%; height: 100%; object-fit: cover; }
.editor-thumbs { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px; width: 240px; }
.editor-thumb { position: relative; width: 72px; height: 72px; }
.editor-thumb img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
.editor-thumb button, .editor-add {
  border: 1px dashed #d9d0c3;
  background: #fff;
  cursor: pointer;
}
.editor-thumb button {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  line-height: 14px;
}
.editor-add { width: 72px; height: 72px; border-radius: 6px; font-size: 24px; }
@media (max-width: 800px) {
  .editor { grid-template-columns: 1fr; }
}
</style>
