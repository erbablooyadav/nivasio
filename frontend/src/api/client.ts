import axios, { type AxiosInstance } from 'axios';
import { useAuthStore } from '../store/auth';
import type { ApiResponse } from '../types';

const api: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_URL || '',
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true,  // Send HttpOnly cookies
});

// Attach access token to every request
api.interceptors.request.use((config) => {
    const token = useAuthStore.getState().accessToken;
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

// Auto-refresh on 401
api.interceptors.response.use(
    (res) => res,
    async (error) => {
        const original = error.config;
        if (error.response?.status === 401 && !original._retry) {
            original._retry = true;
            try {
                const { data } = await axios.post<ApiResponse<{ accessToken: string }>>(
                    '/api/v1/auth/refresh', {}, { withCredentials: true }
                );
                if (data.success && data.data.accessToken) {
                    useAuthStore.getState().setAccessToken(data.data.accessToken);
                    original.headers.Authorization = `Bearer ${data.data.accessToken}`;
                    return api(original);
                }
            } catch {
                useAuthStore.getState().logout();
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;
