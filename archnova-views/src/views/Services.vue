<template>
  <main class="page">
    <div class="wrap">
      <div class="kicker">SERVICES</div>
      <h2 class="serif" style="font-size: 42px; margin-top: 8px">可承接的业务</h2>
      <p style="max-width: 640px; color: var(--muted); line-height: 1.8">
        面向个人、创业团队和中小企业。第一版只做能上线的范围，不做一份用不上的需求清单。
      </p>
      <div class="grid-2" style="margin-top: 28px">
        <article v-for="item in services" :key="item.key" class="card">
          <div class="no">{{ item.no }}</div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary }}</p>
          <ul>
            <li v-for="point in item.points" :key="point">{{ point }}</li>
          </ul>
        </article>
      </div>
      <div class="grid-4" style="margin-top: 16px">
        <article v-for="item in steps" :key="item.no" class="card">
          <div class="no">{{ item.no }}</div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.text }}</p>
        </article>
      </div>
    </div>
  </main>
</template>

<script setup>
import { services, steps } from '../data/services'
</script>
