import { useState, useMemo, memo } from 'react';
import { motion } from 'framer-motion';
import {
  LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import { formatCurrency } from '@/utils/formatters';
import { cn } from '@/lib/utils';
import { SkeletonChart } from '@/components/ui/SkeletonLoader';

// Demo data
const dailyData = [
  { day: '1 May', spending: 620, income: 0 },
  { day: '2 May', spending: 340, income: 0 },
  { day: '3 May', spending: 180, income: 0 },
  { day: '4 May', spending: 920, income: 6000 },
  { day: '5 May', spending: 1250, income: 0 },
  { day: '6 May', spending: 450, income: 0 },
  { day: '7 May', spending: 780, income: 2500 },
  { day: '8 May', spending: 380, income: 0 },
  { day: '9 May', spending: 560, income: 0 },
  { day: '10 May', spending: 14999, income: 0 },
  { day: '11 May', spending: 210, income: 0 },
];

const categoryData = [
  { name: 'Food & Dining', value: 4200, color: '#F97316' },
  { name: 'Transport', value: 1850, color: '#3B82F6' },
  { name: 'Shopping', value: 3100, color: '#A855F7' },
  { name: 'Bills & Utilities', value: 2400, color: '#EAB308' },
  { name: 'Entertainment', value: 1600, color: '#EC4899' },
  { name: 'Other', value: 5270, color: '#6B7280' },
];

const VIEWS = [
  { key: 'line', label: 'Trend' },
  { key: 'category', label: 'Category' },
];

const ChartTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="glass-strong rounded-btn px-3 py-2 text-xs shadow-lg border border-color-border">
      <p className="text-text-secondary mb-1">{label}</p>
      {payload.map((p) => (
        <p key={p.dataKey} className="tabular-nums font-medium" style={{ color: p.color }}>
          {p.name}: {formatCurrency(p.value)}
        </p>
      ))}
    </div>
  );
};

const PieTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="glass-strong rounded-btn px-3 py-2 text-xs shadow-lg border border-color-border">
      <p className="font-medium text-text-primary">{payload[0].name}</p>
      <p className="tabular-nums" style={{ color: payload[0].payload.color }}>
        {formatCurrency(payload[0].value)}
      </p>
    </div>
  );
};

const CustomLegend = ({ payload }) => (
  <div className="flex flex-wrap justify-center gap-x-3 gap-y-1 mt-2">
    {payload?.map((entry) => (
      <span key={entry.value} className="flex items-center gap-1 text-[10px] text-text-secondary">
        <span className="h-2 w-2 rounded-full" style={{ backgroundColor: entry.color }} />
        {entry.value}
      </span>
    ))}
  </div>
);

const TransactionChart = memo(({ isLoading, onCategoryClick }) => {
  const [view, setView] = useState('line');
  const totalSpent = useMemo(() => categoryData.reduce((s, d) => s + d.value, 0), []);

  if (isLoading) return <SkeletonChart className="h-72" />;

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: 0.15 }}
      className="bg-bg-card rounded-card border border-color-border p-5"
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-semibold text-text-primary">Spending Analytics</h3>
        <div className="flex bg-bg-primary rounded-btn p-0.5">
          {VIEWS.map((v) => (
            <button
              key={v.key}
              onClick={() => setView(v.key)}
              className={cn(
                'px-3 py-1 text-xs font-medium rounded-md transition-all',
                view === v.key
                  ? 'bg-accent-green/15 text-accent-green'
                  : 'text-text-secondary hover:text-text-primary'
              )}
            >
              {v.label}
            </button>
          ))}
        </div>
      </div>

      {/* Charts */}
      {view === 'line' ? (
        <ResponsiveContainer width="100%" height={220}>
          <LineChart data={dailyData}>
            <XAxis dataKey="day" axisLine={false} tickLine={false} tick={{ fill: '#94A3B8', fontSize: 10 }} />
            <YAxis hide />
            <Tooltip content={<ChartTooltip />} />
            <Line
              type="monotone"
              dataKey="spending"
              name="Spending"
              stroke="#EF4444"
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 4, fill: '#EF4444' }}
            />
            <Line
              type="monotone"
              dataKey="income"
              name="Income"
              stroke="#22C55E"
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 4, fill: '#22C55E' }}
            />
          </LineChart>
        </ResponsiveContainer>
      ) : (
        <div className="relative">
          <ResponsiveContainer width="100%" height={240}>
            <PieChart>
              <Pie
                data={categoryData}
                cx="50%"
                cy="50%"
                innerRadius={55}
                outerRadius={80}
                paddingAngle={3}
                dataKey="value"
                stroke="none"
                onClick={(data) => onCategoryClick?.(data.name)}
                style={{ cursor: 'pointer' }}
              >
                {categoryData.map((entry) => (
                  <Cell key={entry.name} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip content={<PieTooltip />} />
              <Legend content={<CustomLegend />} />
            </PieChart>
          </ResponsiveContainer>
          <div className="absolute inset-0 flex items-center justify-center pointer-events-none" style={{ marginBottom: 30 }}>
            <div className="text-center">
              <p className="text-base font-bold tabular-nums text-text-primary">{formatCurrency(totalSpent)}</p>
              <p className="text-[9px] text-text-secondary">total</p>
            </div>
          </div>
        </div>
      )}
    </motion.div>
  );
});

TransactionChart.displayName = 'TransactionChart';
export { TransactionChart };
