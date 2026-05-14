import { memo } from 'react';
import { motion } from 'framer-motion';
import { ProgressBar } from '@/components/ui/ProgressBar';
import { formatCurrency } from '@/utils/formatters';
import { Skeleton } from '@/components/ui/SkeletonLoader';

const demoGoals = [
  {
    id: '1',
    name: 'Emergency Fund',
    emoji: '🛡️',
    current: 3400,
    target: 10000,
    daysRemaining: 142,
    onTrack: true,
  },
  {
    id: '2',
    name: 'New Laptop',
    emoji: '💻',
    current: 8200,
    target: 45000,
    daysRemaining: 280,
    onTrack: false,
  },
  {
    id: '3',
    name: 'Goa Trip',
    emoji: '🏖️',
    current: 4500,
    target: 6000,
    daysRemaining: 45,
    onTrack: true,
  },
];

const GoalsProgress = memo(({ goals, isLoading }) => {
  const items = goals?.length ? goals : demoGoals;

  if (isLoading) {
    return (
      <div className="bg-bg-card rounded-card border border-color-border p-5 space-y-4">
        <Skeleton className="h-5 w-32" />
        {[0, 1, 2].map((i) => (
          <div key={i} className="space-y-2">
            <Skeleton className="h-3.5 w-40" />
            <Skeleton className="h-2 w-full" />
            <Skeleton className="h-3 w-28" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: 0.4 }}
      className="bg-bg-card rounded-card border border-color-border p-5"
    >
      <h3 className="text-sm font-semibold text-text-primary mb-4">Savings Goals</h3>

      <div className="space-y-4">
        {items.map((goal, i) => {
          const percentage = Math.round((goal.current / goal.target) * 100);
          return (
            <motion.div
              key={goal.id}
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.5 + i * 0.08 }}
            >
              <div className="flex items-center justify-between mb-1.5">
                <div className="flex items-center gap-2">
                  <span className="text-base">{goal.emoji}</span>
                  <span className="text-sm font-medium text-text-primary">{goal.name}</span>
                </div>
                <span className="text-xs text-text-secondary">
                  {goal.onTrack ? (
                    <span className="text-accent-green">On track ✅</span>
                  ) : (
                    <span className="text-accent-amber">Behind ⚠️</span>
                  )}
                </span>
              </div>

              <ProgressBar
                value={goal.current}
                max={goal.target}
                color={goal.onTrack ? 'green' : 'amber'}
                size="sm"
              />

              <div className="flex items-center justify-between mt-1">
                <p className="text-xs text-text-secondary tabular-nums">
                  {formatCurrency(goal.current)} of {formatCurrency(goal.target)}
                </p>
                <p className="text-[10px] text-text-secondary">
                  {goal.daysRemaining}d left
                </p>
              </div>
            </motion.div>
          );
        })}
      </div>
    </motion.div>
  );
});

GoalsProgress.displayName = 'GoalsProgress';
export { GoalsProgress };
