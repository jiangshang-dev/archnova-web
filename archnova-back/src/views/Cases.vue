<template>
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
    <a-modal v-model:open="visible" title="案例" width="720px" @ok="save">
      <a-form layout="vertical">
        <a-form-item label="标题"><a-input v-model:value="form.title" /></a-form-item>
        <a-form-item label="摘要"><a-textarea v-model:value="form.summary" :rows="2" /></a-form-item>
        <a-form-item label="说明"><a-textarea v-model:value="form.content" :rows="5" /></a-form-item>
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
        <a-form-item label="封面">
          <img v-if="form.cover" :src="fileUrl(form.cover)" alt="" style="width: 160px; margin-bottom: 8px; border-radius: 8px" />
          <a-upload :show-upload-list="false" accept="image/*" :custom-request="uploadCover">
            <a-button>上传封面</a-button>
          </a-upload>
        </a-form-item>
        <a-form-item label="详情图片">
          <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 8px">
            <img v-for="(item, index) in detailImages" :key="item" :src="fileUrl(item)" alt="" style="width: 88px; height: 88px; object-fit: cover; border-radius: 8px; cursor: pointer" @click="detailImages.splice(index, 1)" />
          </div>
          <a-upload :show-upload-list="false" accept="image/*" :custom-request="uploadDetail">
            <a-button>添加详情图</a-button>
          </a-upload>
        </a-form-item>
        <a-form-item label="技术标签，逗号分隔"><a-input v-model:value="form.techStack" /></a-form-item>
        <a-form-item label="演示地址"><a-input v-model:value="form.demoUrl" /></a-form-item>
        <a-form-item label="GitHub 地址"><a-input v-model:value="form.githubUrl" /></a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import http, { fileUrl } from '../api/http'

const categories = ['小程序', 'App', '网站', '企业系统', '桌面应用']
const keyword = ref('')
const records = ref([])
const visible = ref(false)
const form = reactive(blank())
const detailImages = ref([])
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
  visible.value = true
}

async function uploadCover(options) {
  form.cover = await uploadImage(options.file)
  options.onSuccess?.({})
}

async function uploadDetail(options) {
  detailImages.value.push(await uploadImage(options.file))
  options.onSuccess?.({})
}

async function uploadImage(file) {
  const data = new FormData()
  data.append('file', file)
  const result = await http.post('/api/admin/oss/upload', data)
  return result.url
}

async function save() {
  form.detailImages = detailImages.value.join(',')
  await http.post('/api/admin/cases', form)
  visible.value = false
  message.success('已保存')
  load()
}

async function remove(record) {
  await http.post(`/api/admin/cases/${record.id}/delete`)
  message.success('已删除')
  load()
}

onMounted(load)
</script>
