import { memo, useState } from 'react';
import { motion } from 'framer-motion';
import { MoreHorizontal, Edit3, MessageSquare, Flag, Users, Download } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Badge } from '@/components/ui/Badge';
import { formatCurrency, formatRelativeTime } from '@/utils/formatters';

const categoryConfig = {
  'Food & Dining': { emoji: '🍕', color: 'bg-orange-500/15 border-orange-500/20' },
  'Transport': { emoji: '🚗', color: 'bg-blue-500/15 border-blue-500/20' },
  'Shopping': { emoji: '🛍️', color: 'bg-purple-500/15 border-purple-500/20' },
  'Bills & Utilities': { emoji: '📱', color: 'bg-yellow-500/15 border-yellow-500/20' },
  'Entertainment': { emoji: '🎬', color: 'bg-pink-500/15 border-pink-500/20' },
  'Education': { emoji: '📚', color: 'bg-teal-500/15 border-teal-500/20' },
  'Groceries': { emoji: '🛒', color: 'bg-lime-500/15 border-lime-500/20' },
  'Health': { emoji: '💊', color: 'bg-red-400/15 border-red-400/20' },
  'Salary': { emoji: '💼', color: 'bg-green-500/15 border-green-500/20' },
  'Freelance': { emoji: '💻', color: 'bg-cyan-500/15 border-cyan-500/20' },
  'Investment': { emoji: '📈', color: 'bg-emerald-500/15 border-emerald-500/20' },
  'Transfer': { emoji: '🔄', color: 'bg-gray-500/15 border-gray-500/20' },
  'Rent': { emoji: '🏠', color: 'bg-amber-500/15 border-amber-500/20' },
  'Other': { emoji: '💳', color: 'bg-slate-500/15 border-slate-500/20' },
};

const menuItems = [
  { icon: Edit3, label: 'Edit category' },
  { icon: MessageSquare, label: 'Add note' },
  { icon: Flag, label: 'Mark as fraud' },
  { icon: Users, label: 'Split with friend' },
  { icon: Download, label: 'Download receipt' },
];

const TransactionCard = memo(({ transaction, index = 0, onClick, searchTerm }) => {
  const [showMenu, setShowMenu] = useState(false);
  const {
    merchantName = 'Unknown',
    category = 'Other',
    amount = 0,
    type = 'DEBIT',
    status = 'COMPLETED',
    timestamp,
  } = transaction;

  const isCredit = type === 'CREDIT';
  const isFlagged = status === 'FLAGGED';
  const config = categoryConfig[category] || categoryConfig['Other'];

  // Highlight search term in merchant name
  const renderName = () => {
    if (!searchTerm || searchTerm.length < 2) return merchantName;
    const regex = new RegExp(`(${searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
    const parts = merchantName.split(regex);
    return parts.map((part, i) =>
      regex.test(part) ? (
        <span key={i} className="bg-accent-green/20 text-accent-green rounded px-0.5">{part}</span>
      ) : (
        part
      )
    );
  };

  return (
    <motion.div
      initial={{ opacity: 0, x: 15 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.25, delay: Math.min(index * 0.03, 0.3) }}
      className={cn(
        'group relative flex items-center gap-3 p-3 rounded-btn hover:bg-bg-primary/60 transition-colors cursor-pointer',
        isFlagged && 'border-l-2 border-l-accent-red bg-accent-red/[0.02]'
      )}
      onClick={() => onClick?.(transaction)}
    >
      {/* Category icon */}
      <div className={cn('h-10 w-10 rounded-full flex items-center justify-center text-base flex-shrink-0 border', config.color)}>
        {config.emoji}
      </div>

      {/* Details */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <p className="text-sm font-medium text-text-primary truncate">{renderName()}</p>
          {isFlagged && <Badge type="FLAGGED" pulse>Flagged</Badge>}
          {status === 'PENDING' && <Badge type="PENDING">Pending</Badge>}
        </div>
        <div className="flex items-center gap-1.5 mt-0.5">
          <span className="text-[10px] text-text-secondary/70 bg-bg-primary px-1.5 py-0.5 rounded">{category}</span>
          <span className="text-[10px] text-text-secondary">·</span>
          <span className="text-[10px] text-text-secondary">{formatRelativeTime(timestamp)}</span>
        </div>
      </div>

      {/* Amount + menu */}
      <div className="flex items-center gap-2 flex-shrink-0">
        <p className={cn('text-sm font-semibold tabular-nums', isCredit ? 'text-accent-green' : 'text-text-primary')}>
          {isCredit ? '+' : '-'}{formatCurrency(Math.abs(amount))}
        </p>

        {/* Three dot menu */}
        <div className="relative">
          <button
            onClick={(e) => { e.stopPropagation(); setShowMenu(!showMenu); }}
            className="p-1 rounded-md text-text-secondary opacity-0 group-hover:opacity-100 hover:bg-bg-secondary transition-all"
          >
            <MoreHorizontal className="h-4 w-4" />
          </button>

          {showMenu && (
            <>
              <div className="fixed inset-0 z-40" onClick={(e) => { e.stopPropagation(); setShowMenu(false); }} />
              <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                className="absolute right-0 top-8 z-50 w-44 bg-bg-card border border-color-border rounded-card shadow-xl py-1"
              >
                {menuItems.map((item) => (
                  <button
                    key={item.label}
                    onClick={(e) => { e.stopPropagation(); setShowMenu(false); }}
                    className="flex items-center gap-2 w-full px-3 py-2 text-xs text-text-secondary hover:text-text-primary hover:bg-bg-primary transition-colors"
                  >
                    <item.icon className="h-3.5 w-3.5" />
                    {item.label}
                  </button>
                ))}
              </motion.div>
            </>
          )}
        </div>
      </div>
    </motion.div>
  );
});

TransactionCard.displayName = 'TransactionCard';
export { TransactionCard, categoryConfig };
