<template>
  <a-card title="开源项目">
    <a-space style="margin-bottom: 12px">
      <a-input v-model:value="keyword" placeholder="搜索仓库名或简介" @pressEnter="load" />
      <a-select v-model:value="status" allow-clear placeholder="全部状态" style="width: 120px" :options="[{ value: 1, label: '展示中' }, { value: 0, label: '已隐藏' }]" @change="load" />
      <a-button @click="load">查询</a-button>
      <a-button type="primary" :loading="syncing" @click="sync">从 GitHub 同步</a-button>
      <a-button @click="open()">手动添加</a-button>
    </a-space>
    <a-table :data-source="records" :columns="columns" row-key="id" :pagination="pagination" @change="onTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-switch :checked="record.status === 1" checked-children="展示" un-checked-children="隐藏" @change="(checked) => toggle(record, checked)" />
        </template>
        <template v-if="column.key === 'action'">
          <a-button type="link" @click="open(record)">编辑</a-button>
        </template>
      </template>
    </a-table>
    <a-modal v-model:open="visible" title="开源项目" width="640px" @ok="save">
      <a-form layout="vertical">
        <a-form-item label="仓库名"><a-input v-model:value="form.name" /></a-form-item>
        <a-form-item label="简介"><a-textarea v-model:value="form.description" :rows="3" /></a-form-item>
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="语言"><a-input v-model:value="form.language" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="排序"><a-input-number v-model:value="form.sortNum" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8">
            <a-form-item label="前台展示">
              <a-select v-model:value="form.status" :options="[{ value: 1, label: '展示' }, { value: 0, label: '隐藏' }]" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="GitHub 地址"><a-input v-model:value="form.htmlUrl" /></a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import http from '../api/http'

const keyword = ref('')
const status = ref()
const records = ref([])
const visible = ref(false)
const syncing = ref(false)
const form = reactive(blank())
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const columns = [
  { title: '仓库', dataIndex: 'name' },
  { title: '简介', dataIndex: 'description', ellipsis: true },
  { title: '语言', dataIndex: 'language', width: 100 },
  { title: '排序', dataIndex: 'sortNum', width: 80 },
  { title: '前台', key: 'status', width: 110 },
  { title: '操作', key: 'action', width: 90 }
]

function blank() {
  return { id: null, name: '', description: '', language: '', htmlUrl: '', sortNum: 0, status: 1 }
}

async function load(allowSync = true) {
  const data = await http.get('/api/admin/repos', {
    params: { page: pagination.current, size: pagination.pageSize, keyword: keyword.value, status: status.value }
  })
  records.value = data.records
  pagination.total = data.total
  if (allowSync && !keyword.value && (status.value === undefined || status.value === null) && pagination.current === 1 && data.total === 0) {
    await sync()
  }
}

function onTable(page) {
  pagination.current = page.current
  load()
}

function open(record) {
  Object.assign(form, record ? { ...record } : blank())
  visible.value = true
}

async function save() {
  await http.post('/api/admin/repos', form)
  message.success('已保存')
  visible.value = false
  load()
}

async function toggle(record, checked) {
  await http.post('/api/admin/repos', { ...record, status: checked ? 1 : 0 })
  message.success(checked ? '已在前台展示' : '已从前台隐藏')
  load()
}

async function sync() {
  syncing.value = true
  try {
    const result = await http.post('/api/admin/repos/sync')
    const tip = result.showNew
      ? `已导入 ${result.added} 个，都会在前台展示。不想要的，把开关拨到隐藏。`
      : `同步完成，新增 ${result.added} 个（默认隐藏），更新 ${result.updated} 个。已经隐藏的不会被重新打开。`
    message.success(tip)
    await load(false)
  } finally {
    syncing.value = false
  }
}

onMounted(load)
</script>
