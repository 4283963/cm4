import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import { Layout, Menu, Button } from 'antd';
import {
  CarOutlined,
  AppstoreOutlined,
  DashboardOutlined,
  ReloadOutlined,
  EnvironmentOutlined,
} from '@ant-design/icons';
import VehicleDashboard from '@/pages/VehicleDashboard';
import CargoTraceability from '@/pages/CargoTraceability';
import FenceManagement from '@/pages/FenceManagement';
import { mockApi } from '@/services/api';
import { useState } from 'react';
import { message } from 'antd';

const { Header, Content, Sider } = Layout;

function App() {
  const navigate = useNavigate();
  const [refreshing, setRefreshing] = useState(false);

  const handleSendMockData = async () => {
    setRefreshing(true);
    try {
      await mockApi.sendMockDataForAll();
      message.success('模拟数据已推送');
      setTimeout(() => window.location.reload(), 500);
    } catch (error) {
      message.error('推送失败');
    } finally {
      setRefreshing(false);
    }
  };

  const menuItems = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: <Link to="/">车辆看板</Link>,
    },
    {
      key: '/traceability',
      icon: <AppstoreOutlined />,
      label: <Link to="/traceability">货品溯源</Link>,
    },
    {
      key: '/fences',
      icon: <EnvironmentOutlined />,
      label: <Link to="/fences">电子围栏管理</Link>,
    },
  ];

  return (
    <Layout className="app-layout">
      <Header className="app-header">
        <div className="app-logo">
          <CarOutlined style={{ fontSize: 24 }} />
          多温区冷藏车货品溯源系统
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <Button
            type="primary"
            icon={<ReloadOutlined spin={refreshing} />}
            onClick={handleSendMockData}
            loading={refreshing}
          >
            推送模拟数据
          </Button>
        </div>
      </Header>
      <Layout>
        <Sider width={200} style={{ background: '#fff' }}>
          <Menu
            mode="inline"
            defaultSelectedKeys={[window.location.pathname]}
            style={{ height: '100%', borderRight: 0 }}
            items={menuItems}
            onClick={({ key }) => navigate(key)}
          />
        </Sider>
        <Layout style={{ padding: '24px' }}>
          <Content
            style={{
              padding: 24,
              margin: 0,
              minHeight: 'calc(100vh - 112px)',
              background: '#fff',
              borderRadius: 8,
            }}
          >
            <Routes>
              <Route path="/" element={<VehicleDashboard />} />
              <Route path="/traceability" element={<CargoTraceability />} />
              <Route path="/traceability/:batchNo" element={<CargoTraceability />} />
              <Route path="/fences" element={<FenceManagement />} />
            </Routes>
          </Content>
        </Layout>
      </Layout>
    </Layout>
  );
}

export default App;
