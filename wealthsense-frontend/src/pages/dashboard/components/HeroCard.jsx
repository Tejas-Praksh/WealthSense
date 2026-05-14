import { useState, useEffect, useRef, memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Wallet, TrendingDown, Target, AlertTriangle, X } from 'lucide-react';
import { formatCurrency } from '@/utils/formatters';
import { SkeletonCard } from '@/components/ui/SkeletonLoader';

// Animated counter hook
function useCountUp(target, duration = 1000) {
  const [value, setValue] = useState(0);
  const rafRef = useRef(null);

  useEffect(() => {
    if (target === null || target === undefined) return;
    const start = performance.now();
    const from = 0;
    const to = target;

    function tick(now) {
      const elapsed = now - start;
      const progress = Math.min(elapsed / duration, 1);
      // easeOutCubic
      const eased = 1 - Math.pow(1 - progress, 3);
      setValue(from + (to - from) * eased);
      if (progress < 1) {
        rafRef.current = requestAnimationFrame(tick);
      }
    }

    rafRef.current = requestAnimationFrame(tick);
    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
    };
  }, [target, duration]);

  return Math.round(value);
}

const miniStats = [
  { label: 'Earned', icon: Wallet, key: 'earned', emoji: '💰' },
  { label: 'Spent', icon: TrendingDown, key: 'spent', emoji: '💸' },
  { label: 'Saved', icon: Target, key: 'saved', emoji: '🎯' },
];

const HeroCard = memo(({ data, isLoading, fraudAlert, onDismissAlert }) => {
  const animatedBalance = useCountUp(data?.remainingBudget ?? 0, 1200);

  if (isLoading) return <SkeletonCard className="h-52" />;

  const balance = data?.remainingBudget ?? 2340;
  const earned = data?.totalIncome ?? 6000;
  const spent = data?.totalExpense ?? 3660;
  const saved = data?.totalSaved ?? 500;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
      className="relative overflow-hidden rounded-card border border-color-border"
      style={{
        background: 'linear-gradient(135deg, #0F172A 0%, #1E3A5F 100%)',
      }}
    >
      {/* Subtle grid overlay */}
      <div
        className="absolute inset-0 opacity-[0.03] pointer-events-none"
        style={{
          backgroundImage: 'radial-gradient(circle, #22C55E 1px, transparent 1px)',
          backgroundSize: '24px 24px',
        }}
      />

      {/* Fraud Alert Banner */}
      <AnimatePresence>
        {fraudAlert && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="bg-accent-red/15 border-b border-accent-red/30 overflow-hidden"
          >
            <div className="flex items-center justify-between px-5 py-2.5">
              <div className="flex items-center gap-2 text-accent-red text-sm font-medium">
                <AlertTriangle className="h-4 w-4" />
                <span>⚠️ Suspicious transaction detected — review now</span>
              </div>
              <button
                onClick={onDismissAlert}
                className="text-accent-red/60 hover:text-accent-red transition-colors"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="relative z-10 p-6 md:p-8">
        {/* Main balance */}
        <div className="text-center mb-6">
          <p className="text-text-secondary text-sm mb-1">Remaining this month</p>
          <motion.p
            className="text-5xl md:text-[56px] font-bold tabular-nums tracking-tight"
            style={{ color: '#22C55E' }}
            key={balance}
          >
            {formatCurrency(animatedBalance)}
          </motion.p>
          <p className="text-text-secondary text-sm mt-1.5">left to spend</p>
        </div>

        {/* Mini stats row */}
        <div className="grid grid-cols-3 gap-3 md:gap-4">
          {miniStats.map((stat) => {
            const val = stat.key === 'earned' ? earned : stat.key === 'spent' ? spent : saved;
            return (
              <motion.div
                key={stat.key}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.3 + miniStats.indexOf(stat) * 0.1 }}
                className="bg-white/[0.04] rounded-btn p-3 text-center border border-white/[0.06]"
              >
                <span className="text-base mr-1">{stat.emoji}</span>
                <span className="text-xs text-text-secondary">{stat.label}</span>
                <p className="text-sm md:text-base font-semibold text-text-primary tabular-nums mt-0.5">
                  {formatCurrency(val)}
                </p>
              </motion.div>
            );
          })}
        </div>
      </div>
    </motion.div>
  );
});

HeroCard.displayName = 'HeroCard';
export { HeroCard };
