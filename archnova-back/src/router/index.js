import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import Dashboard from '../views/Dashboard.vue'
import Config from '../views/Config.vue'
import Cases from '../views/Cases.vue'
import Contacts from '../views/Contacts.vue'
import Chat from '../views/Chat.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', component: Dashboard },
        { path: 'config', component: Config },
        { path: 'cases', component: Cases },
        { path: 'contacts', component: Contacts },
        { path: 'chat', component: Chat }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !localStorage.getItem('archnova-token')) {
    return '/login'
  }
})

export default router
