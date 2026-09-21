import { reactive } from 'vue'
import http from '../api/http'

export const shopUser = reactive({
  username: '',
  nickname: '',
  email: '',
  points: 0
})

export function saveShopUser(data) {
  shopUser.username = data?.username || ''
  shopUser.nickname = data?.nickname || ''
  shopUser.email = data?.email || ''
  shopUser.points = data?.points || 0
  if (data?.token) localStorage.setItem('archnova-shop-token', data.token)
}

export function clearShopUser() {
  shopUser.username = ''
  shopUser.nickname = ''
  shopUser.email = ''
  shopUser.points = 0
  localStorage.removeItem('archnova-shop-token')
}

export async function refreshShopUser() {
  if (!localStorage.getItem('archnova-shop-token')) {
    shopUser.username = ''
    shopUser.nickname = ''
    shopUser.email = ''
    shopUser.points = 0
    return null
  }
  const data = await http.get('/api/shop/me')
  saveShopUser(data)
  return data
}
