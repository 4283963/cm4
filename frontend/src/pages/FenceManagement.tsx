import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Modal,
  Form,
  Input,
  InputNumber,
  Switch,
  Space,
  message,
  Popconfirm,
  Tag,
  Typography,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  EnvironmentOutlined,
} from '@ant-design/icons';
import { MapContainer, TileLayer, Polygon, Marker, Popup, useMapEvents } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import type { ElectronicFenceDTO } from '@/types';
import { fenceApi } from '@/services/api';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const BEIJING_CENTER: [number, number] = [39.9042, 116.4074];

const MapClickHandler: React.FC<{
  onMapClick: (lat: number, lng: number) => void;
  drawing: boolean;
}> = ({ onMapClick, drawing }) => {
  useMapEvents({
    click: (e) => {
      if (drawing) {
        onMapClick(e.latlng.lat, e.latlng.lng);
      }
    },
  });
  return null;
};

const FenceManagement: React.FC = () => {
  const [fences, setFences] = useState<ElectronicFenceDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingFence, setEditingFence] = useState<ElectronicFenceDTO | null>(null);
  const [drawingMode, setDrawingMode] = useState(false);
  const [drawingPoints, setDrawingPoints] = useState<number[][]>([]);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchFences();
  }, []);

  const fetchFences = async () => {
    setLoading(true);
    try {
      const data = await fenceApi.getAllFences();
      setFences(data);
    } catch (error) {
      message.error('加载围栏列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingFence(null);
    setDrawingPoints([]);
    setDrawingMode(true);
    form.resetFields();
    form.setFieldsValue({
      enabled: true,
      maxTemperature: 4,
    });
    setModalVisible(true);
  };

  const handleEdit = (fence: ElectronicFenceDTO) => {
    setEditingFence(fence);
    setDrawingPoints(fence.coordinates || []);
    setDrawingMode(true);
    form.setFieldsValue({
      fenceName: fence.fenceName,
      description: fence.description,
      maxTemperature: fence.maxTemperature,
      enabled: fence.enabled,
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await fenceApi.deleteFence(id);
      message.success('删除成功');
      fetchFences();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleMapClick = (lat: number, lng: number) => {
    setDrawingPoints([...drawingPoints, [lat, lng]]);
  };

  const handleClearDrawing = () => {
    setDrawingPoints([]);
  };

  const handleUndoPoint = () => {
    if (drawingPoints.length > 0) {
      setDrawingPoints(drawingPoints.slice(0, -1));
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      if (drawingPoints.length < 3) {
        message.error('请在地图上至少点击3个点来绘制多边形围栏');
        return;
      }

      const fenceData = {
        ...values,
        coordinates: drawingPoints,
        fenceType: 'POLYGON',
      };

      if (editingFence) {
        await fenceApi.updateFence(editingFence.id, fenceData);
        message.success('更新成功');
      } else {
        await fenceApi.createFence(fenceData);
        message.success('创建成功');
      }

      setModalVisible(false);
      setDrawingMode(false);
      fetchFences();
    } catch (error) {
      console.error('Submit error:', error);
    }
  };

  const columns = [
    {
      title: '围栏名称',
      dataIndex: 'fenceName',
      key: 'fenceName',
      render: (text: string) => <Text strong>{text}</Text>,
    },
    {
      title: '最高温度限制',
      dataIndex: 'maxTemperature',
      key: 'maxTemperature',
      render: (temp: number) => <Tag color="orange">{temp}°C</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      render: (enabled: boolean) => (
        <Tag color={enabled ? 'green' : 'default'}>{enabled ? '启用' : '禁用'}</Tag>
      ),
    },
    {
      title: '顶点数',
      key: 'points',
      render: (_: unknown, record: ElectronicFenceDTO) => (
        <span>{record.coordinates?.length || 0} 个点</span>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, record: ElectronicFenceDTO) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个围栏吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <Title level={3} className="page-title">
          <EnvironmentOutlined style={{ marginRight: 8 }} />
          电子围栏管理
        </Title>
        <Space>
          <Button
            icon={<ReloadOutlined />}
            onClick={fetchFences}
          >
            刷新
          </Button>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleCreate}
          >
            新建围栏
          </Button>
        </Space>
      </div>

      <Card title="所有电子围栏">
        <Table
          columns={columns}
          dataSource={fences}
          rowKey="id"
          loading={loading}
        />
      </Card>

      <Modal
        title={editingFence ? '编辑电子围栏' : '新建电子围栏'}
        open={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          setDrawingMode(false);
        }}
        onOk={handleSubmit}
        okText="保存"
        cancelText="取消"
        width={900}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="fenceName"
            label="围栏名称"
            rules={[{ required: true, message: '请输入围栏名称' }]}
          >
            <Input placeholder="例如：北京五环内配送区" />
          </Form.Item>

          <Form.Item label="地图绘制">
            <div style={{ marginBottom: 8 }}>
              <Space>
                <Text type="secondary">
                  {drawingMode
                    ? '📝 点击地图添加顶点，至少需要3个点'
                    : '点击"开始绘制"后在地图上点击添加顶点'}
                </Text>
                <Button size="small" onClick={handleUndoPoint} disabled={drawingPoints.length === 0}>
                  撤销上一点
                </Button>
                <Button size="small" onClick={handleClearDrawing}>
                  清除所有
                </Button>
              </Space>
            </div>
            <div style={{ height: 400, borderRadius: 8, overflow: 'hidden' }}>
              <MapContainer
                center={BEIJING_CENTER}
                zoom={11}
                style={{ height: '100%', width: '100%' }}
              >
                <TileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution='&copy; OpenStreetMap contributors'
                />
                <MapClickHandler
                  onMapClick={handleMapClick}
                  drawing={drawingMode}
                />
                {drawingPoints.length > 0 && (
                  <>
                    <Polygon
                      positions={drawingPoints as [number, number][]}
                      pathOptions={{ color: '#ff4d4f', fillColor: '#ff4d4f', fillOpacity: 0.2 }}
                    />
                    {drawingPoints.map((point, index) => (
                      <Marker key={index} position={point as [number, number]}>
                        <Popup>顶点 {index + 1}: {point[0].toFixed(4)}, {point[1].toFixed(4)}</Popup>
                      </Marker>
                    ))}
                  </>
                )}
              </MapContainer>
            </div>
            <div style={{ marginTop: 8 }}>
              <Text type="secondary">
                已添加 {drawingPoints.length} 个顶点
                {drawingPoints.length >= 3 && (
                  <Tag color="green" style={{ marginLeft: 8 }}>✓ 有效</Tag>
                )}
                {drawingPoints.length < 3 && drawingPoints.length > 0 && (
                  <Tag color="orange" style={{ marginLeft: 8 }}>还需 {3 - drawingPoints.length} 个点</Tag>
                )}
              </Text>
            </div>
          </Form.Item>

          <Form.Item
            name="maxTemperature"
            label="最高温度限制 (°C)"
            rules={[{ required: true, message: '请输入最高温度限制' }]}
          >
            <InputNumber
              min={-30}
              max={30}
              step={0.5}
              style={{ width: '100%' }}
              placeholder="例如：4 表示该区域内温度不得超过4°C"
            />
          </Form.Item>

          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="可选的描述信息" />
          </Form.Item>

          <Form.Item name="enabled" label="启用状态" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default FenceManagement;
