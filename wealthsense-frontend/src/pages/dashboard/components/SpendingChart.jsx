import { useState, useMemo, memo } from 'react';
import { motion } from 'framer-motion';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell,
  PieChart, Pie, Legend,
} from 'recharts';
import { formatCurrency } from '@/utils/formatters';
import { SkeletonChart } from '@/components/ui/SkeletonLoader';
import { cn } from '@/lib/utils';

// Demo data — will be replaced by API
const weekData = [
  { day: 'Mon', amount: 520, budget: 600 },
  { day: 'Tue', amount: 340, budget: 600 },
  { day: 'Wed', amount: 780, budget: 600 },
  { day: 'Thu', amount: 420, budget: 600 },
  { day: 'Fri', amount: 610, budget: 600 },
  { day: 'Sat', amount: 290, budget: 600 },
  { day: 'Sun', amount: 700, budget: 600 },
];

const monthData = [
  { name: 'Food', value: 1400, color: '#22C55E' },
  { name: 'Transport', value: 620, color: '#3B82F6' },
  { name: 'Shopping', value: 540, color: '#F59E0B' },
  { name: 'Bills', value: 500, color: '#A855F7' },
  { name: 'Entertainment', value: 380, color: '#EC4899' },
  { name: 'Other', value: 220, color: '#6B7280' },
];

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="glass-strong rounded-btn px-3 py-2 text-xs shadow-lg">
      <p className="text-text-secondary">{label}</p>
      <p className="text-text-primary font-semibold tabular-nums">
        {formatCurrency(payload[0].value)}
      </p>
    </div>
  );
};

const PieTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="glass-strong rounded-btn px-3 py-2 text-xs shadow-lg">
      <p className="text-text-primary font-semibold">{payload[0].name}</p>
      <p className="tabular-nums" style={{ color: payload[0].payload.color }}>
        {formatCurrency(payload[0].value)}
      </p>
    </div>
  );
};

const CustomLegend = ({ payload }) => (
  <div className="flex flex-wrap justify-center gap-x-4 gap-y-1 mt-2">
    {payload?.map((entry) => (
      <div key={entry.value} className="flex items-center gap-1.5 text-xs">
        <span
          className="h-2 w-2 rounded-full"
          style={{ backgroundColor: entry.color }}
        />
        <span className="text-text-secondary">{entry.value}</span>
      </div>
    ))}
  </div>
);

const SpendingChart = memo(({ data, isLoading }) => {
  const [activeTab, setActiveTab] = useState('week');
  const totalSpent = useMemo(() => monthData.reduce((s, d) => s + d.value, 0), []);

  if (isLoading) return <SkeletonChart />;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: 0.1 }}
      className="bg-bg-card rounded-card border border-color-border p-5"
    >
      {/* Header + tabs */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-semibold text-text-primary">Spending</h3>
        <div className="flex bg-bg-primary rounded-btn p-0.5">
          {['week', 'month'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={cn(
                'px-3 py-1 text-xs font-medium rounded-md transition-all duration-200',
                activeTab === tab
                  ? 'bg-accent-green/15 text-accent-green'
                  : 'text-text-secondary hover:text-text-primary'
              )}
            >
              {tab === 'week' ? 'Week' : 'Month'}
            </button>
          ))}
        </div>
      </div>

      {/* Charts */}
      {activeTab === 'week' ? (
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={weekData} barCategoryGap="20%">
            <XAxis
              dataKey="day"
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#94A3B8', fontSize: 11 }}
            />
            <YAxis hide />
            <Tooltip content={<CustomTooltip />} cursor={false} />
            <Bar dataKey="amount" radius={[4, 4, 0, 0]}>
              {weekData.map((entry, i) => (
                <Cell
                  key={i}
                  fill={entry.amount > entry.budget ? '#EF4444' : '#22C55E'}
                  fillOpacity={0.85}
                />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      ) : (
        <div className="relative">
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie
                data={monthData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={85}
                paddingAngle={3}
                dataKey="value"
                stroke="none"
              >
                {monthData.map((entry) => (
                  <Cell key={entry.name} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip content={<PieTooltip />} />
              <Legend content={<CustomLegend />} />
            </PieChart>
          </ResponsiveContainer>
          {/* Center label */}
          <div className="absolute inset-0 flex items-center justify-center pointer-events-none" style={{ marginBottom: 30 }}>
            <div className="text-center">
              <p className="text-lg font-bold tabular-nums text-text-primary">
                {formatCurrency(totalSpent)}
              </p>
              <p className="text-[10px] text-text-secondary">total spent</p>
            </div>
          </div>
        </div>
      )}
    </motion.div>
  );
});

SpendingChart.displayName = 'SpendingChart';
export { SpendingChart };
