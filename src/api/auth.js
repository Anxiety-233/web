import request from './request';

export function login(username, password) {
  return request.post('/auth/login', { username, password });
}

export function logout() {
  return request.post('/auth/logout');
}

export function refreshToken(username) {
  const refreshTokenValue = localStorage.getItem('refreshToken');
  return request.post(`/auth/refresh-token?username=${username}`, null, {
    headers: { 'refresh-token': refreshTokenValue },
  });
}
