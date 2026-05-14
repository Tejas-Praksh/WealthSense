import { memo, useMemo } from 'react';
import { motion } from 'framer-motion';
import { TrendingUp, TrendingDown, ArrowDownLeft, ArrowUpRight, Hash, CreditCard } from 'lucide-react';
import { formatCurrency } from '@/utils/formatters';
import { Skeleton } from '@/components/ui/SkeletonLoader';

const demoStats = {
  totalSpent: { amount: 18420, change: -12, label: 'Total Spent', icon: ArrowUpRight, color: 'text-accent-red', bgColor: 'bg-accent-red/10' },
  totalReceived: { amount: 26000, change: 8, label: 'Total Received', icon: ArrowDownLeft, color: 'text-accent-green', bgColor: 'bg-accent-green/10' },
  largest: { amount: 14999, merchant: 'Unknown UPI — Dubai', label: 'Largest Transaction', icon: CreditCard, color: 'text-accent-amber', bgColor: 'bg-accent-amber/10' },
  count: { amount: 47, change: 15, label: 'Transactions', icon: Hash, color: 'text-accent-blue', bgColor: 'bg-accent-blue/10' },
};

function AnimatedNumber({ value, isCurrency = true }) {
  return (
    <span className="tabular-nums">
      {isCurrency ? formatCurrency(value) : value}
    </span>
  );
}

const StatCard = memo(({ stat, index }) => {
  const Icon = stat.icon;
  const isPositive = stat.change > 0;

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.08 }}
      className="bg-bg-card rounded-card border border-color-border p-4"
    >
      <div className="flex items-center justify-between mb-2">
        <span className="text-xs text-text-secondary font-medium">{stat.label}</span>
        <div className={`p-1.5 rounded-md ${stat.bgColor}`}>
          <Icon className={`h-3.5 w-3.5 ${stat.color}`} />
        </div>
      </div>
      <p className={`text-xl font-bold ${stat.color}`}>
        <AnimatedNumber value={stat.amount} isCurrency={stat.label !== 'Transactions'} />
      </p>
      {stat.merchant ? (
        <p className="text-[10px] text-text-secondary mt-1 truncate">{stat.merchant}</p>
      ) : stat.change !== undefined ? (
        <div className="flex items-center gap-1 mt-1">
          {isPositive ? (
            <TrendingUp className="h-3 w-3 text-accent-green" />
          ) : (
            <TrendingDown className="h-3 w-3 text-accent-red" />
          )}
          <span className={`text-[10px] font-medium ${isPositive ? 'text-accent-green' : 'text-accent-red'}`}>
            {isPositive ? '+' : ''}{stat.change}% vs last month
          </span>
        </div>
      ) : null}
    </motion.div>
  );
});

StatCard.displayName = 'StatCard';

const TransactionStats = memo(({ stats, isLoading }) => {
  const items = useMemo(() => {
    if (stats) return Object.values(stats);
    return Object.values(demoStats);
  }, [stats]);

  if (isLoading) {
    return (
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {[0, 1, 2, 3].map((i) => (
          <div key={i} className="bg-bg-card rounded-card border border-color-border p-4 space-y-2">
            <Skeleton className="h-3 w-20" />
            <Skeleton className="h-6 w-28" />
            <Skeleton className="h-3 w-24" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
      {items.map((stat, i) => (
        <StatCard key={stat.label} stat={stat} index={i} />
      ))}
    </div>
  );
});

TransactionStats.displayName = 'TransactionStats';
export { TransactionStats };
