import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import type { TracePointDTO } from '@/types';
import dayjs from 'dayjs';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

interface TemperatureChartProps {
  tracePoints: TracePointDTO[];
  minTemp?: number;
  maxTemp?: number;
}

export default function TemperatureChart({
  tracePoints,
  minTemp,
  maxTemp,
}: TemperatureChartProps) {
  const labels = tracePoints.map((p) => dayjs(p.traceTime).format('MM-DD HH:mm'));
  const temperatures = tracePoints.map((p) => p.temperature ?? null);

  const pointColors = tracePoints.map((p) =>
    p.temperatureStatus === 'ABNORMAL' ? '#ff4d4f' : '#52c41a'
  );

  const pointBackgrounds = tracePoints.map((p) =>
    p.temperatureStatus === 'ABNORMAL' ? '#ffccc7' : '#d9f7be'
  );

  const data = {
    labels,
    datasets: [
      ...(minTemp !== undefined
        ? [
            {
              label: '最低温度阈值',
              data: labels.map(() => minTemp),
              borderColor: '#faad14',
              borderDash: [5, 5],
              borderWidth: 1,
              pointRadius: 0,
              fill: false,
            },
          ]
        : []),
      ...(maxTemp !== undefined
        ? [
            {
              label: '最高温度阈值',
              data: labels.map(() => maxTemp),
              borderColor: '#faad14',
              borderDash: [5, 5],
              borderWidth: 1,
              pointRadius: 0,
              fill: false,
            },
          ]
        : []),
      {
        label: '实时温度 (°C)',
        data: temperatures,
        borderColor: '#1890ff',
        backgroundColor: 'rgba(24, 144, 255, 0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 5,
        pointHoverRadius: 7,
        pointBackgroundColor: pointBackgrounds,
        pointBorderColor: pointColors,
        pointBorderWidth: 2,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      tooltip: {
        mode: 'index' as const,
        intersect: false,
        callbacks: {
          label: function (context: any) {
            const point = tracePoints[context.dataIndex];
            const baseLabel = `${context.dataset.label}: ${context.parsed.y?.toFixed(1)}°C`;
            if (point?.locationName) {
              return `${baseLabel} (${point.locationName})`;
            }
            return baseLabel;
          },
        },
      },
    },
    scales: {
      y: {
        title: {
          display: true,
          text: '温度 (°C)',
        },
        grid: {
          color: 'rgba(0, 0, 0, 0.05)',
        },
      },
      x: {
        title: {
          display: true,
          text: '时间',
        },
        grid: {
          display: false,
        },
        ticks: {
          maxRotation: 45,
          minRotation: 45,
        },
      },
    },
    interaction: {
      mode: 'nearest' as const,
      axis: 'x' as const,
      intersect: false,
    },
  };

  return (
    <div className="temp-chart-container">
      <Line data={data} options={options} />
    </div>
  );
}
