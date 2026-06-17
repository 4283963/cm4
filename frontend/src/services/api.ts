import axios from 'axios';
import type { ApiResponse, VehicleStatusDTO, CargoTraceDTO, CargoInfoDTO, ElectronicFenceDTO, AlertDTO, AlertStats } from '@/types';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

export const vehicleApi = {
  getAllVehicles: () =>
    api.get<ApiResponse<VehicleStatusDTO[]>>('/vehicles').then((res) => res.data.data),

  getVehicleStatus: (plateNumber: string) =>
    api.get<ApiResponse<VehicleStatusDTO>>(`/vehicles/${plateNumber}`).then((res) => res.data.data),
};

export const traceabilityApi = {
  getCargoTraceability: (batchNo: string) =>
    api.get<ApiResponse<CargoTraceDTO>>(`/traceability/cargo/${batchNo}`).then((res) => res.data.data),

  getCargoTraceabilityByRange: (batchNo: string, startTime: string, endTime: string) =>
    api
      .get<ApiResponse<CargoTraceDTO>>(`/traceability/cargo/${batchNo}/range`, {
        params: { startTime, endTime },
      })
      .then((res) => res.data.data),
};

export const cargoApi = {
  getAllInTransitCargos: () =>
    api.get<ApiResponse<CargoInfoDTO[]>>('/cargos').then((res) => res.data.data),
};

export const mockApi = {
  initMockData: () => api.post<ApiResponse<unknown>>('/mock/init'),

  generateMockData: (plateNumber: string) =>
    api.get<ApiResponse<unknown>>(`/mock/generate/${plateNumber}`).then((res) => res.data.data),

  sendMockData: (plateNumber: string) =>
    api.post<ApiResponse<unknown>>(`/mock/send/${plateNumber}`).then((res) => res.data.data),

  sendMockDataForAll: () =>
    api.post<ApiResponse<unknown>>('/mock/send-all').then((res) => res.data.data),

  sendMockDataWithGpsLost: (plateNumber: string) =>
    api.post<ApiResponse<unknown>>(`/mock/send-gps-lost/${plateNumber}`).then((res) => res.data.data),

  sendMockDataWithGpsLostForAll: () =>
    api.post<ApiResponse<unknown>>('/mock/send-all-gps-lost').then((res) => res.data.data),

  sendMockDataWithHighTemp: (plateNumber: string) =>
    api.post<ApiResponse<unknown>>(`/mock/send-high-temp/${plateNumber}`).then((res) => res.data.data),

  sendMockDataWithHighTempForAll: () =>
    api.post<ApiResponse<unknown>>('/mock/send-all-high-temp').then((res) => res.data.data),
};

export const fenceApi = {
  getAllFences: (activeOnly?: boolean) =>
    api.get<ApiResponse<ElectronicFenceDTO[]>>('/api/fences', { params: { activeOnly } }).then((res) => res.data.data),

  getFenceById: (id: number) =>
    api.get<ApiResponse<ElectronicFenceDTO>>(`/api/fences/${id}`).then((res) => res.data.data),

  createFence: (data: Partial<ElectronicFenceDTO>) =>
    api.post<ApiResponse<ElectronicFenceDTO>>('/api/fences', data).then((res) => res.data.data),

  updateFence: (id: number, data: Partial<ElectronicFenceDTO>) =>
    api.put<ApiResponse<ElectronicFenceDTO>>(`/api/fences/${id}`, data).then((res) => res.data.data),

  deleteFence: (id: number) =>
    api.delete<ApiResponse<unknown>>(`/api/fences/${id}`).then((res) => res.data.data),
};

export const alertApi = {
  getAllAlerts: (status?: string) =>
    api.get<ApiResponse<AlertDTO[]>>('/api/alerts', { params: { status } }).then((res) => res.data.data),

  getPendingAlerts: () =>
    api.get<ApiResponse<AlertDTO[]>>('/api/alerts/pending').then((res) => res.data.data),

  getAlertStats: () =>
    api.get<ApiResponse<AlertStats>>('/api/alerts/stats').then((res) => res.data.data),

  getAlertById: (id: number) =>
    api.get<ApiResponse<AlertDTO>>(`/api/alerts/${id}`).then((res) => res.data.data),

  getAlertsForCargo: (cargoBatchId: number) =>
    api.get<ApiResponse<AlertDTO[]>>(`/api/alerts/cargo/${cargoBatchId}`).then((res) => res.data.data),

  acknowledgeAlert: (id: number, acknowledgedBy?: string) =>
    api.post<ApiResponse<AlertDTO>>(`/api/alerts/${id}/acknowledge`, { acknowledgedBy }).then((res) => res.data.data),

  resolveAlert: (id: number, acknowledgedBy?: string) =>
    api.post<ApiResponse<AlertDTO>>(`/api/alerts/${id}/resolve`, { acknowledgedBy }).then((res) => res.data.data),
};

export default api;
