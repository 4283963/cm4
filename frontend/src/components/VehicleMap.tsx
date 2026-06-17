import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import type { VehicleStatusDTO, TracePointDTO } from '@/types';
import dayjs from 'dayjs';

const createCustomIcon = (status: string) => {
  const color = status === 'TRANSIT' ? '#1890ff' : '#52c41a';
  return L.divIcon({
    className: 'custom-vehicle-icon',
    html: `<div style="
      background: ${color};
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: bold;
      font-size: 14px;
      border: 3px solid white;
      box-shadow: 0 2px 8px rgba(0,0,0,0.3);
    ">🚚</div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 16],
    popupAnchor: [0, -16],
  });
};

const createTraceIcon = (status: string) => {
  const color = status === 'NORMAL' ? '#52c41a' : status === 'ABNORMAL' ? '#ff4d4f' : '#8c8c8c';
  return L.divIcon({
    className: 'custom-trace-icon',
    html: `<div style="
      background: ${color};
      width: 16px;
      height: 16px;
      border-radius: 50%;
      border: 2px solid white;
      box-shadow: 0 1px 4px rgba(0,0,0,0.3);
    "></div>`,
    iconSize: [16, 16],
    iconAnchor: [8, 8],
    popupAnchor: [0, -8],
  });
};

interface VehicleMapProps {
  vehicles: VehicleStatusDTO[];
  tracePoints?: TracePointDTO[];
  showTrace?: boolean;
  height?: string;
}

export default function VehicleMap({
  vehicles,
  tracePoints = [],
  showTrace = false,
  height = '500px',
}: VehicleMapProps) {
  const center: [number, number] = vehicles.length > 0
    ? [vehicles[0].currentLatitude || 39.9042, vehicles[0].currentLongitude || 116.4074]
    : [39.9042, 116.4074];

  const polylinePositions: [number, number][] = tracePoints
    .filter((p) => p.latitude && p.longitude)
    .map((p) => [p.latitude, p.longitude]);

  return (
    <MapContainer
      center={center}
      zoom={11}
      style={{ height, width: '100%', borderRadius: 8 }}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {vehicles.map((vehicle) => (
        <Marker
          key={vehicle.id}
          position={[vehicle.currentLatitude || 0, vehicle.currentLongitude || 0]}
          icon={createCustomIcon(vehicle.status)}
        >
          <Popup>
            <div className="map-popup">
              <div className="popup-title">{vehicle.plateNumber}</div>
              <div className="popup-item">
                司机：<span>{vehicle.driverName}</span>
              </div>
              <div className="popup-item">
                车型：<span>{vehicle.model}</span>
              </div>
              <div className="popup-item">
                状态：<span>{vehicle.status === 'TRANSIT' ? '运输中' : '停靠'}</span>
              </div>
              <div className="popup-item">
                温区数：<span>{vehicle.temperatureZones.length}</span>
              </div>
              <div className="popup-item">
                货品数：<span>{vehicle.cargos.length} 批</span>
              </div>
              <div className="popup-item">
                更新时间：<span>{dayjs(vehicle.lastUpdateTime).format('MM-DD HH:mm:ss')}</span>
              </div>
            </div>
          </Popup>
        </Marker>
      ))}

      {showTrace && (
        <>
          {polylinePositions.length > 1 && (
            <Polyline
              positions={polylinePositions}
              color="#1890ff"
              weight={3}
              opacity={0.7}
              dashArray="10, 10"
            />
          )}
          {tracePoints.map((point, index) => (
            <Marker
              key={index}
              position={[point.latitude || 0, point.longitude || 0]}
              icon={createTraceIcon(point.temperatureStatus)}
            >
              <Popup>
                <div className="map-popup">
                  <div className="popup-title">
                    {dayjs(point.traceTime).format('MM-DD HH:mm:ss')}
                  </div>
                  <div className="popup-item">
                    位置：<span>{point.locationName || '未知'}</span>
                  </div>
                  <div className="popup-item">
                    温度：
                    <span
                      className={
                        point.temperatureStatus === 'NORMAL'
                          ? 'popup-temp-normal'
                          : 'popup-temp-abnormal'
                      }
                    >
                      {point.temperature?.toFixed(1)}°C
                    </span>
                  </div>
                  {point.humidity && (
                    <div className="popup-item">
                      湿度：<span>{point.humidity.toFixed(1)}%</span>
                    </div>
                  )}
                  <div className="popup-item">
                    温区：<span>{point.zoneCode}</span>
                  </div>
                </div>
              </Popup>
            </Marker>
          ))}
        </>
      )}
    </MapContainer>
  );
}
