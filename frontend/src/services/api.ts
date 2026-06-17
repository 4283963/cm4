import axios from 'axios';
import type { ApiResponse, VehicleStatusDTO, CargoTraceDTO, CargoInfoDTO } from '@/types';

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
};

export default api;
