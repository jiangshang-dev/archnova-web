<template>
  <main>
    <section class="hero">
      <div>
        <div class="eyebrow">{{ site.location || '北京' }} · 软件开发</div>
        <h1>帮个人和团队<br />把想法做成能用的产品</h1>
        <p class="lead">{{ site.slogan }}。我们承接小程序、App、网站和企业系统，目标是让你拿到可以给客户看、可以继续改的第一版。</p>
        <div class="hero-actions">
          <router-link class="btn btn-solid" to="/contact">描述你的项目</router-link>
          <router-link class="btn btn-ghost" to="/cases">看做过的案例</router-link>
        </div>
      </div>
      <aside class="hero-card">
        <h2>现在可以做</h2>
        <div class="sub">按场景接，不按岗位堆模块</div>
        <div v-for="item in services" :key="item.key" class="offer">
          <b>{{ item.title }}</b>
          <span>{{ item.summary.slice(0, 18) }}</span>
        </div>
      </aside>
    </section>

    <section class="section">
      <div class="wrap">
        <div class="section-head">
          <div>
            <div class="kicker">SERVICES</div>
            <h2>四类可以落地的交付</h2>
          </div>
          <p>先确认你要解决的业务问题，再决定是小程序、App、网站还是内部系统。</p>
        </div>
        <div class="grid-4">
          <article v-for="item in services" :key="item.key" class="card">
            <div class="no">{{ item.no }}</div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.summary }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="section" style="padding-top: 0">
      <div class="wrap">
        <div class="section-head">
          <div>
            <div class="kicker">CASES</div>
            <h2>项目案例</h2>
          </div>
          <router-link to="/cases">全部案例</router-link>
        </div>
        <div class="grid-3">
          <router-link v-for="item in cases" :key="item.id" :to="`/cases/${item.id}`" class="card case-card">
            <div class="cat">{{ item.category }}</div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.summary }}</p>
            <div class="more">查看说明</div>
          </router-link>
        </div>
      </div>
    </section>

    <section class="band">
      <div>
        <h2>有想法，先说清楚场景</h2>
        <p>微信、邮箱或右下角在线客服都可以。</p>
      </div>
      <router-link class="btn btn-solid" to="/contact">联系我们</router-link>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'
import { services } from '../data/services'
import { site } from '../store/site'

const cases = ref([])
onMounted(async () => {
  const list = await http.get('/api/open/cases')
  cases.value = (list || []).slice(0, 3)
})
</script>
