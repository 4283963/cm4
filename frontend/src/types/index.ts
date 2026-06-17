export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface VehicleStatusDTO {
  id: number;
  plateNumber: string;
  driverName: string;
  driverPhone: string;
  model: string;
  status: string;
  currentLatitude: number;
  currentLongitude: number;
  lastUpdateTime: string;
  temperatureZones: TemperatureZoneStatusDTO[];
  cargos: CargoSummaryDTO[];
}

export interface TemperatureZoneStatusDTO {
  id: number;
  zoneCode: string;
  zoneName: string;
  zoneType: string;
  minTemperature: number;
  maxTemperature: number;
  currentTemperature: number;
  temperatureStatus: string;
  cargoCount: number;
}

export interface CargoSummaryDTO {
  id: number;
  batchNo: string;
  cargoName: string;
  cargoType: string;
  quantity: number;
  unit: string;
  status: string;
  zoneCode: string;
  loadingTime: string;
  expectedArrivalTime: string;
}

export interface CargoTraceDTO {
  cargoInfo: CargoInfoDTO;
  tracePoints: TracePointDTO[];
  temperatureStats: TemperatureStatsDTO;
}

export interface CargoInfoDTO {
  id: number;
  batchNo: string;
  cargoName: string;
  cargoType: string;
  origin: string;
  destination: string;
  quantity: number;
  unit: string;
  weight: number;
  requiredMinTemp: number;
  requiredMaxTemp: number;
  status: string;
  loadingTime: string;
  expectedArrivalTime: string;
  vehiclePlate: string;
  zoneCode: string;
  zoneName: string;
}

export interface TracePointDTO {
  traceTime: string;
  latitude: number;
  longitude: number;
  locationName: string;
  temperature: number;
  humidity: number;
  temperatureStatus: string;
  zoneCode: string;
  vehiclePlate: string;
  remark: string;
}

export interface TemperatureStatsDTO {
  minTemp: number;
  maxTemp: number;
  avgTemp: number;
  totalPoints: number;
  abnormalPoints: number;
  abnormalRate: number;
  alerts: TemperatureAlertDTO[];
}

export interface TemperatureAlertDTO {
  alertTime: string;
  temperature: number;
  locationName: string;
  latitude: number;
  longitude: number;
  alertType: string;
}

export type ZoneType = 'FROZEN' | 'CHILLED' | 'FRESH';

export const ZONE_TYPE_CONFIG: Record<ZoneType, { label: string; color: string; bgColor: string }> = {
  FROZEN: { label: '冷冻区', color: '#1677ff', bgColor: '#e6f4ff' },
  CHILLED: { label: '冷藏区', color: '#52c41a', bgColor: '#f6ffed' },
  FRESH: { label: '保鲜区', color: '#fa8c16', bgColor: '#fff7e6' },
};

export const TEMPERATURE_STATUS_CONFIG: Record<string, { label: string; color: string }> = {
  NORMAL: { label: '正常', color: '#52c41a' },
  ABNORMAL: { label: '异常', color: '#ff4d4f' },
  UNKNOWN: { label: '未知', color: '#8c8c8c' },
};
