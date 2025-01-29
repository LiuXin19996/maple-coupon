import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:10000',
        changeOrigin: true,
        secure: false,
        ws: true,
        rewrite: (path) => path,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => {
            console.log('Proxy Request:', {
              method: proxyReq.method,
              path: proxyReq.path,
              headers: proxyReq.getHeaders()
            });
            
            if (proxyReq.method === 'OPTIONS') {
              proxyReq.setHeader('Access-Control-Allow-Origin', 'http://localhost:5173');
              proxyReq.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
              proxyReq.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With');
              proxyReq.setHeader('Access-Control-Allow-Credentials', 'true');
              proxyReq.end();
              return;
            }
          });

          proxy.on('proxyRes', (proxyRes, req) => {
            console.log('Proxy Response:', {
              statusCode: proxyRes.statusCode,
              method: req.method,
              path: req.url,
              headers: proxyRes.headers
            });

            proxyRes.headers['Access-Control-Allow-Origin'] = 'http://localhost:5173';
            proxyRes.headers['Access-Control-Allow-Methods'] = 'GET, POST, PUT, DELETE, OPTIONS';
            proxyRes.headers['Access-Control-Allow-Headers'] = 'Content-Type, Authorization, X-Requested-With';
            proxyRes.headers['Access-Control-Allow-Credentials'] = 'true';
          });

          proxy.on('error', (err, req, res) => {
            console.error('Proxy Error:', err);
            res.writeHead(500, {
              'Content-Type': 'application/json'
            });
            res.end(JSON.stringify({
              error: 'Proxy Error',
              message: err.message
            }));
          });
        }
      }
    }
  }
})
