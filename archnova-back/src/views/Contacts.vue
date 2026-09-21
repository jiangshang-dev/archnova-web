<template>
  <a-card title="客户留言">
    <a-table :data-source="records" :columns="columns" row-key="id" :pagination="pagination" @change="onTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">{{ record.status === 1 ? '已读' : '未读' }}</template>
        <template v-if="column.key === 'action'">
          <a-button v-if="record.status !== 1" type="link" @click="read(record)">标为已读</a-button>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../api/http'

const records = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const columns = [
  { title: '称呼', dataIndex: 'name' },
  { title: '微信/手机', dataIndex: 'mobile' },
  { title: '邮箱', dataIndex: 'email' },
  { title: '类型', dataIndex: 'serviceType' },
  { title: '需求', dataIndex: 'content', ellipsis: true },
  { title: 'IP', dataIndex: 'clientIp' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action' }
]

async function load() {
  const data = await http.get('/api/admin/contacts', { params: { page: pagination.current, size: pagination.pageSize } })
  records.value = data.records
  pagination.total = data.total
}
function onTable(page) {
  pagination.current = page.current
  load()
}
async function read(record) {
  await http.post(`/api/admin/contacts/${record.id}/read`)
  load()
}
onMounted(load)
</script>
