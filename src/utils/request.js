import axios from 'axios';
import { v4 as uuidv4 } from 'uuid';

const request = axios.create({
    baseURL: 'https://localhost:8000/api',
    timeout: 5000,
    withCredentials: true
});

let isRefreshing = false;
let requestsQueue = [];

// 请求拦截器：防重放与 Token 注入
request.interceptors.request.use(
    (config) => {
        const accessToken = localStorage.getItem('accessToken');
        if (accessToken) {
            config.headers['Authorization'] = `Bearer ${accessToken}`;
        }
        // 安全增强：防重放攻击
        config.headers['X-Timestamp'] = Date.now().toString();
        config.headers['X-Nonce'] = uuidv4();
        return config;
    },
    (error) => Promise.reject(error)
);

// 响应拦截器：双 Token 无感刷新机制
request.interceptors.response.use(
    (response) => response.data,
    async (error) => {
        const originalRequest = error.config;
        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                return new Promise((resolve) => {
                    requestsQueue.push((token) => {
                        originalRequest.headers['Authorization'] = `Bearer ${token}`;
                        resolve(request(originalRequest));
                    });
                });
            }
            originalRequest._retry = true;
            isRefreshing = true;
            try {
                // 调用刷新接口
                const res = await axios.post('https://localhost:8000/api/refresh', {}, { withCredentials: true });
                const newAccessToken = res.data.accessToken;
                localStorage.setItem('accessToken', newAccessToken);

                requestsQueue.forEach(cb => cb(newAccessToken));
                requestsQueue = [];
                originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
                return request(originalRequest);
            } catch (refreshError) {
                localStorage.clear();
                window.location.href = '/login';
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }
        return Promise.reject(error);
    }
);

export default request;