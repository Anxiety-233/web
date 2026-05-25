import request from './request';
import { v4 as uuidv4 } from 'uuid';

export function getLogList(pageNum = 1, pageSize = 10, search = '') {
  return request.get('/log/list', { params: { pageNum, pageSize, search } });
}

export function getLogStats() {
  return request.get('/log/stats');
}

export function exportLogCSV() {
  const token = localStorage.getItem('accessToken');
  const timestamp = Date.now().toString();
  const nonce = uuidv4();

  fetch('/api/log/export', {
    headers: {
      'Authorization': `Bearer ${token}`,
      'timestamp': timestamp,
      'nonce': nonce,
      'ngrok-skip-browser-warning': 'true'
    }
  })
    .then(res => res.blob())
    .then(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'operation_logs.csv';
      a.click();
      window.URL.revokeObjectURL(url);
    });
}
