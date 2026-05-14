import { memo } from 'react';
import { motion } from 'framer-motion';
import { Shield, Calendar, ChevronRight, Lock, TrendingUp, Clock } from 'lucide-react';
import { ProgressBar } from '@/components/ui/ProgressBar';
import { Button } from '@/components/ui/Button';
import { formatCurrency } from '@/utils/formatters';
import { cn } from '@/lib/utils';

const SECTION_80C = {
  limit: 150000,
  items: [
    { name: 'EPF Contribution', amount: 43200, icon: '🏦' },
    { name: 'Life Insurance', amount: 12000, icon: '🛡️' },
    { name: 'ELSS Mutual Fund', amount: 24000, icon: '📈' },
    { name: 'PPF', amount: 15800, icon: '💰' },
  ],
};

const OPTIONS = [
  {
    name: 'ELSS Tax Saver Fund',
    desc: 'Highest returns, 3-year lock-in',
    returns: '14–16%',
    lockIn: '3 years',
    risk: 'High',
    riskColor: 'text-accent-red',
    recommended: true,
  },
  {
    name: 'Public Provident Fund (PPF)',
    desc: 'Safest government-backed option',
    returns: '7.1%',
    lockIn: '15 years',
    risk: 'Very Low',
    riskColor: 'text-accent-green',
  },
  {
    name: 'National Pension System (NPS)',
    desc: 'Extra ₹50,000 deduction under 80CCD',
    returns: '9–12%',
    lockIn: 'Till 60',
    risk: 'Medium',
    riskColor: 'text-accent-amber',
  },
];

const TAX_DATES = [
  { date: 'Mar 31, 2027', label: '80C Investment Deadline', icon: '🚨', urgent: true },
  { date: 'Jul 31, 2026', label: 'ITR Filing Deadline', icon: '📋', urgent: false },
  { date: 'Sep 15, 2026', label: 'Advance Tax Q2', icon: '💳', urgent: false },
];

const TaxSaving = memo(() => {
  const used = SECTION_80C.items.reduce((s, i) => s + i.amount, 0);
  const remaining = SECTION_80C.limit - used;
  const taxSaved = Math.round(used * 0.312);
  const potentialSave = Math.round(remaining * 0.312);

  return (
    <div className="space-y-5">
      {/* Hero */}
      <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }}
        className="bg-gradient-to-br from-green-500/5 to-blue-500/5 border border-accent-green/20 rounded-card p-5"
      >
        <div className="flex items-center gap-2 mb-2">
          <Shield className="h-5 w-5 text-accent-green" />
          <h3 className="text-base font-semibold text-text-primary">Tax Savings Dashboard</h3>
        </div>
        <p className="text-2xl font-bold text-accent-green tabular-nums mb-1">
          Save up to {formatCurrency(SECTION_80C.limit * 0.312)}
        </p>
        <p className="text-xs text-text-secondary">₹1.5L limit × 31.2% tax bracket (New Regime not applicable)</p>
      </motion.div>

      {/* 80C Progress */}
      <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}
        className="bg-bg-card rounded-card border border-color-border p-5"
      >
        <div className="flex items-center justify-between mb-3">
          <h4 className="text-sm font-semibold text-text-primary">Section 80C</h4>
          <span className="text-xs text-text-secondary tabular-nums">Limit: {formatCurrency(SECTION_80C.limit)}</span>
        </div>

        <ProgressBar value={used} max={SECTION_80C.limit} color={used >= SECTION_80C.limit ? 'green' : 'amber'} showLabel label={`${formatCurrency(used)} used`} size="md" className="mb-4" />

        <div className="space-y-2 mb-4">
          {SECTION_80C.items.map((item) => (
            <div key={item.name} className="flex items-center justify-between py-1.5 px-2 rounded-btn bg-bg-primary">
              <div className="flex items-center gap-2">
                <span className="text-sm">{item.icon}</span>
                <span className="text-xs text-text-primary">{item.name}</span>
              </div>
              <span className="text-xs font-medium tabular-nums text-text-primary">{formatCurrency(item.amount)}</span>
            </div>
          ))}
        </div>

        <div className="bg-accent-amber/5 border border-accent-amber/20 rounded-btn p-3">
          <p className="text-xs text-accent-amber font-medium">
            💡 Invest {formatCurrency(remaining)} more in 80C before 31st March to save {formatCurrency(potentialSave)} in taxes
          </p>
        </div>
      </motion.div>

      {/* Investment Options */}
      <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.15 }}>
        <h4 className="text-sm font-semibold text-text-primary mb-3">Investment Options to Fill 80C</h4>
        <div className="space-y-3">
          {OPTIONS.map((opt, i) => (
            <motion.div
              key={opt.name}
              initial={{ opacity: 0, x: 15 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.05 * i }}
              className={cn(
                'bg-bg-card rounded-card border p-4 flex items-center gap-4',
                opt.recommended ? 'border-accent-green/30 bg-accent-green/[0.02]' : 'border-color-border'
              )}
            >
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-0.5">
                  <p className="text-sm font-medium text-text-primary">{opt.name}</p>
                  {opt.recommended && (
                    <span className="px-1.5 py-0.5 rounded text-[9px] bg-accent-green/15 text-accent-green font-medium">Recommended</span>
                  )}
                </div>
                <p className="text-[10px] text-text-secondary mb-1.5">{opt.desc}</p>
                <div className="flex gap-3 text-[10px]">
                  <span className="flex items-center gap-1 text-text-secondary">
                    <TrendingUp className="h-2.5 w-2.5" /> {opt.returns}
                  </span>
                  <span className="flex items-center gap-1 text-text-secondary">
                    <Lock className="h-2.5 w-2.5" /> {opt.lockIn}
                  </span>
                  <span className={cn('font-medium', opt.riskColor)}>
                    {opt.risk} Risk
                  </span>
                </div>
              </div>
              <Button variant="outline" size="sm">
                Invest <ChevronRight className="h-3 w-3" />
              </Button>
            </motion.div>
          ))}
        </div>
      </motion.div>

      {/* Tax Calendar */}
      <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}
        className="bg-bg-card rounded-card border border-color-border p-5"
      >
        <div className="flex items-center gap-2 mb-3">
          <Calendar className="h-4 w-4 text-text-secondary" />
          <h4 className="text-sm font-semibold text-text-primary">Tax Calendar</h4>
        </div>
        <div className="space-y-2">
          {TAX_DATES.map((d) => (
            <div key={d.label} className={cn(
              'flex items-center justify-between py-2 px-3 rounded-btn',
              d.urgent ? 'bg-accent-red/5 border border-accent-red/20' : 'bg-bg-primary'
            )}>
              <div className="flex items-center gap-2">
                <span className="text-sm">{d.icon}</span>
                <span className="text-xs text-text-primary">{d.label}</span>
              </div>
              <span className={cn('text-[10px] font-medium tabular-nums', d.urgent ? 'text-accent-red' : 'text-text-secondary')}>
                {d.date}
              </span>
            </div>
          ))}
        </div>
      </motion.div>

      <p className="text-[9px] text-text-secondary/50 text-center pb-4">
        Tax calculations based on Old Tax Regime. Consult a CA for personalized advice.
      </p>
    </div>
  );
});

TaxSaving.displayName = 'TaxSaving';
export { TaxSaving };
