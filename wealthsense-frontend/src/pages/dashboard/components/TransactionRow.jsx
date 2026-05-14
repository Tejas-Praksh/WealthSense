import { memo } from 'react';
import { motion } from 'framer-motion';
import { cn } from '@/lib/utils';
import { Badge } from '@/components/ui/Badge';
import { formatCurrency, formatRelativeTime } from '@/utils/formatters';

const categoryEmojis = {
  'Food & Dining': '🍕',
  'Transport': '🚗',
  'Shopping': '🛍️',
  'Entertainment': '🎬',
  'Education': '📚',
  'Bills & Utilities': '📱',
  'Health': '💊',
  'Travel': '✈️',
  'Groceries': '🛒',
  'Rent': '🏠',
  'Salary': '💼',
  'Freelance': '💻',
  'Investment': '📈',
  'Transfer': '🔄',
  'Other': '💳',
};

const TransactionRow = memo(({ transaction, index = 0, onClick }) => {
  const {
    id,
    merchantName = 'Unknown',
    category = 'Other',
    amount = 0,
    type = 'DEBIT',
    status = 'COMPLETED',
    timestamp,
  } = transaction;

  const isCredit = type === 'CREDIT';
  const emoji = categoryEmojis[category] || '💳';

  return (
    <motion.button
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.3, delay: index * 0.05 }}
      onClick={() => onClick?.(transaction)}
      className="w-full flex items-center gap-3 p-3 rounded-btn hover:bg-bg-primary/60 transition-colors text-left group"
    >
      {/* Category icon */}
      <div className="h-10 w-10 rounded-full bg-bg-primary flex items-center justify-center text-lg flex-shrink-0 border border-color-border group-hover:border-accent-green/20 transition-colors">
        {emoji}
      </div>

      {/* Details */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <p className="text-sm font-medium text-text-primary truncate">
            {merchantName}
          </p>
          {status === 'FLAGGED' && <Badge type="FLAGGED" pulse>Flagged</Badge>}
          {status === 'PENDING' && <Badge type="PENDING">Pending</Badge>}
        </div>
        <p className="text-xs text-text-secondary mt-0.5">
          {category} · {formatRelativeTime(timestamp)}
        </p>
      </div>

      {/* Amount */}
      <p
        className={cn(
          'text-sm font-semibold tabular-nums flex-shrink-0',
          isCredit ? 'text-accent-green' : 'text-text-primary'
        )}
      >
        {isCredit ? '+' : '-'}{formatCurrency(Math.abs(amount))}
      </p>
    </motion.button>
  );
});

TransactionRow.displayName = 'TransactionRow';
export { TransactionRow, categoryEmojis };
