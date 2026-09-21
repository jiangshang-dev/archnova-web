import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Services from '../views/Services.vue'
import Cases from '../views/Cases.vue'
import CaseDetail from '../views/CaseDetail.vue'
import Github from '../views/Github.vue'
import Contact from '../views/Contact.vue'

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
    { path: '/contact', component: Contact }
  ]
})

export default router
