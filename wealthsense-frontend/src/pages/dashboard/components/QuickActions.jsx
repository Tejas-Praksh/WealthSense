import { memo } from 'react';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { Plus, Brain, BarChart3, Target } from 'lucide-react';
import { cn } from '@/lib/utils';
import { ROUTES } from '@/utils/constants';

const actions = [
  {
    label: 'Add Transaction',
    icon: Plus,
    emoji: '➕',
    color: 'text-accent-green',
    bg: 'bg-accent-green/10 hover:bg-accent-green/20 border-accent-green/20',
    route: ROUTES.TRANSACTIONS,
  },
  {
    label: 'Ask AI',
    icon: Brain,
    emoji: '🤖',
    color: 'text-purple-400',
    bg: 'bg-purple-500/10 hover:bg-purple-500/20 border-purple-500/20',
    route: ROUTES.AI_ADVISOR,
  },
  {
    label: 'Analytics',
    icon: BarChart3,
    emoji: '📊',
    color: 'text-accent-blue',
    bg: 'bg-accent-blue/10 hover:bg-accent-blue/20 border-accent-blue/20',
    route: ROUTES.TRANSACTIONS,
  },
  {
    label: 'Set Goal',
    icon: Target,
    emoji: '🎯',
    color: 'text-accent-amber',
    bg: 'bg-accent-amber/10 hover:bg-accent-amber/20 border-accent-amber/20',
    route: ROUTES.INVESTMENTS,
  },
];

const QuickActions = memo(() => {
  const navigate = useNavigate();

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: 0.5 }}
      className="grid grid-cols-4 gap-2 md:gap-3"
    >
      {actions.map((action, i) => {
        const Icon = action.icon;
        return (
          <motion.button
            key={action.label}
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.55 + i * 0.06 }}
            whileTap={{ scale: 0.95 }}
            onClick={() => navigate(action.route)}
            className={cn(
              'flex flex-col items-center gap-2 p-3 md:p-4 rounded-card border transition-all duration-200',
              action.bg
            )}
          >
            <Icon className={cn('h-5 w-5 md:h-6 md:w-6', action.color)} />
            <span className="text-[11px] md:text-xs font-medium text-text-primary whitespace-nowrap">
              {action.label}
            </span>
          </motion.button>
        );
      })}
    </motion.div>
  );
});

QuickActions.displayName = 'QuickActions';
export { QuickActions };
