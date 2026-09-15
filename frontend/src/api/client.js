import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
});

let refreshing = null;

api.interceptors.request.use((config) => {
  const raw = localStorage.getItem('ds_auth');
  if (raw) {
    const auth = JSON.parse(raw);
    if (auth?.accessToken) {
      config.headers.Authorization = `Bearer ${auth.accessToken}`;
    }
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    const status = error.response?.status;
    if (status === 401 && !original._retry && !original.url?.includes('/auth/login')) {
      original._retry = true;
      const raw = localStorage.getItem('ds_auth');
      const auth = raw ? JSON.parse(raw) : null;
      if (!auth?.refreshToken) {
        localStorage.removeItem('ds_auth');
        window.location.href = '/login';
        return Promise.reject(error);
      }
      try {
        if (!refreshing) {
          refreshing = axios.post((import.meta.env.VITE_API_URL || '/api') + '/auth/refresh', {
            refreshToken: auth.refreshToken,
          });
        }
        const { data } = await refreshing;
        refreshing = null;
        const next = data.data;
        localStorage.setItem('ds_auth', JSON.stringify(next));
        original.headers.Authorization = `Bearer ${next.accessToken}`;
        return api(original);
      } catch (refreshError) {
        refreshing = null;
        localStorage.removeItem('ds_auth');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export default api;
