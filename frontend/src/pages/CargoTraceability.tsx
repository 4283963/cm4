import { useState, useEffect } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import {
  Row,
  Col,
  Card,
  Statistic,
  Tag,
  List,
  Button,
  Space,
  Select,
  Input,
  DatePicker,
  Timeline,
  Alert,
  Table,
  message,
  Tabs,
  Progress,
} from 'antd';
import {
  SearchOutlined,
  EnvironmentOutlined,
  ThermometerOutlined,
  WarningOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CarOutlined,
  ArrowRightOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { cargoApi, traceabilityApi, mockApi } from '@/services/api';
import type {
  CargoInfoDTO,
  CargoTraceDTO,
  TracePointDTO,
  TemperatureAlertDTO,
} from '@/types';
import { ZONE_TYPE_CONFIG, TEMPERATURE_STATUS_CONFIG } from '@/types';
import VehicleMap from '@/components/VehicleMap';
import TemperatureChart from '@/components/TemperatureChart';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;
const { TabPane } = Tabs;

export default function CargoTraceability() {
  const { batchNo: urlBatchNo } = useParams<{ batchNo: string }>();
  const location = useLocation();
  const state = location.state as { plateNumber?: string } | null;

  const [cargos, setCargos] = useState<CargoInfoDTO[]>([]);
  const [filteredCargos, setFilteredCargos] = useState<CargoInfoDTO[]>([]);
  const [selectedBatchNo, setSelectedBatchNo] = useState<string | null>(urlBatchNo || null);
  const [traceData, setTraceData] = useState<CargoTraceDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [filterPlate, setFilterPlate] = useState<string | undefined>(state?.plateNumber);

  const fetchCargos = async () => {
    try {
      const data = await cargoApi.getAllInTransitCargos();
      setCargos(data);
      setFilteredCargos(data);
    } catch (error) {
      message.error('获取货品列表失败');
    }
  };

  useEffect(() => {
    fetchCargos();
  }, []);

  useEffect(() => {
    let filtered = cargos;
    if (searchText) {
      const text = searchText.toLowerCase();
      filtered = filtered.filter(
        (c) =>
          c.batchNo.toLowerCase().includes(text) ||
          c.cargoName.toLowerCase().includes(text) ||
          c.cargoType.toLowerCase().includes(text)
      );
    }
    if (filterPlate) {
      filtered = filtered.filter((c) => c.vehiclePlate === filterPlate);
    }
    setFilteredCargos(filtered);
  }, [searchText, filterPlate, cargos]);

  useEffect(() => {
    if (selectedBatchNo) {
      fetchTraceData(selectedBatchNo);
    }
  }, [selectedBatchNo]);

  const fetchTraceData = async (batchNo: string) => {
    setLoading(true);
    try {
      const data = await traceabilityApi.getCargoTraceability(batchNo);
      setTraceData(data);
    } catch (error) {
      message.error('获取溯源数据失败');
      setTraceData(null);
    } finally {
      setLoading(false);
    }
  };

  const handleSendMockData = async (plateNumber: string) => {
    try {
      await mockApi.sendMockData(plateNumber);
      message.success('模拟数据已推送');
      if (selectedBatchNo) {
        fetchTraceData(selectedBatchNo);
      }
    } catch (error) {
      message.error('推送失败');
    }
  };

  const handleTimeRangeSearch = async (dates: any) => {
    if (!selectedBatchNo || !dates) return;
    setLoading(true);
    try {
      const startTime = dates[0].format('YYYY-MM-DD HH:mm:ss');
      const endTime = dates[1].format('YYYY-MM-DD HH:mm:ss');
      const data = await traceabilityApi.getCargoTraceabilityByTimeRange(
        selectedBatchNo,
        startTime,
        endTime
      );
      setTraceData(data);
    } catch (error) {
      message.error('获取溯源数据失败');
    } finally {
      setLoading(false);
    }
  };

  const cargoColumns: ColumnsType<CargoInfoDTO> = [
    {
      title: '批次号',
      dataIndex: 'batchNo',
      key: 'batchNo',
      render: (text) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: '货品名称',
      dataIndex: 'cargoName',
      key: 'cargoName',
    },
    {
      title: '货品类型',
      dataIndex: 'cargoType',
      key: 'cargoType',
    },
    {
      title: '运输车辆',
      dataIndex: 'vehiclePlate',
      key: 'vehiclePlate',
      render: (text) => (
        <Space>
          <CarOutlined />
          {text}
        </Space>
      ),
    },
    {
      title: '温区',
      dataIndex: 'zoneName',
      key: 'zoneName',
      render: (_, record) => {
        const zoneType = record.zoneCode?.includes('FROZEN')
          ? 'FROZEN'
          : record.zoneCode?.includes('FRESH')
          ? 'FRESH'
          : 'CHILLED';
        const config = ZONE_TYPE_CONFIG[zoneType as keyof typeof ZONE_TYPE_CONFIG];
        return (
          <span
            className="zone-badge"
            style={{
              background: config?.bgColor,
              color: config?.color,
            }}
          >
            {record.zoneName}
          </span>
        );
      },
    },
    {
      title: '运输路线',
      key: 'route',
      render: (_, record) => (
        <Space>
          <span>{record.origin}</span>
          <ArrowRightOutlined style={{ color: '#8c8c8c' }} />
          <span>{record.destination}</span>
        </Space>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button type="link" onClick={() => setSelectedBatchNo(record.batchNo)}>
          查看溯源
        </Button>
      ),
    },
  ];

  const alertColumns: ColumnsType<TemperatureAlertDTO> = [
    {
      title: '告警时间',
      dataIndex: 'alertTime',
      key: 'alertTime',
      render: (text) => dayjs(text).format('MM-DD HH:mm:ss'),
    },
    {
      title: '告警类型',
      dataIndex: 'alertType',
      key: 'alertType',
      render: (type) => (
        <Tag color={type === 'OVERHEAT' ? 'red' : 'orange'}>
          {type === 'OVERHEAT' ? '温度过高' : type === 'UNDERCOOL' ? '温度过低' : '温度异常'}
        </Tag>
      ),
    },
    {
      title: '温度',
      dataIndex: 'temperature',
      key: 'temperature',
      render: (temp) => <span className="temperature-abnormal">{temp?.toFixed(1)}°C</span>,
    },
    {
      title: '地点',
      dataIndex: 'locationName',
      key: 'locationName',
      render: (text) => (
        <Space>
          <EnvironmentOutlined />
          {text || '未知位置'}
        </Space>
      ),
    },
  ];

  const getAlertIcon = (type: string) => {
    if (type === 'OVERHEAT' || type === 'UNDERCOOL') {
      return <WarningOutlined style={{ color: '#ff4d4f' }} />;
    }
    return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
  };

  return (
    <div>
      <h1 className="page-title">货品温度+地理双重生命周期溯源</h1>

      {!selectedBatchNo ? (
        <Card>
          <div className="control-panel">
            <Input
              placeholder="搜索批次号、货品名称、类型"
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              style={{ width: 300 }}
              allowClear
            />
            <Select
              placeholder="筛选运输车辆"
              value={filterPlate}
              onChange={setFilterPlate}
              style={{ width: 200 }}
              allowClear
            >
              {[...new Set(cargos.map((c) => c.vehiclePlate))].map((plate) => (
                <Select.Option key={plate} value={plate}>
                  {plate}
                </Select.Option>
              ))}
            </Select>
            <Button
              type="primary"
              onClick={fetchCargos}
            >
              刷新列表
            </Button>
          </div>

          <Table
            columns={cargoColumns}
            dataSource={filteredCargos}
            rowKey="batchNo"
            loading={loading}
            pagination={{ pageSize: 10 }}
          />
        </Card>
      ) : (
        <div>
          <div style={{ marginBottom: 16 }}>
            <Button onClick={() => setSelectedBatchNo(null)}>← 返回货品列表</Button>
          </div>

          {traceData && (
            <>
              {traceData.tracePoints.filter(p => p.gpsLost).length > 0 && (
                <Alert
                  message={`检测到 ${traceData.tracePoints.filter(p => p.gpsLost).length} 次 GPS 信号丢失（隧道/山区）`}
                  description="系统已自动使用最后已知位置回填，温度数据完整保留，溯源链条未断裂"
                  type="warning"
                  showIcon
                  style={{ marginBottom: 16 }}
                />
              )}
              <Card title="货品基本信息" style={{ marginBottom: 16 }}>
                <Row gutter={[16, 16]}>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">批次号</span>
                      <span className="value">
                        <Tag color="blue">{traceData.cargoInfo.batchNo}</Tag>
                      </span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">货品名称</span>
                      <span className="value">{traceData.cargoInfo.cargoName}</span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">货品类型</span>
                      <span className="value">{traceData.cargoInfo.cargoType}</span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">数量</span>
                      <span className="value">
                        {traceData.cargoInfo.quantity} {traceData.cargoInfo.unit}
                      </span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">重量</span>
                      <span className="value">{traceData.cargoInfo.weight} kg</span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">要求温度范围</span>
                      <span className="value">
                        {traceData.cargoInfo.requiredMinTemp?.toFixed(1)}°C ~{' '}
                        {traceData.cargoInfo.requiredMaxTemp?.toFixed(1)}°C
                      </span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">运输车辆</span>
                      <span className="value">
                        <Space>
                          <CarOutlined />
                          {traceData.cargoInfo.vehiclePlate}
                        </Space>
                      </span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">所在温区</span>
                      <span className="value">
                        <span
                          className="zone-badge"
                          style={{
                            background:
                              ZONE_TYPE_CONFIG[
                                traceData.cargoInfo.zoneCode?.includes('FROZEN')
                                  ? 'FROZEN'
                                  : traceData.cargoInfo.zoneCode?.includes('FRESH')
                                  ? 'FRESH'
                                  : 'CHILLED'
                              ]?.bgColor || '#f0f0f0',
                            color:
                              ZONE_TYPE_CONFIG[
                                traceData.cargoInfo.zoneCode?.includes('FROZEN')
                                  ? 'FROZEN'
                                  : traceData.cargoInfo.zoneCode?.includes('FRESH')
                                  ? 'FRESH'
                                  : 'CHILLED'
                              ]?.color || '#8c8c8c',
                          }}
                        >
                          {traceData.cargoInfo.zoneName}
                        </span>
                      </span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">运输路线</span>
                      <span className="value">
                        <Space>
                          <EnvironmentOutlined />
                          {traceData.cargoInfo.origin}
                          <ArrowRightOutlined style={{ color: '#8c8c8c' }} />
                          {traceData.cargoInfo.destination}
                        </Space>
                      </span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">状态</span>
                      <span className="value">
                        <Tag color={traceData.cargoInfo.status === 'IN_TRANSIT' ? 'processing' : 'green'}>
                          {traceData.cargoInfo.status === 'IN_TRANSIT' ? '运输中' : '已送达'}
                        </Tag>
                      </span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">装车时间</span>
                      <span className="value">
                        <ClockCircleOutlined style={{ marginRight: 4 }} />
                        {dayjs(traceData.cargoInfo.loadingTime).format('YYYY-MM-DD HH:mm')}
                      </span>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div className="cargo-info-item">
                      <span className="label">预计到达</span>
                      <span className="value">
                        <ClockCircleOutlined style={{ marginRight: 4 }} />
                        {dayjs(traceData.cargoInfo.expectedArrivalTime).format('YYYY-MM-DD HH:mm')}
                      </span>
                    </div>
                  </Col>
                </Row>
              </Card>

              <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
                <Col xs={24} sm={12} md={6}>
                  <Card>
                    <Statistic
                      title="最低温度"
                      value={traceData.temperatureStats.minTemp}
                      suffix="°C"
                      valueStyle={{ color: '#1890ff' }}
                    />
                  </Card>
                </Col>
                <Col xs={24} sm={12} md={6}>
                  <Card>
                    <Statistic
                      title="最高温度"
                      value={traceData.temperatureStats.maxTemp}
                      suffix="°C"
                      valueStyle={{ color: '#722ed1' }}
                    />
                  </Card>
                </Col>
                <Col xs={24} sm={12} md={6}>
                  <Card>
                    <Statistic
                      title="平均温度"
                      value={traceData.temperatureStats.avgTemp}
                      suffix="°C"
                      valueStyle={{ color: '#52c41a' }}
                    />
                  </Card>
                </Col>
                <Col xs={24} sm={12} md={6}>
                  <Card>
                    <div className="stat-card">
                      <div className="stat-value" style={{ color: traceData.temperatureStats.abnormalRate > 0 ? '#ff4d4f' : '#52c41a' }}>
                        {traceData.temperatureStats.abnormalRate.toFixed(1)}%
                      </div>
                      <div className="stat-label">
                        温度异常率 ({traceData.temperatureStats.abnormalPoints}/{traceData.temperatureStats.totalPoints} 点)
                      </div>
                      <Progress
                        percent={traceData.temperatureStats.abnormalRate}
                        status={traceData.temperatureStats.abnormalRate > 0 ? 'exception' : 'success'}
                        showInfo={false}
                        style={{ marginTop: 8 }}
                      />
                    </div>
                  </Card>
                </Col>
                {traceData.tracePoints.filter(p => p.gpsLost).length > 0 && (
                  <Col xs={24} sm={12} md={6}>
                    <Card>
                      <div className="stat-card">
                        <div className="stat-value" style={{ color: '#fa8c16' }}>
                          {traceData.tracePoints.filter(p => p.gpsLost).length}
                        </div>
                        <div className="stat-label">
                          GPS信号丢失次数
                        </div>
                        <div style={{ fontSize: 12, color: '#8c8c8c', marginTop: 8 }}>
                          已自动回填位置，温度数据完整
                        </div>
                      </div>
                    </Card>
                  </Col>
                )}
              </Row>

              {traceData.temperatureStats.alerts.length > 0 && (
                <Alert
                  message={`发现 ${traceData.temperatureStats.alerts.length} 次温度异常记录`}
                  description="请关注以下异常时间点的温度情况，评估对货品质量的影响"
                  type="warning"
                  showIcon
                  style={{ marginBottom: 16 }}
                />
              )}

              <div className="control-panel">
                <RangePicker
                  showTime
                  format="YYYY-MM-DD HH:mm:ss"
                  placeholder={['开始时间', '结束时间']}
                  onChange={handleTimeRangeSearch}
                />
                <Button
                  onClick={() => fetchTraceData(selectedBatchNo)}
                >
                  重置时间范围
                </Button>
                <Button
                  type="primary"
                  onClick={() => handleSendMockData(traceData.cargoInfo.vehiclePlate)}
                >
                  推送新数据
                </Button>
              </div>

              <Tabs defaultActiveKey="map">
                <TabPane tab="地理+温度轨迹地图" key="map">
                  <Card title="温度-地理双重生命周期溯源图" loading={loading}>
                    <VehicleMap
                      vehicles={
                        traceData.cargoInfo.vehiclePlate
                          ? [
                              {
                                id: 0,
                                plateNumber: traceData.cargoInfo.vehiclePlate,
                                driverName: '',
                                driverPhone: '',
                                model: '',
                                status: 'TRANSIT',
                                currentLatitude:
                                  traceData.tracePoints.length > 0
                                    ? traceData.tracePoints[traceData.tracePoints.length - 1].latitude
                                    : 39.9042,
                                currentLongitude:
                                  traceData.tracePoints.length > 0
                                    ? traceData.tracePoints[traceData.tracePoints.length - 1].longitude
                                    : 116.4074,
                                lastUpdateTime: '',
                                temperatureZones: [],
                                cargos: [],
                              },
                            ]
                          : []
                      }
                      tracePoints={traceData.tracePoints}
                      showTrace={true}
                      height="500px"
                    />
                    <div style={{ marginTop: 16, display: 'flex', gap: 24, flexWrap: 'wrap' }}>
                      <Space>
                        <div
                          style={{
                            width: 12,
                            height: 12,
                            borderRadius: '50%',
                            background: '#52c41a',
                            border: '2px solid white',
                            boxShadow: '0 1px 4px rgba(0,0,0,0.3)',
                          }}
                        />
                        <span>温度正常</span>
                      </Space>
                      <Space>
                        <div
                          style={{
                            width: 12,
                            height: 12,
                            borderRadius: '50%',
                            background: '#ff4d4f',
                            border: '2px solid white',
                            boxShadow: '0 1px 4px rgba(0,0,0,0.3)',
                          }}
                        />
                        <span>温度异常</span>
                      </Space>
                      <Space>
                        <div
                          style={{
                            width: 20,
                            height: 2,
                            background: '#1890ff',
                          }}
                        />
                        <span>行驶轨迹</span>
                      </Space>
                    </div>
                  </Card>
                </TabPane>

                <TabPane tab="温度变化趋势图" key="chart">
                  <Card title="温度变化趋势" loading={loading}>
                    {traceData.tracePoints.length > 0 ? (
                      <TemperatureChart
                        tracePoints={traceData.tracePoints}
                        minTemp={traceData.cargoInfo.requiredMinTemp}
                        maxTemp={traceData.cargoInfo.requiredMaxTemp}
                      />
                    ) : (
                      <div style={{ textAlign: 'center', padding: 40, color: '#8c8c8c' }}>
                        暂无温度数据，请先推送模拟数据
                      </div>
                    )}
                  </Card>
                </TabPane>

                <TabPane tab="溯源时间轴" key="timeline">
                  <Card title="温度+地理溯源时间轴" loading={loading}>
                    {traceData.tracePoints.length > 0 ? (
                      <Timeline
                        className="trace-timeline"
                        items={[...traceData.tracePoints]
                          .reverse()
                          .slice(0, 20)
                          .map((point: TracePointDTO, index: number) => ({
                            color: point.gpsLost
                              ? 'orange'
                              : point.temperatureStatus === 'NORMAL'
                              ? 'green'
                              : 'red',
                            dot: point.gpsLost ? (
                              <EnvironmentOutlined style={{ color: '#fa8c16' }} />
                            ) : (
                              getAlertIcon(point.temperatureStatus)
                            ),
                            children: (
                              <Card size="small" style={{ marginBottom: 8 }}>
                                {point.gpsLost && (
                                  <div
                                    style={{
                                      background: '#fff7e6',
                                      padding: '6px 12px',
                                      borderRadius: 4,
                                      marginBottom: 8,
                                      fontSize: 12,
                                      color: '#fa8c16',
                                    }}
                                  >
                                    ⚠️ GPS信号丢失（隧道/山区），使用最后已知位置回填
                                  </div>
                                )}
                                <div
                                  style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                  }}
                                >
                                  <Space>
                                    <ClockCircleOutlined style={{ color: '#8c8c8c' }} />
                                    {dayjs(point.traceTime).format('YYYY-MM-DD HH:mm:ss')}
                                  </Space>
                                  <Tag
                                    color={
                                      TEMPERATURE_STATUS_CONFIG[point.temperatureStatus]
                                        ?.color || '#8c8c8c'
                                    }
                                  >
                                    {TEMPERATURE_STATUS_CONFIG[point.temperatureStatus]
                                      ?.label || '未知'}
                                  </Tag>
                                </div>
                                <Row gutter={[16, 8]} style={{ marginTop: 8 }}>
                                  <Col span={12}>
                                    <div style={{ color: '#8c8c8c', fontSize: 12 }}>
                                      <EnvironmentOutlined style={{ marginRight: 4 }} />
                                      位置
                                      {point.gpsLost && (
                                        <span style={{ color: '#fa8c16', marginLeft: 4 }}>
                                          (估算)
                                        </span>
                                      )}
                                    </div>
                                    <div style={{ fontWeight: 500 }}>
                                      {point.locationName || '未知位置'}
                                    </div>
                                    <div style={{ fontSize: 12, color: '#8c8c8c' }}>
                                      {point.latitude?.toFixed(6)}, {point.longitude?.toFixed(6)}
                                    </div>
                                  </Col>
                                  <Col span={12}>
                                    <div style={{ color: '#8c8c8c', fontSize: 12 }}>
                                      <ThermometerOutlined style={{ marginRight: 4 }} />
                                      温度
                                    </div>
                                    <div
                                      style={{
                                        fontWeight: 500,
                                        fontSize: 18,
                                        color:
                                          point.temperatureStatus === 'NORMAL'
                                            ? '#52c41a'
                                            : '#ff4d4f',
                                      }}
                                    >
                                      {point.temperature?.toFixed(1)}°C
                                    </div>
                                    {point.humidity !== null && point.humidity !== undefined && (
                                      <div style={{ fontSize: 12, color: '#8c8c8c' }}>
                                        湿度: {point.humidity.toFixed(1)}%
                                      </div>
                                    )}
                                  </Col>
                                </Row>
                              </Card>
                            ),
                          }))}
                      />
                    ) : (
                      <div style={{ textAlign: 'center', padding: 40, color: '#8c8c8c' }}>
                        暂无溯源数据，请先推送模拟数据
                      </div>
                    )}
                  </Card>
                </TabPane>

                <TabPane
                  tab={
                    <Space>
                      异常记录
                      {traceData.temperatureStats.alerts.length > 0 && (
                        <Tag color="red">{traceData.temperatureStats.alerts.length}</Tag>
                      )}
                    </Space>
                  }
                  key="alerts"
                >
                  <Card title="温度异常记录" loading={loading}>
                    {traceData.temperatureStats.alerts.length > 0 ? (
                      <Table
                        columns={alertColumns}
                        dataSource={traceData.temperatureStats.alerts}
                        rowKey="alertTime"
                        pagination={false}
                      />
                    ) : (
                      <div
                        style={{
                          textAlign: 'center',
                          padding: 40,
                          color: '#52c41a',
                        }}
                      >
                        <CheckCircleOutlined style={{ fontSize: 48, marginBottom: 16 }} />
                        <div>暂无温度异常记录，冷链状态良好</div>
                      </div>
                    )}
                  </Card>
                </TabPane>
              </Tabs>
            </>
          )}
        </div>
      )}
    </div>
  );
}
