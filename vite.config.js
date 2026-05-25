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
      'https://web-lilac-zeta-79.vercel.app'
    ],
    proxy: {      
        '/api': {        
          target: 'http://vip.xa.frp.one:55040',
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
