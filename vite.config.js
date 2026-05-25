import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  server: {
    host:'0.0.0.0',
    allowedHosts: [
      'anxiety12.fucku.top',
      'anxiety12.top',
      'vip.xa.frp.one'
    ],
    proxy: {      
        '/api': {        
          target: 'http://hn.frp.one:38654',        
          changeOrigin: true,        
          secure: false,
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
