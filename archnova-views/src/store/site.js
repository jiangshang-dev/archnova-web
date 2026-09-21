import { reactive } from 'vue'
import http from '../api/http'

export const site = reactive({
  loaded: false,
  company_name: '极构科技',
  slogan: '把业务想法做成能上线的产品',
  location: '北京·通州',
  email: '',
  wechat_id: '',
  wechat_qr: '',
  github_url: 'https://github.com/jiangshang-dev',
  github_user: 'jiangshang-dev',
  phone: ''
})

export async function loadSite() {
  const data = await http.get('/api/open/site')
  Object.assign(site, data, { loaded: true })
  return site
}
