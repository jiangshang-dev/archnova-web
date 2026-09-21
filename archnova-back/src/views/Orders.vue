<template>
  <div>
    <a-card title="商城订单">
      <a-radio-group v-model:value="status" style="margin-bottom: 12px" @change="load">
        <a-radio-button :value="null">全部</a-radio-button>
        <a-radio-button :value="0">待支付</a-radio-button>
        <a-radio-button :value="1">已完成</a-radio-button>
        <a-radio-button :value="2">已取消</a-radio-button>
      </a-radio-group>
      <a-table :data-source="records" :columns="columns" row-key="id" :pagination="pagination" @change="onTable">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'pay'">{{ payText(record.payType) }}</template>
          <template v-if="column.key === 'amount'">
            <span v-if="record.payType === 'POINTS'">{{ record.pointsCost }} 积分</span>
            <span v-else>¥ {{ (record.amountCent / 100).toFixed(2) }}</span>
          </template>
          <template v-if="column.key === 'status'">{{ ['待支付', '已完成', '已取消'][record.status] }}</template>
        </template>
      </a-table>
    </a-card>
    <a-card title="买家积分" style="margin-top: 16px">
      <a-table :data-source="customers" :columns="customerColumns" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'points'">
            <a-input-number v-model:value="record.points" :min="0" />
            <a-button type="link" @click="savePoints(record)">保存</a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import http from '../api/http'

const status = ref(null)
const records = ref([])
const customers = ref([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const columns = [
  { title: '订单号', dataIndex: 'orderNo' },
  { title: '商品', dataIndex: 'productTitle' },
  { title: '支付方式', key: 'pay' },
  { title: '金额', key: 'amount' },
  { title: '状态', key: 'status' },
  { title: '时间', dataIndex: 'createTime' }
]
const customerColumns = [
  { title: '账号', dataIndex: 'username' },
  { title: '昵称', dataIndex: 'nickname' },
  { title: '积分', key: 'points' }
]

function payText(value) {
  return { POINTS: '积分', ALIPAY: '支付宝', WECHAT: '微信' }[value] || value
}

async function load() {
  const params = { page: pagination.current, size: pagination.pageSize }
  if (status.value !== null) params.status = status.value
  const data = await http.get('/api/admin/shop/orders', { params })
  records.value = data.records
  pagination.total = data.total
}

function onTable(page) {
  pagination.current = page.current
  load()
}

async function loadCustomers() {
  const data = await http.get('/api/admin/shop/customers')
  customers.value = data.records || []
}

async function savePoints(record) {
  await http.post(`/api/admin/shop/customers/${record.id}/points`, { points: record.points })
  message.success('积分已更新')
}

onMounted(() => {
  load()
  loadCustomers()
})
</script>
