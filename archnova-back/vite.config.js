import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VUE_APP_')
  const target = env.VUE_APP_SERVER_URL || 'http://127.0.0.1:8088'
  return {
    envPrefix: 'VUE_APP_',
    plugins: [vue()],
    server: {
      port: 5174,
      proxy: {
        '/dev-api': {
          target,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/dev-api/, '')
        },
        '/ws': {
          target: target.replace(/^http/, 'ws'),
          ws: true,
          changeOrigin: true
        }
      }
    }
  }
})
