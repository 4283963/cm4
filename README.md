# 多温区冷藏车货品溯源系统

## 系统概述

这是一个完整的多温区冷藏车货品溯源系统，用于监控生鲜物流过程中的车辆位置和各温区温度，并为每一批货品生成完整的"温度+地理"双重生命周期溯源数据。

## 技术栈

### 后端
- **Spring Boot 3.2.0** - 应用框架
- **Spring Data JPA** - ORM 数据访问
- **PostgreSQL** - 关系型数据库
- **Lombok** - 代码简化
- **MapStruct** - 对象映射
- **Jackson** - JSON 处理

### 前端
- **React 18** - UI 框架
- **TypeScript** - 类型安全
- **Vite** - 构建工具
- **Ant Design 5** - UI 组件库
- **Leaflet + React-Leaflet** - 地图组件
- **Chart.js + react-chartjs-2** - 图表组件
- **React Router** - 路由管理
- **Axios** - HTTP 客户端

## 系统架构

```
第三方网关 → 复杂嵌套 JSON → Spring Boot 后端 → 解析处理 → PostgreSQL
                                                         ↓
                                         前端 React 看板 ← REST API
                                                         ↓
                                         地图展示 + 温度图表 + 溯源时间轴
```

## 核心业务流程

1. **数据推送**: 第三方 GPS 和温度传感器网关定时推送复杂嵌套 JSON 数据到 `/api/gateway/data`
2. **数据解析**: 后端解析经纬度、各温区温度、传感器数据
3. **关联货品**: 根据车辆和温区信息，关联到每一批在运货品
4. **生成溯源日志**: 为每个货品创建包含位置+温度的溯源记录
5. **前端展示**: 通过 API 拉取数据，在地图上展示轨迹和温度变化

## 项目结构

```
cm4/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/coldchain/traceability/
│   │   ├── TraceabilityApplication.java    # 启动类
│   │   ├── config/                         # 配置类
│   │   │   ├── DataInitializer.java        # 数据初始化
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   ├── controller/                     # REST 控制器
│   │   │   ├── GatewayController.java      # 网关数据接收
│   │   │   ├── VehicleController.java      # 车辆管理
│   │   │   ├── TraceabilityController.java # 溯源查询
│   │   │   ├── CargoBatchController.java   # 货品管理
│   │   │   └── MockDataController.java     # 模拟数据
│   │   ├── service/                        # 业务服务
│   │   │   ├── GatewayDataService.java     # 网关数据处理核心
│   │   │   ├── VehicleService.java         # 车辆服务
│   │   │   ├── TraceabilityService.java    # 溯源服务
│   │   │   ├── CargoBatchService.java      # 货品服务
│   │   │   └── MockDataService.java        # 模拟数据服务
│   │   ├── repository/                     # 数据访问层
│   │   │   ├── VehicleRepository.java
│   │   │   ├── TemperatureZoneRepository.java
│   │   │   ├── CargoBatchRepository.java
│   │   │   └── CargoTraceLogRepository.java
│   │   ├── entity/                         # 数据库实体
│   │   │   ├── Vehicle.java                # 车辆
│   │   │   ├── TemperatureZone.java        # 温区
│   │   │   ├── CargoBatch.java             # 货品批次
│   │   │   └── CargoTraceLog.java          # 溯源日志
│   │   └── dto/                            # 数据传输对象
│   │       ├── GatewayDataDTO.java         # 网关数据结构（复杂嵌套）
│   │       ├── ApiResponse.java            # 统一响应
│   │       ├── VehicleStatusDTO.java       # 车辆状态
│   │       └── CargoTraceDTO.java          # 溯源数据
│   └── src/main/resources/
│       ├── application.yml                 # 应用配置
│       └── db/init.sql                     # 数据库初始化脚本
│
└── frontend/                         # React 前端
    ├── src/
    │   ├── main.tsx                      # 入口文件
    │   ├── App.tsx                       # 主应用组件
    │   ├── types/index.ts                # TypeScript 类型定义
    │   ├── services/api.ts               # API 服务层
    │   ├── styles/global.scss            # 全局样式
    │   ├── components/                   # 通用组件
    │   │   ├── VehicleMap.tsx            # 车辆地图组件
    │   │   └── TemperatureChart.tsx      # 温度图表组件
    │   └── pages/                        # 页面组件
    │       ├── VehicleDashboard.tsx      # 车辆状态看板
    │       └── CargoTraceability.tsx     # 货品溯源页面
    ├── package.json
    ├── vite.config.ts
    └── tsconfig.json
```

## 数据库设计

### 核心数据表

| 表名 | 说明 |
|------|------|
| `vehicles` | 运输车辆信息 |
| `temperature_zones` | 车辆温区配置（冷冻区、冷藏区、保鲜区） |
| `cargo_batches` | 货品批次信息 |
| `cargo_trace_logs` | 货品溯源日志（每一次推送都生成记录） |

## API 接口文档

### 1. 网关数据推送（第三方调用）

**POST** `/api/gateway/data`

接收第三方网关推送的复杂嵌套 JSON 数据。请求体结构示例：

```json
{
  "gateway_id": "GW-ABC123",
  "message_id": "MSG-1234567890",
  "timestamp": "2024-01-15T10:30:00",
  "vehicle": {
    "plate_number": "京A·12345",
    "vehicle_id": "VH-001",
    "driver_id": "DR-001"
  },
  "gps": {
    "latitude": 39.9042,
    "longitude": 116.4074,
    "altitude": 50.5,
    "speed": 65.5,
    "heading": 180.0,
    "satellites": 10,
    "location_name": "北京市朝阳区境内",
    "timestamp": "2024-01-15T10:30:00"
  },
  "temperature_zones": [
    {
      "zone_code": "FROZEN-01",
      "zone_name": "冷冻区-1",
      "avg_temperature": -20.5,
      "status": "NORMAL",
      "sensors": [
        {
          "sensor_id": "TEMP-FZ-01",
          "sensor_type": "temperature",
          "value": -20.5,
          "unit": "°C",
          "status": "NORMAL",
          "timestamp": "2024-01-15T10:30:00"
        },
        {
          "sensor_id": "HUM-FZ-01",
          "sensor_type": "humidity",
          "value": 65.2,
          "unit": "%",
          "status": "NORMAL",
          "timestamp": "2024-01-15T10:30:00"
        }
      ]
    }
  ],
  "device_status": {
    "battery_level": 95,
    "signal_strength": 85,
    "connection_status": "CONNECTED"
  }
}
```

### 2. 车辆状态查询

**GET** `/api/vehicles` - 获取所有在途车辆状态

**GET** `/api/vehicles/{plateNumber}` - 获取指定车辆详细状态

### 3. 货品溯源查询

**GET** `/api/traceability/cargo/{batchNo}` - 获取货品完整溯源数据

**GET** `/api/traceability/cargo/{batchNo}/range?startTime=xxx&endTime=xxx` - 按时间范围查询

### 4. 货品管理

**GET** `/api/cargos` - 获取所有在运货品列表

### 5. 模拟数据接口（测试用）

**POST** `/api/mock/init` - 初始化模拟数据（3辆车 + 多个温区 + 货品）

**GET** `/api/mock/generate/{plateNumber}` - 生成模拟数据（不入库）

**POST** `/api/mock/send/{plateNumber}` - 推送单辆车模拟数据

**POST** `/api/mock/send-all` - 推送所有车辆模拟数据

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.8+
- PostgreSQL 14+
- Node.js 18+
- npm 或 pnpm

### 1. 数据库准备

```sql
-- 执行 backend/src/main/resources/db/init.sql
CREATE DATABASE coldchain_db;
CREATE USER coldchain_user WITH PASSWORD 'coldchain_password';
GRANT ALL PRIVILEGES ON DATABASE coldchain_db TO coldchain_user;
\c coldchain_db
GRANT ALL ON SCHEMA public TO coldchain_user;
```

### 2. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

> **注意**: 首次启动会自动初始化 3 辆冷藏车、多个温区和 10 批货品的模拟数据

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器将在 `http://localhost:3000` 启动

### 4. 测试流程

1. 打开前端页面 `http://localhost:3000`
2. 点击顶部"推送模拟数据"按钮，模拟第三方网关推送数据
3. 查看车辆看板，观察车辆位置和温区状态
4. 点击"查看溯源"进入货品溯源页面
5. 查看地图上的温度+地理双重轨迹
6. 切换标签页查看温度趋势图、溯源时间轴和异常记录

## 核心功能特性

### 后端特性
- ✅ 复杂嵌套 JSON 解析（支持多温区、多传感器）
- ✅ 异步数据处理（@Async）
- ✅ 批量数据插入优化
- ✅ 温度异常自动检测
- ✅ 完整的溯源日志生成
- ✅ 多维度溯源数据统计
- ✅ 模拟数据生成器

### 前端特性
- ✅ 车辆实时位置地图展示
- ✅ 多温区温度状态监控
- ✅ 货品"温度+地理"双重轨迹地图
- ✅ 温度变化趋势图表
- ✅ 溯源时间轴展示
- ✅ 温度异常告警记录
- ✅ 按时间范围筛选溯源数据
- ✅ 响应式设计，支持移动端

## 温区类型

| 类型 | 代码 | 温度范围 | 适用货品 |
|------|------|----------|----------|
| 冷冻区 | FROZEN | -25°C ~ -18°C | 海鲜、肉类、速冻食品 |
| 冷藏区 | CHILLED | 0°C ~ 4°C | 蔬菜、水果、乳制品 |
| 保鲜区 | FRESH | 4°C ~ 10°C | 鲜花、部分水果 |

## 关键代码参考

### 复杂 JSON 数据解析核心
- [GatewayDataService.java](file:///Users/kl/Documents/trae_projects2/cm4/backend/src/main/java/com/coldchain/traceability/service/GatewayDataService.java#L27-L55) - 数据处理主流程
- [GatewayDataDTO.java](file:///Users/kl/Documents/trae_projects2/cm4/backend/src/main/java/com/coldchain/traceability/dto/GatewayDataDTO.java) - 复杂嵌套 JSON 结构定义

### 溯源数据生成
- [GatewayDataService.java](file:///Users/kl/Documents/trae_projects2/cm4/backend/src/main/java/com/coldchain/traceability/service/GatewayDataService.java#L124-L163) - 货品溯源日志生成

### 前端地图组件
- [VehicleMap.tsx](file:///Users/kl/Documents/trae_projects2/cm4/frontend/src/components/VehicleMap.tsx) - 地图展示（含轨迹和温度标记）

### 货品溯源页面
- [CargoTraceability.tsx](file:///Users/kl/Documents/trae_projects2/cm4/frontend/src/pages/CargoTraceability.tsx) - 温度+地理双重溯源图

## 温度状态判断逻辑

系统会自动判断每个溯源点的温度状态：

1. **NORMAL (正常)**: 温度在货品要求的温度范围内
2. **ABNORMAL (异常)**: 温度超出范围，进一步细分为：
   - `OVERHEAT` - 温度过高
   - `UNDERCOOL` - 温度过低
3. **UNKNOWN (未知)**: 缺少温度数据或阈值配置

## 注意事项

1. 生产环境请将 `ddl-auto` 设置为 `none`，使用 Flyway 管理数据库版本
2. 网关推送接口应添加身份认证（如 API Key、OAuth2）
3. 大规模数据时请考虑使用时序数据库（如 InfluxDB、TimescaleDB）存储溯源日志
4. 地图服务在生产环境建议使用国内地图服务商（高德、百度）
5. 建议添加 WebSocket 实现真正的实时数据推送

## License

MIT
