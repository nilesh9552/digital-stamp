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
        path.resolve(__dirname, '../certs/172.16.25.138+2-key.pem')
      ),
      cert: fs.readFileSync(
        path.resolve(__dirname, '../certs/172.16.25.138+2.pem')
      ),
    },

    proxy: {
      '/api': {
        target: 'https://localhost:8080',
          secure: false,
      },
    },
  },
});