import { useState, memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Pencil, Wallet, Trash2, PartyPopper } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { ProgressBar } from '@/components/ui/ProgressBar';
import { formatCurrency } from '@/utils/formatters';
import { cn } from '@/lib/utils';

const GOAL_ICONS = {
  '🚨': 'Emergency Fund', '📱': 'New Phone', '✈️': 'Travel',
  '🎓': 'Education', '🏠': 'House', '💍': 'Wedding',
  '🚗': 'Vehicle', '🏖️': 'Trip', '💰': 'Custom',
};

const DEMO_GOALS = [
  { id: '1', name: 'Goa Trip', emoji: '🏖️', target: 10000, current: 7800, monthly: 500, startDate: '2025-06-01', targetDate: '2026-06-01', status: 'on_track' },
  { id: '2', name: 'Emergency Fund', emoji: '🚨', target: 50000, current: 18000, monthly: 2000, startDate: '2025-01-01', targetDate: '2026-12-01', status: 'on_track' },
  { id: '3', name: 'New iPhone', emoji: '📱', target: 60000, current: 12000, monthly: 3000, startDate: '2025-09-01', targetDate: '2027-01-01', status: 'behind' },
  { id: '4', name: 'Europe Trip', emoji: '✈️', target: 200000, current: 35000, monthly: 5000, startDate: '2025-01-01', targetDate: '2028-06-01', status: 'on_track' },
];

const statusConfig = {
  on_track: { label: '✅ On Track', color: 'text-accent-green', barColor: 'green' },
  behind: { label: '⚠️ Slightly Behind', color: 'text-accent-amber', barColor: 'amber' },
  critical: { label: '🔴 At Risk', color: 'text-accent-red', barColor: 'red' },
  completed: { label: '🎉 Achieved!', color: 'text-accent-green', barColor: 'green' },
};

const GoalCard = memo(({ goal, index, onAddMoney }) => {
  const pct = Math.min((goal.current / goal.target) * 100, 100);
  const remaining = goal.target - goal.current;
  const monthsLeft = Math.ceil(remaining / goal.monthly);
  const config = statusConfig[goal.status] || statusConfig.on_track;
  const isComplete = pct >= 100;

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.08 }}
      className={cn(
        'bg-bg-card rounded-card border p-4',
        isComplete ? 'border-accent-green/30 bg-accent-green/[0.02]' : 'border-color-border'
      )}
    >
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center gap-2.5">
          <span className="text-xl">{goal.emoji}</span>
          <div>
            <p className="text-sm font-semibold text-text-primary">{goal.name}</p>
            <p className={cn('text-[10px] font-medium', config.color)}>{config.label}</p>
          </div>
        </div>
        <p className="text-xs font-bold tabular-nums text-text-primary">{Math.round(pct)}%</p>
      </div>

      <ProgressBar value={goal.current} max={goal.target} color={config.barColor} size="sm" className="mb-2" />

      <div className="flex items-center justify-between text-[10px] text-text-secondary mb-3">
        <span className="tabular-nums">{formatCurrency(goal.current)} of {formatCurrency(goal.target)}</span>
        <span>₹{goal.monthly.toLocaleString('en-IN')}/mo · {monthsLeft > 0 ? `${monthsLeft} months left` : 'Done!'}</span>
      </div>

      <div className="flex gap-2">
        <Button variant="outline" size="sm" className="flex-1" onClick={() => onAddMoney?.(goal)}>
          <Wallet className="h-3 w-3" /> Add Money
        </Button>
        <Button variant="ghost" size="sm">
          <Pencil className="h-3 w-3" />
        </Button>
      </div>
    </motion.div>
  );
});
GoalCard.displayName = 'GoalCard';

/* ── Add Goal Modal ──────────────────────────────── */
const AddGoalModal = memo(({ isOpen, onClose }) => {
  const [name, setName] = useState('');
  const [emoji, setEmoji] = useState('💰');
  const [target, setTarget] = useState('');
  const [monthly, setMonthly] = useState('');

  const monthsNeeded = target && monthly ? Math.ceil(Number(target) / Number(monthly)) : 0;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Set a New Goal" size="md">
      <div className="space-y-4">
        {/* Emoji picker */}
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-2 block">Choose Icon</label>
          <div className="flex flex-wrap gap-2">
            {Object.keys(GOAL_ICONS).map((e) => (
              <button
                key={e}
                onClick={() => setEmoji(e)}
                className={cn(
                  'h-9 w-9 rounded-md flex items-center justify-center text-lg transition-all',
                  emoji === e ? 'bg-accent-green/15 border border-accent-green/30 scale-110' : 'bg-bg-primary hover:bg-bg-secondary'
                )}
              >
                {e}
              </button>
            ))}
          </div>
        </div>

        {/* Name */}
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Goal Name</label>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Goa Trip"
            className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50"
          />
        </div>

        {/* Target */}
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Target Amount</label>
          <input
            type="number"
            value={target}
            onChange={(e) => setTarget(e.target.value)}
            placeholder="₹10,000"
            className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary tabular-nums placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50"
          />
        </div>

        {/* Monthly */}
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Monthly Saving</label>
          <input
            type="number"
            value={monthly}
            onChange={(e) => setMonthly(e.target.value)}
            placeholder="₹500"
            className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary tabular-nums placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50"
          />
        </div>

        {/* Auto-calc */}
        {monthsNeeded > 0 && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="bg-accent-green/5 border border-accent-green/20 rounded-btn p-3">
            <p className="text-xs text-accent-green font-medium">
              💡 You'll reach your goal in <strong>{monthsNeeded} months</strong> ({(monthsNeeded / 12).toFixed(1)} years)
            </p>
          </motion.div>
        )}

        <Button variant="primary" className="w-full" size="md" onClick={onClose}>
          Create Goal
        </Button>
      </div>
    </Modal>
  );
});
AddGoalModal.displayName = 'AddGoalModal';

/* ── Main MyGoals ────────────────────────────────── */
const MyGoals = memo(() => {
  const [showAddGoal, setShowAddGoal] = useState(false);

  const totalProgress = DEMO_GOALS.reduce((s, g) => s + g.current, 0);
  const totalTarget = DEMO_GOALS.reduce((s, g) => s + g.target, 0);

  return (
    <div className="space-y-5">
      {/* Header */}
      <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} className="bg-bg-card rounded-card border border-color-border p-4 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-text-primary">{DEMO_GOALS.length} Goals</p>
          <p className="text-xs text-text-secondary tabular-nums">
            {formatCurrency(totalProgress)} saved of {formatCurrency(totalTarget)} ({Math.round((totalProgress / totalTarget) * 100)}%)
          </p>
        </div>
        <Button variant="primary" size="sm" onClick={() => setShowAddGoal(true)}>
          <Plus className="h-3.5 w-3.5" /> New Goal
        </Button>
      </motion.div>

      {/* Goal cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {DEMO_GOALS.map((goal, i) => (
          <GoalCard key={goal.id} goal={goal} index={i} />
        ))}
      </div>

      <AddGoalModal isOpen={showAddGoal} onClose={() => setShowAddGoal(false)} />
    </div>
  );
});

MyGoals.displayName = 'MyGoals';
export { MyGoals };
