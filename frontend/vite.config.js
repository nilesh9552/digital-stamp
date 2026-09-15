import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import fs from 'fs';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 5173,

    https: {
      key: fs.readFileSync(
        path.resolve(__dirname, '../certs/digital-stamp-key.pem')
      ),
      cert: fs.readFileSync(
        path.resolve(__dirname, '../certs/digital-stamp.pem')
      ),
    },

    proxy: {
      '/api': {
        target: 'https://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});