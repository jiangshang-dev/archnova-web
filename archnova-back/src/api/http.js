import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '../router'

const http = axios.create({
  baseURL: import.meta.env.VUE_APP_BASE_API || '',
  timeout: 20000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('archnova-token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.code === 'number' && body.code !== 200) {
      message.error(body.message || '请求失败')
      return Promise.reject(body)
    }
    return body.data
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('archnova-token')
      router.push('/login')
    }
    message.error(error.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default http

export function fileUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path
  const server = (import.meta.env.VUE_APP_SERVER_URL || '').replace(/\/$/, '')
  if (!server) return path
  return server + (path.startsWith('/') ? path : `/${path}`)
}

export function chatSocketUrl(query) {
  if (import.meta.env.DEV) {
    const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
    return `${protocol}://${location.host}/ws/chat?${query}`
  }
  const server = (import.meta.env.VUE_APP_SERVER_URL || '').replace(/\/$/, '')
  if (server) return `${server.replace(/^http/, 'ws')}/ws/chat?${query}`
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
  return `${protocol}://${location.host}/ws/chat?${query}`
}
