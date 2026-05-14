import { useState, useMemo, useCallback, memo } from 'react';
import { motion } from 'framer-motion';
import { useSelector, useDispatch } from 'react-redux';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { Calculator, IndianRupee, TrendingUp, ChevronRight } from 'lucide-react';
import { setSipInput } from '@/store/slices/investmentSlice';
import { formatCurrency, formatCompactCurrency, formatPercent } from '@/utils/formatters';
import { Button } from '@/components/ui/Button';
import { cn } from '@/lib/utils';

/* ── SIP Formula ─────────────────────────────────── */
function calculateSIP(monthly, rate, years) {
  const monthlyRate = rate / 100 / 12;
  const months = years * 12;
  if (monthlyRate === 0) {
    return { futureValue: monthly * months, totalInvested: monthly * months, returns: 0, growthPercent: 0 };
  }
  const futureValue = monthly * ((Math.pow(1 + monthlyRate, months) - 1) / monthlyRate) * (1 + monthlyRate);
  const totalInvested = monthly * months;
  const returns = futureValue - totalInvested;
  const growthPercent = (returns / totalInvested) * 100;
  return { futureValue: Math.round(futureValue), totalInvested, returns: Math.round(returns), growthPercent };
}

/* ── Chart data builder ──────────────────────────── */
function buildChartData(monthly, rate, years) {
  const data = [];
  const monthlyRate = rate / 100 / 12;
  for (let y = 0; y <= years; y++) {
    const months = y * 12;
    const invested = monthly * months;
    let value;
    if (monthlyRate === 0 || months === 0) {
      value = invested;
    } else {
      value = monthly * ((Math.pow(1 + monthlyRate, months) - 1) / monthlyRate) * (1 + monthlyRate);
    }
    data.push({ year: `${y}yr`, invested: Math.round(invested), value: Math.round(value) });
  }
  return data;
}

/* ── Tooltip ─────────────────────────────────────── */
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

/* ── Quick select chip ───────────────────────────── */
const Chip = ({ value, label, selected, onClick }) => (
  <button
    onClick={() => onClick(value)}
    className={cn(
      'px-2.5 py-1 rounded-md text-xs font-medium transition-all',
      selected
        ? 'bg-accent-green/15 text-accent-green border border-accent-green/20'
        : 'bg-bg-primary text-text-secondary hover:text-text-primary border border-transparent'
    )}
  >
    {label}
  </button>
);

/* ── Slider ──────────────────────────────────────── */
const Slider = memo(({ min, max, step, value, onChange, label, icon: Icon, formatValue }) => (
  <div>
    <div className="flex items-center justify-between mb-2">
      <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium flex items-center gap-1.5">
        {Icon && <Icon className="h-3 w-3" />} {label}
      </label>
      <span className="text-sm font-bold text-accent-green tabular-nums">{formatValue ? formatValue(value) : value}</span>
    </div>
    <input
      type="range"
      min={min}
      max={max}
      step={step}
      value={value}
      onChange={(e) => onChange(Number(e.target.value))}
      className="w-full h-1.5 rounded-full appearance-none cursor-pointer bg-bg-primary
        [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:h-4 [&::-webkit-slider-thumb]:w-4 [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-accent-green [&::-webkit-slider-thumb]:shadow-md [&::-webkit-slider-thumb]:border-2 [&::-webkit-slider-thumb]:border-bg-primary
        [&::-moz-range-thumb]:h-4 [&::-moz-range-thumb]:w-4 [&::-moz-range-thumb]:rounded-full [&::-moz-range-thumb]:bg-accent-green [&::-moz-range-thumb]:border-2 [&::-moz-range-thumb]:border-bg-primary"
    />
  </div>
));
Slider.displayName = 'Slider';

/* ── Main SIP Calculator ─────────────────────────── */
const SIPCalculator = memo(() => {
  const dispatch = useDispatch();
  const { monthly, rate, years } = useSelector((s) => s.investments.sipCalculation);

  const setMonthly = useCallback((v) => dispatch(setSipInput({ monthly: v })), [dispatch]);
  const setRate = useCallback((v) => dispatch(setSipInput({ rate: v })), [dispatch]);
  const setYears = useCallback((v) => dispatch(setSipInput({ years: v })), [dispatch]);

  const result = useMemo(() => calculateSIP(monthly, rate, years), [monthly, rate, years]);
  const chartData = useMemo(() => buildChartData(monthly, rate, years), [monthly, rate, years]);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
      {/* Left — Inputs */}
      <motion.div initial={{ opacity: 0, x: -15 }} animate={{ opacity: 1, x: 0 }} className="bg-bg-card rounded-card border border-color-border p-5 space-y-6">
        <div className="flex items-center gap-2">
          <Calculator className="h-4 w-4 text-accent-green" />
          <h3 className="text-sm font-semibold text-text-primary">SIP Calculator</h3>
        </div>

        {/* Monthly Investment */}
        <div className="space-y-2">
          <Slider
            min={500} max={50000} step={500}
            value={monthly} onChange={setMonthly}
            label="Monthly Investment" icon={IndianRupee}
            formatValue={(v) => `₹${v.toLocaleString('en-IN')}`}
          />
          <div className="flex flex-wrap gap-1.5">
            {[500, 1000, 2000, 5000, 10000].map((v) => (
              <Chip key={v} value={v} label={`₹${v.toLocaleString('en-IN')}`} selected={monthly === v} onClick={setMonthly} />
            ))}
          </div>
        </div>

        {/* Expected Return */}
        <div className="space-y-2">
          <Slider
            min={6} max={20} step={0.5}
            value={rate} onChange={setRate}
            label="Expected Return Rate" icon={TrendingUp}
            formatValue={(v) => `${v}%`}
          />
          <div className="flex flex-col gap-1 text-[9px] text-text-secondary pl-1">
            <span>Large Cap funds: ~12% · Index funds: ~13% · Small Cap: ~16%</span>
          </div>
        </div>

        {/* Time Period */}
        <div className="space-y-2">
          <Slider
            min={1} max={30} step={1}
            value={years} onChange={setYears}
            label="Time Period"
            formatValue={(v) => `${v} year${v > 1 ? 's' : ''}`}
          />
          <div className="flex flex-wrap gap-1.5">
            {[3, 5, 10, 20, 30].map((v) => (
              <Chip key={v} value={v} label={`${v}yr`} selected={years === v} onClick={setYears} />
            ))}
          </div>
        </div>
      </motion.div>

      {/* Right — Results */}
      <motion.div initial={{ opacity: 0, x: 15 }} animate={{ opacity: 1, x: 0 }} className="space-y-4">
        {/* Big result card */}
        <div className="bg-gradient-to-br from-accent-green/5 to-accent-blue/5 rounded-card border border-accent-green/20 p-5">
          <p className="text-xs text-text-secondary mb-1">In {years} years you'll have</p>
          <motion.p
            key={result.futureValue}
            initial={{ scale: 0.95 }}
            animate={{ scale: 1 }}
            className="text-3xl font-bold tabular-nums text-accent-green mb-1"
          >
            {formatCurrency(result.futureValue)}
          </motion.p>
          <p className="text-sm text-text-secondary mb-4">({formatCompactCurrency(result.futureValue)})</p>

          <div className="grid grid-cols-3 gap-3">
            <div className="bg-bg-primary/50 rounded-btn p-2.5">
              <p className="text-[9px] text-text-secondary">Invested</p>
              <p className="text-sm font-bold tabular-nums text-accent-blue">{formatCurrency(result.totalInvested)}</p>
            </div>
            <div className="bg-bg-primary/50 rounded-btn p-2.5">
              <p className="text-[9px] text-text-secondary">Wealth Gain</p>
              <p className="text-sm font-bold tabular-nums text-accent-green">{formatCurrency(result.returns)}</p>
            </div>
            <div className="bg-bg-primary/50 rounded-btn p-2.5">
              <p className="text-[9px] text-text-secondary">Growth</p>
              <p className="text-sm font-bold tabular-nums text-accent-amber">{formatPercent(result.growthPercent)}</p>
            </div>
          </div>
        </div>

        {/* Wealth growth chart */}
        <div className="bg-bg-card rounded-card border border-color-border p-5">
          <h4 className="text-xs font-medium text-text-secondary mb-3">Wealth Growth Over Time</h4>
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={chartData}>
              <defs>
                <linearGradient id="sipInvGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#3B82F6" stopOpacity={0.4} />
                  <stop offset="100%" stopColor="#3B82F6" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="sipValGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#22C55E" stopOpacity={0.4} />
                  <stop offset="100%" stopColor="#22C55E" stopOpacity={0} />
                </linearGradient>
              </defs>
              <XAxis dataKey="year" axisLine={false} tickLine={false} tick={{ fill: '#94A3B8', fontSize: 10 }} interval={Math.max(0, Math.floor(years / 6) - 1)} />
              <YAxis hide />
              <Tooltip content={<ChartTooltip />} />
              <Area type="monotone" dataKey="invested" name="Invested" stroke="#3B82F6" strokeWidth={2} fill="url(#sipInvGrad)" />
              <Area type="monotone" dataKey="value" name="Total Value" stroke="#22C55E" strokeWidth={2} fill="url(#sipValGrad)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <Button variant="primary" className="w-full" size="lg">
          <ChevronRight className="h-4 w-4" /> Start SIP
        </Button>

        <p className="text-[9px] text-text-secondary/50 text-center">
          Returns are estimated based on historical performance. Actual returns may vary.
        </p>
      </motion.div>
    </div>
  );
});

SIPCalculator.displayName = 'SIPCalculator';
export { SIPCalculator };
