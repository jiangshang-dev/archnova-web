<template>
  <div>
    <a-row :gutter="16">
      <a-col :span="8"><a-card title="案例">{{ data.caseCount || 0 }}</a-card></a-col>
      <a-col :span="8"><a-card title="未读留言">{{ data.unreadContact || 0 }}</a-card></a-col>
      <a-col :span="8"><a-card title="进行中的咨询">{{ data.openChat || 0 }}</a-card></a-col>
    </a-row>
    <a-card title="最近留言" style="margin-top: 16px">
      <a-table :data-source="data.recentContacts || []" :pagination="false" row-key="id" :columns="columns" />
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import http from '../api/http'

const data = reactive({})
const columns = [
  { title: '称呼', dataIndex: 'name' },
  { title: '联系方式', dataIndex: 'mobile' },
  { title: '类型', dataIndex: 'serviceType' },
  { title: 'IP', dataIndex: 'clientIp' },
  { title: '时间', dataIndex: 'createTime' }
]
onMounted(async () => Object.assign(data, await http.get('/api/admin/dashboard')))
</script>
