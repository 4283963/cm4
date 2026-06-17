package com.coldchain.traceability.service;

import com.coldchain.traceability.dto.GatewayDataDTO;
import com.coldchain.traceability.entity.CargoBatch;
import com.coldchain.traceability.entity.TemperatureZone;
import com.coldchain.traceability.entity.Vehicle;
import com.coldchain.traceability.repository.CargoBatchRepository;
import com.coldchain.traceability.repository.TemperatureZoneRepository;
import com.coldchain.traceability.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockDataService {

    private final VehicleRepository vehicleRepository;
    private final TemperatureZoneRepository temperatureZoneRepository;
    private final CargoBatchRepository cargoBatchRepository;
    private final GatewayDataService gatewayDataService;

    private final Random random = new Random();

    @Transactional
    public void initializeMockData() {
        log.info("Initializing mock data...");

        if (vehicleRepository.count() > 0) {
            log.info("Mock data already exists, skipping initialization");
            return;
        }

        Vehicle vehicle1 = createVehicle("京A·12345", "张三", "13800138001", "冷链车-大型", 3);
        Vehicle vehicle2 = createVehicle("沪B·67890", "李四", "13800138002", "冷链车-中型", 2);
        Vehicle vehicle3 = createVehicle("粤C·54321", "王五", "13800138003", "冷链车-小型", 2);

        createTemperatureZones(vehicle1);
        createTemperatureZones(vehicle2);
        createTemperatureZones(vehicle3);

        createCargoBatches(vehicle1);
        createCargoBatches(vehicle2);
        createCargoBatches(vehicle3);

        log.info("Mock data initialization complete");
    }

    private Vehicle createVehicle(String plate, String driver, String phone, String model, int capacity) {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(plate);
        vehicle.setDriverName(driver);
        vehicle.setDriverPhone(phone);
        vehicle.setModel(model);
        vehicle.setTotalCapacity(capacity);
        vehicle.setStatus("TRANSIT");
        vehicle.setCurrentLatitude(39.9042 + random.nextDouble() * 0.1);
        vehicle.setCurrentLongitude(116.4074 + random.nextDouble() * 0.1);
        vehicle.setLastUpdateTime(LocalDateTime.now());
        return vehicleRepository.save(vehicle);
    }

    private void createTemperatureZones(Vehicle vehicle) {
        String plate = vehicle.getPlateNumber();

        if (plate.equals("京A·12345")) {
            createZone(vehicle, "FROZEN-01", "冷冻区-1", "FROZEN", new BigDecimal("-25"), new BigDecimal("-18"));
            createZone(vehicle, "CHILLED-01", "冷藏区-1", "CHILLED", new BigDecimal("0"), new BigDecimal("4"));
            createZone(vehicle, "FRESH-01", "保鲜区-1", "FRESH", new BigDecimal("4"), new BigDecimal("10"));
        } else if (plate.equals("沪B·67890")) {
            createZone(vehicle, "FROZEN-01", "冷冻区-1", "FROZEN", new BigDecimal("-25"), new BigDecimal("-18"));
            createZone(vehicle, "CHILLED-01", "冷藏区-1", "CHILLED", new BigDecimal("0"), new BigDecimal("4"));
        } else {
            createZone(vehicle, "CHILLED-01", "冷藏区-1", "CHILLED", new BigDecimal("0"), new BigDecimal("4"));
            createZone(vehicle, "FRESH-01", "保鲜区-1", "FRESH", new BigDecimal("4"), new BigDecimal("10"));
        }
    }

    private void createZone(Vehicle vehicle, String code, String name, String type,
                            BigDecimal minTemp, BigDecimal maxTemp) {
        TemperatureZone zone = new TemperatureZone();
        zone.setVehicle(vehicle);
        zone.setZoneCode(code);
        zone.setZoneName(name);
        zone.setZoneType(type);
        zone.setMinTemperature(minTemp);
        zone.setMaxTemperature(maxTemp);
        zone.setCurrentTemperature(minTemp.add(maxTemp).divide(new BigDecimal("2")));
        zone.setCapacity(1000);
        temperatureZoneRepository.save(zone);
    }

    private void createCargoBatches(Vehicle vehicle) {
        String plate = vehicle.getPlateNumber();
        List<TemperatureZone> zones = temperatureZoneRepository.findByVehiclePlate(plate);

        if (plate.equals("京A·12345")) {
            for (TemperatureZone zone : zones) {
                if ("FROZEN-01".equals(zone.getZoneCode())) {
                    createCargo("BATCH-FZ-001", "进口深海大虾", "海鲜冷冻", zone,
                            new BigDecimal("-25"), new BigDecimal("-18"),
                            "上海水产批发市场", "北京新发地市场");
                    createCargo("BATCH-FZ-002", "法式鹅肝", "高端冷冻", zone,
                            new BigDecimal("-25"), new BigDecimal("-18"),
                            "广州白云机场", "北京SKP商场");
                } else if ("CHILLED-01".equals(zone.getZoneCode())) {
                    createCargo("BATCH-CL-001", "有机蔬菜礼盒", "有机蔬菜", zone,
                            new BigDecimal("0"), new BigDecimal("4"),
                            "山东寿光蔬菜基地", "北京盒马鲜生");
                    createCargo("BATCH-CL-002", "巴氏杀菌鲜奶", "乳制品", zone,
                            new BigDecimal("0"), new BigDecimal("4"),
                            "内蒙古呼和浩特", "北京各超市");
                } else if ("FRESH-01".equals(zone.getZoneCode())) {
                    createCargo("BATCH-FR-001", "云南鲜花礼盒", "鲜花", zone,
                            new BigDecimal("4"), new BigDecimal("10"),
                            "云南昆明斗南", "北京花店");
                }
            }
        } else if (plate.equals("沪B·67890")) {
            for (TemperatureZone zone : zones) {
                if ("FROZEN-01".equals(zone.getZoneCode())) {
                    createCargo("BATCH-SH-FZ-001", "加拿大北极贝", "海鲜冷冻", zone,
                            new BigDecimal("-25"), new BigDecimal("-18"),
                            "上海洋山港", "杭州万象城Ole超市");
                } else if ("CHILLED-01".equals(zone.getZoneCode())) {
                    createCargo("BATCH-SH-CL-001", "澳洲和牛M9", "高端肉类", zone,
                            new BigDecimal("0"), new BigDecimal("4"),
                            "上海浦东机场", "杭州湖滨银泰");
                }
            }
        } else {
            for (TemperatureZone zone : zones) {
                if ("CHILLED-01".equals(zone.getZoneCode())) {
                    createCargo("BATCH-GZ-CL-001", "广东荔枝", "新鲜水果", zone,
                            new BigDecimal("0"), new BigDecimal("4"),
                            "广东茂名", "深圳各商超");
                } else if ("FRESH-01".equals(zone.getZoneCode())) {
                    createCargo("BATCH-GZ-FR-001", "有机绿叶菜", "有机蔬菜", zone,
                            new BigDecimal("4"), new BigDecimal("10"),
                            "广东惠州农场", "广州盒马鲜生");
                }
            }
        }
    }

    private void createCargo(String batchNo, String name, String type, TemperatureZone zone,
                             BigDecimal minTemp, BigDecimal maxTemp, String origin, String destination) {
        CargoBatch cargo = new CargoBatch();
        cargo.setBatchNo(batchNo);
        cargo.setCargoName(name);
        cargo.setCargoType(type);
        cargo.setOrigin(origin);
        cargo.setDestination(destination);
        cargo.setQuantity(100 + random.nextInt(900));
        cargo.setUnit("箱");
        cargo.setWeight(new BigDecimal(100 + random.nextInt(900)));
        cargo.setRequiredMinTemp(minTemp);
        cargo.setRequiredMaxTemp(maxTemp);
        cargo.setStatus("IN_TRANSIT");
        cargo.setLoadingTime(LocalDateTime.now().minusHours(random.nextInt(24)));
        cargo.setExpectedArrivalTime(LocalDateTime.now().plusHours(12 + random.nextInt(36)));
        cargo.setTemperatureZone(zone);
        cargoBatchRepository.save(cargo);
    }

    public GatewayDataDTO generateMockGatewayData(String plateNumber) {
        return generateMockGatewayData(plateNumber, false);
    }

    public GatewayDataDTO generateMockGatewayData(String plateNumber, boolean forceGpsLost) {
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + plateNumber));

        GatewayDataDTO dto = new GatewayDataDTO();
        dto.setGatewayId("GW-" + UUID.randomUUID().toString().substring(0, 8));
        dto.setMessageId("MSG-" + System.currentTimeMillis());
        dto.setTimestamp(LocalDateTime.now());

        GatewayDataDTO.VehicleInfoDTO vehicleInfo = new GatewayDataDTO.VehicleInfoDTO();
        vehicleInfo.setPlateNumber(plateNumber);
        vehicleInfo.setVehicleId("VH-" + vehicle.getId());
        vehicleInfo.setDriverId("DR-" + plateNumber);
        dto.setVehicle(vehicleInfo);

        double latChange = (random.nextDouble() - 0.5) * 0.01;
        double lngChange = (random.nextDouble() - 0.5) * 0.01;
        double newLat = vehicle.getCurrentLatitude() + latChange;
        double newLng = vehicle.getCurrentLongitude() + lngChange;

        GatewayDataDTO.GpsDataDTO gps = new GatewayDataDTO.GpsDataDTO();

        boolean gpsLost = forceGpsLost || random.nextDouble() < 0.15;

        List<GatewayDataDTO.CoordDTO> coords = new ArrayList<>();
        if (!gpsLost) {
            GatewayDataDTO.CoordDTO coord = new GatewayDataDTO.CoordDTO();
            coord.setLatitude(newLat);
            coord.setLongitude(newLng);
            coord.setAltitude(50.0 + random.nextDouble() * 100);
            coord.setAccuracy(3.0 + random.nextDouble() * 5.0);
            coords.add(coord);
            gps.setLatitude(newLat);
            gps.setLongitude(newLng);
            gps.setAltitude(coord.getAltitude());
            gps.setSpeed(40 + random.nextDouble() * 60);
            gps.setHeading(random.nextDouble() * 360);
            gps.setSatellites(8 + random.nextInt(6));
            gps.setLocationName(generateLocationName(plateNumber));
        } else {
            gps.setSatellites(0);
            gps.setSpeed(0.0);
            gps.setLocationName("信号盲区（隧道/山区）");
            log.warn("Simulating GPS signal lost for vehicle {} - coords array empty", plateNumber);
        }
        gps.setCoords(coords);
        gps.setTimestamp(LocalDateTime.now());
        dto.setGps(gps);

        List<TemperatureZone> zones = temperatureZoneRepository.findByVehiclePlate(plateNumber);
        List<GatewayDataDTO.TemperatureZoneDataDTO> zoneDataList = new ArrayList<>();

        for (TemperatureZone zone : zones) {
            GatewayDataDTO.TemperatureZoneDataDTO zoneData = new GatewayDataDTO.TemperatureZoneDataDTO();
            zoneData.setZoneCode(zone.getZoneCode());
            zoneData.setZoneName(zone.getZoneName());
            zoneData.setStatus("NORMAL");

            double baseTemp = zone.getMinTemperature().add(zone.getMaxTemperature())
                    .divide(new BigDecimal("2")).doubleValue();
            double tempVariation = (random.nextDouble() - 0.5) * 6;
            double actualTemp = baseTemp + tempVariation;
            zoneData.setAvgTemperature(actualTemp);

            List<GatewayDataDTO.SensorDataDTO> sensors = new ArrayList<>();

            GatewayDataDTO.SensorDataDTO tempSensor = new GatewayDataDTO.SensorDataDTO();
            tempSensor.setSensorId("TEMP-" + zone.getZoneCode());
            tempSensor.setSensorType("temperature");
            tempSensor.setValue(actualTemp);
            tempSensor.setUnit("°C");
            tempSensor.setStatus(getStatus(actualTemp, zone.getMinTemperature().doubleValue(),
                    zone.getMaxTemperature().doubleValue()));
            tempSensor.setTimestamp(LocalDateTime.now());
            sensors.add(tempSensor);

            GatewayDataDTO.SensorDataDTO humiditySensor = new GatewayDataDTO.SensorDataDTO();
            humiditySensor.setSensorId("HUM-" + zone.getZoneCode());
            humiditySensor.setSensorType("humidity");
            humiditySensor.setValue(60 + random.nextDouble() * 30);
            humiditySensor.setUnit("%");
            humiditySensor.setStatus("NORMAL");
            humiditySensor.setTimestamp(LocalDateTime.now());
            sensors.add(humiditySensor);

            zoneData.setSensors(sensors);
            zoneDataList.add(zoneData);
        }

        dto.setTemperatureZones(zoneDataList);

        GatewayDataDTO.DeviceStatusDTO deviceStatus = new GatewayDataDTO.DeviceStatusDTO();
        deviceStatus.setBatteryLevel(80 + random.nextInt(20));
        deviceStatus.setSignalStrength(70 + random.nextInt(30));
        deviceStatus.setConnectionStatus("CONNECTED");
        dto.setDeviceStatus(deviceStatus);

        return dto;
    }

    private String generateLocationName(String plateNumber) {
        if (plateNumber.startsWith("京")) {
            return "北京市" + (random.nextBoolean() ? "朝阳区" : "海淀区") + "境内";
        } else if (plateNumber.startsWith("沪")) {
            return "上海市" + (random.nextBoolean() ? "浦东新区" : "黄浦区") + "境内";
        } else if (plateNumber.startsWith("粤")) {
            return "广东省" + (random.nextBoolean() ? "广州市" : "深圳市") + "境内";
        }
        return "运输途中";
    }

    private String getStatus(double value, double min, double max) {
        if (value < min || value > max) {
            return "ABNORMAL";
        }
        return "NORMAL";
    }

    @Transactional
    public void sendMockData(String plateNumber) {
        GatewayDataDTO mockData = generateMockGatewayData(plateNumber);
        gatewayDataService.processGatewayData(mockData);
        log.info("Mock data sent for vehicle: {}", plateNumber);
    }

    @Transactional
    public void sendMockDataForAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        for (Vehicle vehicle : vehicles) {
            sendMockData(vehicle.getPlateNumber());
        }
    }

    @Transactional
    public void sendMockDataWithGpsLost(String plateNumber) {
        GatewayDataDTO mockData = generateMockGatewayData(plateNumber, true);
        gatewayDataService.processGatewayData(mockData);
        log.info("Mock data with GPS lost sent for vehicle: {}", plateNumber);
    }

    @Transactional
    public void sendMockDataWithGpsLostForAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        for (Vehicle vehicle : vehicles) {
            sendMockDataWithGpsLost(vehicle.getPlateNumber());
        }
    }
}
