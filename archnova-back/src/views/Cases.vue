<template>
  <div v-if="!editing">
    <a-card title="项目案例">
      <a-space style="margin-bottom: 12px">
        <a-input v-model:value="keyword" placeholder="搜索标题" @pressEnter="load" />
        <a-button @click="load">查询</a-button>
        <a-button type="primary" @click="open()">新增案例</a-button>
      </a-space>
      <a-table :data-source="records" :columns="columns" row-key="id" :pagination="pagination" @change="onTable">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">{{ record.status === 1 ? '上架' : '下架' }}</template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="open(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
  <a-card v-else :title="form.id ? '编辑案例' : '新增案例'">
    <template #extra><a-button @click="editing = false">返回列表</a-button></template>
    <div class="case-editor">
      <section>
        <div class="editor-label">封面</div>
        <div class="case-cover-pick" @click="pickCover">
          <img v-if="form.cover" :src="fileUrl(form.cover)" alt="" />
          <span v-else>上传封面，前台显示在标题下方</span>
        </div>
        <div class="editor-label" style="margin-top: 16px">详情图</div>
        <div class="case-thumbs">
          <div v-for="(item, index) in detailImages" :key="item" class="case-thumb">
            <img :src="fileUrl(item)" alt="" />
            <button type="button" @click="detailImages.splice(index, 1)">×</button>
          </div>
          <button class="case-add" type="button" @click="pickDetail">+</button>
        </div>
        <input ref="coverInput" type="file" accept="image/*" hidden @change="onCover" />
        <input ref="detailInput" type="file" accept="image/*" hidden @change="onDetail" />
      </section>
      <a-form layout="vertical">
        <a-form-item label="标题"><a-input v-model:value="form.title" /></a-form-item>
        <a-form-item label="摘要"><a-textarea v-model:value="form.summary" :rows="2" /></a-form-item>
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="类型">
              <a-select v-model:value="form.category" :options="categories.map((item) => ({ value: item, label: item }))" />
            </a-form-item>
          </a-col>
          <a-col :span="8"><a-form-item label="排序"><a-input-number v-model:value="form.sortNum" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="[{ value: 1, label: '上架' }, { value: 0, label: '下架' }]" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="技术标签，逗号分隔"><a-input v-model:value="form.techStack" /></a-form-item>
        <a-form-item label="演示地址"><a-input v-model:value="form.demoUrl" /></a-form-item>
        <a-form-item label="GitHub 地址"><a-input v-model:value="form.githubUrl" /></a-form-item>
      </a-form>
    </div>
    <div class="editor-label">图文详情</div>
    <p style="color: #8c8378; margin-top: 0">用 Markdown 写说明。工具栏里的图片会按正文顺序出现在前台。</p>
    <MdEditor v-model="form.content" language="zh-CN" :on-upload-img="onUploadImg" style="height: 460px" />
    <div style="margin-top: 16px">
      <a-button type="primary" :loading="saving" @click="save">保存案例</a-button>
    </div>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import http, { fileUrl } from '../api/http'

const categories = ['小程序', 'App', '网站', '企业系统', '桌面应用']
const keyword = ref('')
const records = ref([])
const editing = ref(false)
const saving = ref(false)
const form = reactive(blank())
const detailImages = ref([])
const coverInput = ref(null)
const detailInput = ref(null)
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const columns = [
  { title: '标题', dataIndex: 'title' },
  { title: '类型', dataIndex: 'category' },
  { title: '排序', dataIndex: 'sortNum' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action' }
]

function blank() {
  return { id: null, title: '', summary: '', content: '', category: '网站', techStack: '', cover: '', detailImages: '', demoUrl: '', githubUrl: '', sortNum: 0, status: 1 }
}

async function load() {
  const data = await http.get('/api/admin/cases', { params: { page: pagination.current, size: pagination.pageSize, keyword: keyword.value } })
  records.value = data.records
  pagination.total = data.total
}

function onTable(page) {
  pagination.current = page.current
  load()
}

function open(record) {
  Object.assign(form, record ? { ...record } : blank())
  detailImages.value = (form.detailImages || '').split(',').map((item) => item.trim()).filter(Boolean)
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

async function onUploadImg(files, callback) {
  const urls = []
  for (const file of files) urls.push(await uploadImage(file))
  callback(urls)
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
    form.detailImages = detailImages.value.join(',')
    await http.post('/api/admin/cases', { ...form })
    editing.value = false
    message.success('已保存')
    load()
  } finally {
    saving.value = false
  }
}

async function remove(record) {
  await http.post(`/api/admin/cases/${record.id}/delete`)
  message.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.case-editor { display: grid; grid-template-columns: 280px 1fr; gap: 28px; }
.editor-label { margin-bottom: 8px; font-weight: 600; }
.case-cover-pick {
  width: 240px;
  height: 150px;
  border: 1px dashed #d9d0c3;
  border-radius: 8px;
  display: grid;
  place-items: center;
  overflow: hidden;
  cursor: pointer;
  background: #faf7f2;
  color: #8c8378;
  text-align: center;
  padding: 8px;
}
.case-cover-pick img { width: 100%; height: 100%; object-fit: cover; }
.case-thumbs { display: flex; flex-wrap: wrap; gap: 8px; width: 240px; }
.case-thumb { position: relative; width: 72px; height: 72px; }
.case-thumb img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
.case-thumb button, .case-add { border: 1px dashed #d9d0c3; background: #fff; cursor: pointer; }
.case-thumb button { position: absolute; top: -6px; right: -6px; width: 18px; height: 18px; border-radius: 50%; line-height: 14px; }
.case-add { width: 72px; height: 72px; border-radius: 6px; font-size: 24px; }
@media (max-width: 800px) {
  .case-editor { grid-template-columns: 1fr; }
}
</style>
