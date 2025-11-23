import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  base: './',  
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:8020',
        changeOrigin: true,
        secure: false,
        ws: true
      }
    },
    open: true,  
    cors: true,  
    historyApiFallback: true  
  }
})
