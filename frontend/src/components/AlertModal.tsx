import React, { useState } from 'react';
import { Modal, Button, Tag, Space, Typography, List } from 'antd';
import { WarningOutlined, BellOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import type { AlertDTO } from '@/types';
import { alertApi } from '@/services/api';

const { Text } = Typography;

interface AlertModalProps {
  visible: boolean;
  alerts: AlertDTO[];
  onClose: () => void;
  onAlertHandled: () => void;
}

const AlertModal: React.FC<AlertModalProps> = ({ visible, alerts, onClose, onAlertHandled }) => {
  const [loading, setLoading] = useState(false);
  const [selectedAlert, setSelectedAlert] = useState<AlertDTO | null>(null);

  const handleAcknowledge = async (alertId: number) => {
    setLoading(true);
    try {
      await alertApi.acknowledgeAlert(alertId, 'admin');
      onAlertHandled();
    } catch (error) {
      console.error('Failed to acknowledge alert:', error);
    } finally {
      setLoading(false);
    }
  };

  const getAlertLevelColor = (level: string) => {
    return level === 'CRITICAL' ? '#ff4d4f' : '#fa8c16';
  };

  const getAlertLevelText = (level: string) => {
    return level === 'CRITICAL' ? '严重' : '警告';
  };

  const getAlertTypeText = (type: string) => {
    return type === 'TEMPERATURE_BROKEN_CHAIN' ? '温度断链' : '越界报警';
  };

  return (
    <Modal
      title={
        <Space>
          <WarningOutlined style={{ color: '#ff4d4f', fontSize: 24 }} />
          <span style={{ color: '#ff4d4f', fontSize: 18, fontWeight: 'bold' }}>
            🚨 紧急报警通知 - {alerts.length} 条待处理
          </span>
        </Space>
      }
      open={visible}
      onCancel={onClose}
      width={800}
      footer={[
        <Button key="close" onClick={onClose}>
          稍后处理
        </Button>,
        <Button
          key="acknowledge"
          type="primary"
          danger
          icon={<BellOutlined />}
          onClick={() => {
            if (selectedAlert) {
              handleAcknowledge(selectedAlert.id);
            }
          }}
          disabled={!selectedAlert || loading}
        >
          确认报警
        </Button>,
      ]}
    >
      <div style={{ maxHeight: '60vh', overflowY: 'auto' }}>
        <List
          dataSource={alerts}
          renderItem={(alert) => (
            <List.Item
              key={alert.id}
              onClick={() => setSelectedAlert(alert)}
              style={{
                background: selectedAlert?.id === alert.id ? '#fff2f0' : 'transparent',
                borderLeft: `4px solid ${getAlertLevelColor(alert.alertLevel)}`,
                padding: '12px 16px',
                marginBottom: '8px',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              <List.Item.Meta
                title={
                  <Space>
                    <Tag color={getAlertLevelColor(alert.alertLevel)}>
                      {getAlertLevelText(alert.alertLevel)}
                    </Tag>
                    <Tag>{getAlertTypeText(alert.alertType)}</Tag>
                    <Text strong>{alert.cargoBatchNo}</Text>
                  </Space>
                }
                description={
                  <div>
                    <Text type="secondary">{alert.message}</Text>
                    <div style={{ marginTop: '8px' }}>
                      <Space>
                        <Text type="secondary">
                          🚛 {alert.vehiclePlate}
                        </Text>
                        <Text type="secondary">
                          📍 {alert.fenceName}
                        </Text>
                        <Text type="secondary" style={{ color: '#ff4d4f' }}>
                          🌡️ {alert.temperature?.toFixed(1)}°C / 限值 {alert.maxTemperature?.toFixed(1)}°C
                        </Text>
                        <Text type="secondary">
                          连续 {alert.consecutiveCount} 次超标
                        </Text>
                      </Space>
                    </div>
                    <div style={{ marginTop: '4px' }}>
                      <Text type="secondary" style={{ fontSize: '12px' }}>
                        ⏰ {dayjs(alert.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                      </Text>
                    </div>
                  </div>
                }
              />
              {alert.alertStatus === 'PENDING' && !alert.acknowledged && (
                <Tag color="red">待处理</Tag>
              )}
            </List.Item>
          )}
        />
      </div>
    </Modal>
  );
};

export default AlertModal;
