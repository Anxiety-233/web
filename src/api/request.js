import axios from 'axios';
import { v4 as uuidv4 } from 'uuid';
import router from '../router';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  config.headers['timestamp'] = Date.now().toString();
  config.headers['nonce'] = uuidv4();
  config.headers['ngrok-skip-browser-warning'] = 'true';
  return config;
});

request.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code === 200) {
      return res.data;
    }
    return Promise.reject(new Error(res.msg || '请求失败'));
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.clear();
      router.push('/login');
    }
    return Promise.reject(error);
  }
);

export default request;
