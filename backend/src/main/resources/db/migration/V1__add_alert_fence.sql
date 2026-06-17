-- 电子围栏和报警功能数据库迁移脚本
-- 执行前请确保已连接到 coldchain_db 数据库

-- 电子围栏表
CREATE TABLE IF NOT EXISTS electronic_fences (
    id BIGSERIAL PRIMARY KEY,
    fence_name VARCHAR(100) NOT NULL,
    fence_type VARCHAR(20) NOT NULL DEFAULT 'POLYGON',
    coordinates TEXT NOT NULL,
    center_latitude DOUBLE PRECISION,
    center_longitude DOUBLE PRECISION,
    radius DOUBLE PRECISION,
    max_temperature DECIMAL(5,2) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE electronic_fences IS '电子围栏配置表';
COMMENT ON COLUMN electronic_fences.fence_type IS '围栏类型：POLYGON(多边形), CIRCLE(圆形)';
COMMENT ON COLUMN electronic_fences.coordinates IS '坐标点JSON数组：[[lat,lng],[lat,lng]...]';
COMMENT ON COLUMN electronic_fences.max_temperature IS '该围栏内的最高温度限制(°C)';

-- 报警表
CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL,
    alert_level VARCHAR(20) NOT NULL,
    alert_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    cargo_batch_id BIGINT,
    cargo_batch_no VARCHAR(50),
    vehicle_plate VARCHAR(20),
    fence_id BIGINT,
    fence_name VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    temperature DECIMAL(5,2),
    max_temperature DECIMAL(5,2),
    consecutive_count INTEGER,
    message TEXT,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_by VARCHAR(50),
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE alerts IS '报警记录表';
COMMENT ON COLUMN alerts.alert_type IS '报警类型：TEMPERATURE_BROKEN_CHAIN(温度断链), GEO_FENCE(越界)';
COMMENT ON COLUMN alerts.alert_level IS '报警级别：WARNING, CRITICAL';
COMMENT ON COLUMN alerts.alert_status IS '报警状态：PENDING(待处理), ACKNOWLEDGED(已确认), RESOLVED(已解决)';

-- 扩展货品批次表
ALTER TABLE cargo_batches ADD COLUMN IF NOT EXISTS alert_status VARCHAR(20) DEFAULT 'NORMAL';
ALTER TABLE cargo_batches ADD COLUMN IF NOT EXISTS last_alert_id BIGINT;

COMMENT ON COLUMN cargo_batches.alert_status IS '报警状态：NORMAL(正常), BROKEN_CHAIN(已变质拦截)';

-- 扩展温度区表
ALTER TABLE temperature_zones ADD COLUMN IF NOT EXISTS consecutive_over_temp_count INTEGER DEFAULT 0;
ALTER TABLE temperature_zones ADD COLUMN IF NOT EXISTS last_temperature_check TIMESTAMP;

COMMENT ON COLUMN temperature_zones.consecutive_over_temp_count IS '连续温度超标计数';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_alert_status ON alerts(alert_status);
CREATE INDEX IF NOT EXISTS idx_alert_cargo_batch ON alerts(cargo_batch_id);
CREATE INDEX IF NOT EXISTS idx_alert_created ON alerts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_fence_enabled ON electronic_fences(enabled);

-- 插入测试数据：北京五环电子围栏（简化的多边形）
INSERT INTO electronic_fences (fence_name, fence_type, coordinates, center_latitude, center_longitude, max_temperature, description, enabled)
VALUES (
    '北京五环内配送区',
    'POLYGON',
    '[[39.9925,116.4551],[39.9925,116.2865],[39.8425,116.2865],[39.8425,116.4551],[39.9925,116.4551]]',
    39.9175,
    116.3708,
    4.0,
    '北京五环内生鲜配送区，温度必须保持在4°C以下',
    true
) ON CONFLICT DO NOTHING;
