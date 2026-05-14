import { memo, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown, ChevronUp } from 'lucide-react';
import { formatCurrency } from '@/utils/formatters';

const contextItems = [
  { label: 'This month spent', value: 3660, color: 'text-accent-red' },
  { label: 'Top category', value: 'Food (₹1,200)', color: 'text-accent-amber', isText: true },
  { label: 'Savings this month', value: 500, color: 'text-accent-green' },
  { label: 'Transactions analyzed', value: 47, color: 'text-accent-blue', isText: true },
];

const FinancialContextCard = memo(() => {
  const [collapsed, setCollapsed] = useState(true);

  return (
    <motion.div
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      className="bg-bg-card border border-color-border rounded-card overflow-hidden"
    >
      <button
        onClick={() => setCollapsed(!collapsed)}
        className="w-full flex items-center justify-between px-4 py-2.5 text-left"
      >
        <span className="text-[10px] uppercase tracking-wider text-text-secondary font-medium">
          🧠 AI knows your finances
        </span>
        {collapsed ? (
          <ChevronDown className="h-3.5 w-3.5 text-text-secondary" />
        ) : (
          <ChevronUp className="h-3.5 w-3.5 text-text-secondary" />
        )}
      </button>

      <AnimatePresence>
        {!collapsed && (
          <motion.div
            initial={{ height: 0 }}
            animate={{ height: 'auto' }}
            exit={{ height: 0 }}
            className="overflow-hidden"
          >
            <div className="grid grid-cols-2 gap-2 px-4 pb-3">
              {contextItems.map((item) => (
                <div key={item.label} className="bg-bg-primary rounded-btn px-2.5 py-1.5">
                  <p className="text-[9px] text-text-secondary">{item.label}</p>
                  <p className={`text-xs font-semibold tabular-nums ${item.color}`}>
                    {item.isText ? item.value : formatCurrency(item.value)}
                  </p>
                </div>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
});

FinancialContextCard.displayName = 'FinancialContextCard';
export { FinancialContextCard };
