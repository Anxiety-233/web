import request from './request';

export function getEquipmentList(pageNum = 1, pageSize = 10) {
  return request.get('/equipment/view/list', { params: { pageNum, pageSize } });
}

export function getEquipmentById(id) {
  return request.get(`/equipment/view/${id}`);
}

export function controlEquipment(id, status) {
  return request.patch(`/equipment/control/${id}/${status}`);
}

export function getTemperature(id) {
  return request.get(`/equipment/temperature/${id}`);
}
