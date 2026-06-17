import { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Tag, List, Avatar, Button, Space, Modal, message } from 'antd';
import {
  CarOutlined,
  EnvironmentOutlined,
  ThermometerOutlined,
  WarningOutlined,
  ReloadOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { vehicleApi, mockApi } from '@/services/api';
import type { VehicleStatusDTO, TemperatureZoneStatusDTO } from '@/types';
import { ZONE_TYPE_CONFIG, TEMPERATURE_STATUS_CONFIG } from '@/types';
import VehicleMap from '@/components/VehicleMap';
import dayjs from 'dayjs';

export default function VehicleDashboard() {
  const [vehicles, setVehicles] = useState<VehicleStatusDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedVehicle, setSelectedVehicle] = useState<VehicleStatusDTO | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const navigate = useNavigate();

  const fetchVehicles = async () => {
    setLoading(true);
    try {
      const data = await vehicleApi.getAllVehicles();
      setVehicles(data);
    } catch (error) {
      message.error('获取车辆数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVehicles();
    const interval = setInterval(fetchVehicles, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleSendMockData = async (plateNumber: string) => {
    try {
      await mockApi.sendMockData(plateNumber);
      message.success(`${plateNumber} 模拟数据已推送`);
      fetchVehicles();
    } catch (error) {
      message.error('推送失败');
    }
  };

  const handleSendMockDataWithGpsLost = async (plateNumber: string) => {
    try {
      await mockApi.sendMockDataWithGpsLost(plateNumber);
      message.success(`${plateNumber} GPS信号丢失模拟数据已推送`);
      fetchVehicles();
    } catch (error) {
      message.error('推送失败');
    }
  };

  const showVehicleDetail = (vehicle: VehicleStatusDTO) => {
    setSelectedVehicle(vehicle);
    setModalVisible(true);
  };

  const totalVehicles = vehicles.length;
  const totalCargos = vehicles.reduce((sum, v) => sum + v.cargos.length, 0);
  const totalZones = vehicles.reduce((sum, v) => sum + v.temperatureZones.length, 0);
  const abnormalZones = vehicles.reduce(
    (sum, v) =>
      sum + v.temperatureZones.filter((z) => z.temperatureStatus === 'ABNORMAL').length,
    0
  );

  const getZoneClass = (zoneType: string) => {
    const type = zoneType as keyof typeof ZONE_TYPE_CONFIG;
    switch (type) {
      case 'FROZEN':
        return 'zone-frozen';
      case 'CHILLED':
        return 'zone-chilled';
      case 'FRESH':
        return 'zone-fresh';
      default:
        return 'zone-chilled';
    }
  };

  const getTemperatureClass = (status: string) => {
    switch (status) {
      case 'NORMAL':
        return 'temperature-normal';
      case 'ABNORMAL':
        return 'temperature-abnormal';
      default:
        return 'temperature-unknown';
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <h1 className="page-title">运输车辆状态看板</h1>
        <Space>
          <Button
            icon={<WarningOutlined />}
            danger
            onClick={async () => {
              try {
                await mockApi.sendMockDataWithGpsLostForAll();
                message.success('所有车辆 GPS 信号丢失模拟数据已推送');
                fetchVehicles();
              } catch (error) {
                message.error('推送失败');
              }
            }}
          >
            模拟全部GPS丢失
          </Button>
          <Button
            icon={<ReloadOutlined />}
            onClick={async () => {
              try {
                await mockApi.sendMockDataForAll();
                message.success('所有车辆模拟数据已推送');
                fetchVehicles();
              } catch (error) {
                message.error('推送失败');
              }
            }}
          >
            推送全部数据
          </Button>
          <Button
            type="primary"
            icon={<ReloadOutlined spin={loading} />}
            onClick={fetchVehicles}
            loading={loading}
          >
            刷新数据
          </Button>
        </Space>
      </div>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="在途车辆"
              value={totalVehicles}
              prefix={<CarOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="在运货品"
              value={totalCargos}
              suffix="批"
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="运行温区"
              value={totalZones}
              prefix={<ThermometerOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="异常温区"
              value={abnormalZones}
              prefix={<WarningOutlined />}
              valueStyle={{ color: abnormalZones > 0 ? '#ff4d4f' : '#8c8c8c' }}
            />
          </Card>
        </Col>
      </Row>

      <Card title="车辆实时位置" style={{ marginBottom: 24 }}>
        <VehicleMap vehicles={vehicles} />
      </Card>

      <Row gutter={[16, 16]}>
        {vehicles.map((vehicle) => (
          <Col xs={24} lg={12} xl={8} key={vehicle.id}>
            <Card
              className="vehicle-card"
              title={
                <Space>
                  <Avatar icon={<CarOutlined />} style={{ backgroundColor: '#1890ff' }} />
                  <span className="card-title">{vehicle.plateNumber}</span>
                  <Tag color={vehicle.status === 'TRANSIT' ? 'blue' : 'green'}>
                    {vehicle.status === 'TRANSIT' ? '运输中' : '停靠'}
                  </Tag>
                </Space>
              }
              extra={
                <Button type="link" onClick={() => showVehicleDetail(vehicle)}>
                  <EyeOutlined /> 查看详情
                </Button>
              }
              actions={[
                <Button
                  key="trace"
                  type="link"
                  onClick={() => handleSendMockData(vehicle.plateNumber)}
                >
                  <ReloadOutlined /> 推送数据
                </Button>,
                <Button
                  key="gps-lost"
                  type="link"
                  danger
                  onClick={() => handleSendMockDataWithGpsLost(vehicle.plateNumber)}
                >
                  <WarningOutlined /> 模拟GPS丢失
                </Button>,
                <Button
                  key="cargos"
                  type="link"
                  onClick={() => navigate('/traceability', { state: { plateNumber: vehicle.plateNumber } })}
                >
                  查看货品
                </Button>,
              ]}
            >
              <div style={{ marginBottom: 16 }}>
                <div style={{ display: 'flex', alignItems: 'center', color: '#8c8c8c', marginBottom: 8 }}>
                  <EnvironmentOutlined style={{ marginRight: 8 }} />
                  {vehicle.driverName} ({vehicle.driverPhone})
                </div>
                <div style={{ display: 'flex', alignItems: 'center', color: '#8c8c8c' }}>
                  <CarOutlined style={{ marginRight: 8 }} />
                  {vehicle.model} · 更新于 {dayjs(vehicle.lastUpdateTime).format('HH:mm:ss')}
                </div>
              </div>

              <List
                size="small"
                header={<div style={{ fontWeight: 500 }}>温区状态</div>}
                dataSource={vehicle.temperatureZones}
                renderItem={(zone: TemperatureZoneStatusDTO) => (
                  <List.Item
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                    }}
                  >
                    <Space>
                      <span className={`zone-badge ${getZoneClass(zone.zoneType)}`}>
                        {ZONE_TYPE_CONFIG[zone.zoneType as keyof typeof ZONE_TYPE_CONFIG]?.label ||
                          zone.zoneName}
                      </span>
                      <span style={{ color: '#595959' }}>{zone.zoneName}</span>
                    </Space>
                    <span className={getTemperatureClass(zone.temperatureStatus)}>
                      {zone.currentTemperature?.toFixed(1)}°C
                      <Tag
                        style={{ marginLeft: 8 }}
                        color={
                          TEMPERATURE_STATUS_CONFIG[zone.temperatureStatus]?.color || '#8c8c8c'
                        }
                      >
                        {TEMPERATURE_STATUS_CONFIG[zone.temperatureStatus]?.label || '未知'}
                      </Tag>
                    </span>
                  </List.Item>
                )}
              />

              <div style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid #f0f0f0' }}>
                <div style={{ color: '#8c8c8c', marginBottom: 8 }}>
                  装载货品 ({vehicle.cargos.length} 批)
                </div>
                <Space wrap>
                  {vehicle.cargos.slice(0, 3).map((cargo) => (
                    <Tag key={cargo.id} color="blue">
                      {cargo.cargoName}
                    </Tag>
                  ))}
                  {vehicle.cargos.length > 3 && (
                    <Tag>+{vehicle.cargos.length - 3} 更多</Tag>
                  )}
                </Space>
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      <Modal
        title={
          <Space>
            <CarOutlined />
            {selectedVehicle?.plateNumber} 详细信息
          </Space>
        }
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={1000}
      >
        {selectedVehicle && (
          <div>
            <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
              <Col span={12}>
                <div className="cargo-info-item">
                  <span className="label">车牌号</span>
                  <span className="value">{selectedVehicle.plateNumber}</span>
                </div>
              </Col>
              <Col span={12}>
                <div className="cargo-info-item">
                  <span className="label">司机</span>
                  <span className="value">
                    {selectedVehicle.driverName} ({selectedVehicle.driverPhone})
                  </span>
                </div>
              </Col>
              <Col span={12}>
                <div className="cargo-info-item">
                  <span className="label">车型</span>
                  <span className="value">{selectedVehicle.model}</span>
                </div>
              </Col>
              <Col span={12}>
                <div className="cargo-info-item">
                  <span className="label">状态</span>
                  <span className="value">
                    <Tag color={selectedVehicle.status === 'TRANSIT' ? 'blue' : 'green'}>
                      {selectedVehicle.status === 'TRANSIT' ? '运输中' : '停靠'}
                    </Tag>
                  </span>
                </div>
              </Col>
            </Row>

            <Card title="车辆位置" size="small" style={{ marginBottom: 16 }}>
              <VehicleMap vehicles={[selectedVehicle]} height="300px" />
            </Card>

            <Card title="温区详情" size="small">
              <List
                dataSource={selectedVehicle.temperatureZones}
                renderItem={(zone) => (
                  <List.Item>
                    <List.Item.Meta
                      title={
                        <Space>
                          <span
                            className={`zone-badge ${getZoneClass(zone.zoneType)}`}
                          >
                            {ZONE_TYPE_CONFIG[zone.zoneType as keyof typeof ZONE_TYPE_CONFIG]
                              ?.label || zone.zoneName}
                          </span>
                          {zone.zoneName}
                        </Space>
                      }
                      description={`温度范围: ${zone.minTemperature?.toFixed(1)}°C ~ ${zone.maxTemperature?.toFixed(1)}°C · 货品: ${zone.cargoCount} 批`}
                    />
                    <div className={getTemperatureClass(zone.temperatureStatus)}>
                      当前: {zone.currentTemperature?.toFixed(1)}°C
                      <Tag
                        style={{ marginLeft: 8 }}
                        color={
                          TEMPERATURE_STATUS_CONFIG[zone.temperatureStatus]?.color || '#8c8c8c'
                        }
                      >
                        {TEMPERATURE_STATUS_CONFIG[zone.temperatureStatus]?.label || '未知'}
                      </Tag>
                    </div>
                  </List.Item>
                )}
              />
            </Card>
          </div>
        )}
      </Modal>
    </div>
  );
}
