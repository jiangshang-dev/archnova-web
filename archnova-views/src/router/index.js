import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Services from '../views/Services.vue'
import Cases from '../views/Cases.vue'
import CaseDetail from '../views/CaseDetail.vue'
import Github from '../views/Github.vue'
import Contact from '../views/Contact.vue'
import Shop from '../views/Shop.vue'
import ShopDetail from '../views/ShopDetail.vue'
import ShopAuth from '../views/ShopAuth.vue'
import ShopOrders from '../views/ShopOrders.vue'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior() {
    return { top: 0 }
  },
  routes: [
    { path: '/', component: Home },
    { path: '/services', component: Services },
    { path: '/cases', component: Cases },
    { path: '/cases/:id', component: CaseDetail },
    { path: '/github', component: Github },
    { path: '/contact', component: Contact },
    { path: '/shop', component: Shop },
    { path: '/shop/login', component: ShopAuth },
    { path: '/shop/orders', component: ShopOrders },
    { path: '/shop/:id', component: ShopDetail }
  ]
})

export default router

router.beforeEach((to) => {
  if (to.path === '/shop/orders' && !localStorage.getItem('archnova-shop-token')) {
    return '/shop/login?redirect=/shop/orders'
  }
})
