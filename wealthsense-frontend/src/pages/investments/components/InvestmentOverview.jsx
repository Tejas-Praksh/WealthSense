import { memo, useMemo } from 'react';
import { motion } from 'framer-motion';
import {
  AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell,
} from 'recharts';
import { TrendingUp, TrendingDown, PiggyBank, Plus } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { formatCurrency, formatPercent, formatCompactCurrency } from '@/utils/formatters';
import { cn } from '@/lib/utils';

/* ── Demo Data ─────────────────────────────────── */
const DEMO_INVESTMENTS = [
  { id: '1', name: 'Nifty 50 Index Fund', type: 'SIP', monthlySIP: 2000, totalInvested: 24000, currentValue: 27360, returnsPct: 14, startDate: '2025-11-01', status: 'ACTIVE' },
  { id: '2', name: 'Parag Parikh Flexi Cap', type: 'SIP', monthlySIP: 1000, totalInvested: 12000, currentValue: 13200, returnsPct: 10, startDate: '2025-11-01', status: 'ACTIVE' },
  { id: '3', name: 'PPF Account', type: 'PPF', monthlySIP: 500, totalInvested: 15800, currentValue: 16922, returnsPct: 7.1, startDate: '2024-04-01', status: 'ACTIVE' },
  { id: '4', name: 'Axis ELSS Tax Saver', type: 'ELSS', monthlySIP: 500, totalInvested: 6000, currentValue: 6720, returnsPct: 12, startDate: '2025-09-01', status: 'ACTIVE' },
  { id: '5', name: 'FD — SBI 1 Year', type: 'FD', monthlySIP: 0, totalInvested: 10000, currentValue: 10700, returnsPct: 7.0, startDate: '2025-06-01', status: 'COMPLETED' },
];

const DISTRIBUTION = [
  { name: 'SIP', value: 40560, color: '#22C55E' },
  { name: 'PPF', value: 16922, color: '#3B82F6' },
  { name: 'FD', value: 10700, color: '#EAB308' },
  { name: 'ELSS', value: 6720, color: '#A855F7' },
];

const GROWTH_DATA = Array.from({ length: 12 }, (_, i) => ({
  month: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'][i],
  invested: 56000 + i * 4000,
  value: 56000 + i * 4000 + (i * i * 200),
}));

const typeBadgeMap = {
  SIP: 'SUCCESS', ELSS: 'INFO', PPF: 'WARNING', FD: 'PENDING', 'Lump Sum': 'DEFAULT',
};

/* ── Chart tooltip ─────────────────────────────── */
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
      <p className="tabular-nums" style={{ color: payload[0].payload.color }}>{formatCurrency(payload[0].value)}</p>
    </div>
  );
};

/* ── Component ─────────────────────────────────── */
const InvestmentOverview = memo(({ onStartSIP }) => {
  const totals = useMemo(() => {
    const invested = DEMO_INVESTMENTS.reduce((s, i) => s + i.totalInvested, 0);
    const current = DEMO_INVESTMENTS.reduce((s, i) => s + i.currentValue, 0);
    const returns = current - invested;
    const pct = (returns / invested) * 100;
    return { invested, current, returns, pct };
  }, []);

  const totalDist = DISTRIBUTION.reduce((s, d) => s + d.value, 0);

  return (
    <div className="space-y-5">
      {/* Portfolio Summary */}
      <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} className="bg-bg-card rounded-card border border-color-border p-5">
        <h3 className="text-sm font-medium text-text-secondary mb-4">Portfolio Summary</h3>
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <p className="text-[10px] uppercase text-text-secondary mb-0.5">Total Invested</p>
            <p className="text-lg font-bold tabular-nums text-text-primary">{formatCurrency(totals.invested)}</p>
          </div>
          <div>
            <p className="text-[10px] uppercase text-text-secondary mb-0.5">Current Value</p>
            <p className="text-lg font-bold tabular-nums text-accent-green">{formatCurrency(totals.current)}</p>
          </div>
          <div>
            <p className="text-[10px] uppercase text-text-secondary mb-0.5">Returns</p>
            <div className="flex items-center gap-1.5">
              <p className={cn('text-lg font-bold tabular-nums', totals.returns >= 0 ? 'text-accent-green' : 'text-accent-red')}>
                {totals.returns >= 0 ? '+' : ''}{formatCurrency(totals.returns)}
              </p>
              {totals.pct >= 0 ? <TrendingUp className="h-4 w-4 text-accent-green" /> : <TrendingDown className="h-4 w-4 text-accent-red" />}
            </div>
          </div>
          <div>
            <p className="text-[10px] uppercase text-text-secondary mb-0.5">Growth</p>
            <p className={cn('text-lg font-bold tabular-nums', totals.pct >= 0 ? 'text-accent-green' : 'text-accent-red')}>
              {formatPercent(totals.pct)}
            </p>
          </div>
        </div>
      </motion.div>

      {/* Charts row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Growth chart */}
        <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }} className="lg:col-span-2 bg-bg-card rounded-card border border-color-border p-5">
          <h3 className="text-sm font-medium text-text-secondary mb-3">Portfolio Growth</h3>
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={GROWTH_DATA}>
              <defs>
                <linearGradient id="investedGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#3B82F6" stopOpacity={0.3} />
                  <stop offset="100%" stopColor="#3B82F6" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="valueGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#22C55E" stopOpacity={0.3} />
                  <stop offset="100%" stopColor="#22C55E" stopOpacity={0} />
                </linearGradient>
              </defs>
              <XAxis dataKey="month" axisLine={false} tickLine={false} tick={{ fill: '#94A3B8', fontSize: 10 }} />
              <YAxis hide />
              <Tooltip content={<ChartTooltip />} />
              <Area type="monotone" dataKey="invested" name="Invested" stroke="#3B82F6" strokeWidth={2} fill="url(#investedGrad)" />
              <Area type="monotone" dataKey="value" name="Value" stroke="#22C55E" strokeWidth={2} fill="url(#valueGrad)" />
            </AreaChart>
          </ResponsiveContainer>
        </motion.div>

        {/* Distribution donut */}
        <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.15 }} className="bg-bg-card rounded-card border border-color-border p-5 relative">
          <h3 className="text-sm font-medium text-text-secondary mb-3">Distribution</h3>
          <ResponsiveContainer width="100%" height={160}>
            <PieChart>
              <Pie data={DISTRIBUTION} cx="50%" cy="50%" innerRadius={40} outerRadius={60} paddingAngle={3} dataKey="value" stroke="none">
                {DISTRIBUTION.map((d) => <Cell key={d.name} fill={d.color} />)}
              </Pie>
              <Tooltip content={<PieTooltip />} />
            </PieChart>
          </ResponsiveContainer>
          <div className="absolute inset-0 flex items-center justify-center pointer-events-none" style={{ marginTop: 20 }}>
            <div className="text-center">
              <p className="text-sm font-bold tabular-nums text-text-primary">{formatCompactCurrency(totalDist)}</p>
              <p className="text-[8px] text-text-secondary">total</p>
            </div>
          </div>
          <div className="flex flex-wrap justify-center gap-x-3 gap-y-1 mt-2">
            {DISTRIBUTION.map((d) => (
              <span key={d.name} className="flex items-center gap-1 text-[10px] text-text-secondary">
                <span className="h-2 w-2 rounded-full" style={{ backgroundColor: d.color }} />
                {d.name}
              </span>
            ))}
          </div>
        </motion.div>
      </div>

      {/* Investment list */}
      <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-medium text-text-secondary">Your Investments</h3>
          <Button variant="ghost" size="sm" onClick={onStartSIP}>
            <Plus className="h-3.5 w-3.5" /> Add
          </Button>
        </div>
        <div className="space-y-2">
          {DEMO_INVESTMENTS.map((inv, i) => (
            <motion.div
              key={inv.id}
              initial={{ opacity: 0, x: 15 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.05 * i }}
              className="bg-bg-card rounded-card border border-color-border p-4 flex items-center gap-4"
            >
              <div className="h-10 w-10 rounded-full bg-accent-green/10 flex items-center justify-center flex-shrink-0">
                <PiggyBank className="h-5 w-5 text-accent-green" />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-0.5">
                  <p className="text-sm font-medium text-text-primary truncate">{inv.name}</p>
                  <Badge type={typeBadgeMap[inv.type] || 'DEFAULT'}>{inv.type}</Badge>
                </div>
                <p className="text-[10px] text-text-secondary">
                  {inv.monthlySIP > 0 ? `₹${inv.monthlySIP.toLocaleString('en-IN')}/mo` : 'One-time'} · Since {new Date(inv.startDate).toLocaleDateString('en-IN', { month: 'short', year: 'numeric' })}
                </p>
              </div>
              <div className="text-right flex-shrink-0">
                <p className="text-sm font-bold tabular-nums text-text-primary">{formatCurrency(inv.currentValue)}</p>
                <p className={cn('text-[10px] tabular-nums font-medium', inv.returnsPct >= 0 ? 'text-accent-green' : 'text-accent-red')}>
                  {formatPercent(inv.returnsPct)} · {formatCurrency(inv.currentValue - inv.totalInvested)}
                </p>
              </div>
            </motion.div>
          ))}
        </div>
      </motion.div>

      {/* Disclaimer */}
      <p className="text-[9px] text-text-secondary/50 text-center pt-2 pb-4">
        Investment recommendations are for educational purposes only. Please consult a SEBI registered advisor before investing.
      </p>
    </div>
  );
});

InvestmentOverview.displayName = 'InvestmentOverview';
export { InvestmentOverview };
