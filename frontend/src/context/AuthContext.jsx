import { createContext, useContext, useMemo, useState } from 'react';
import api from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const raw = localStorage.getItem('ds_auth');
    return raw ? JSON.parse(raw) : null;
  });

  const value = useMemo(() => ({
    auth,
    user: auth,
    isAuthenticated: Boolean(auth?.accessToken),
    role: auth?.role,
    login: async (email, password) => {
      const { data } = await api.post('/auth/login', { email, password });
      localStorage.setItem('ds_auth', JSON.stringify(data.data));
      setAuth(data.data);
      return data.data;
    },
    register: async (payload) => {
      const { data } = await api.post('/auth/register', payload);
      localStorage.setItem('ds_auth', JSON.stringify(data.data));
      setAuth(data.data);
      return data.data;
    },
    logout: async () => {
      try {
        await api.post('/auth/logout', { refreshToken: auth?.refreshToken });
      } catch {
        /* ignore */
      }
      localStorage.removeItem('ds_auth');
      setAuth(null);
    },
  }), [auth]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
