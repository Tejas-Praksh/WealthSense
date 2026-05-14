import { memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, MapPin, Flag, Users, Tag, MessageSquare } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { formatCurrency, formatDate } from '@/utils/formatters';
import { categoryEmojis } from './TransactionRow';
import { cn } from '@/lib/utils';

const TransactionDetailModal = memo(({ transaction, isOpen, onClose }) => {
  if (!transaction) return null;

  const {
    merchantName = 'Unknown',
    category = 'Other',
    amount = 0,
    type = 'DEBIT',
    status = 'COMPLETED',
    timestamp,
    description,
    accountNumber,
    referenceId,
  } = transaction;

  const isCredit = type === 'CREDIT';
  const emoji = categoryEmojis[category] || '💳';

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-end md:items-center justify-center">
          {/* Overlay */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/60 backdrop-blur-sm"
            onClick={onClose}
          />

          {/* Modal — slides up from bottom on mobile */}
          <motion.div
            initial={{ y: '100%', opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: '100%', opacity: 0 }}
            transition={{ type: 'spring', damping: 25, stiffness: 300 }}
            className="relative w-full max-w-md bg-bg-card border border-color-border rounded-t-2xl md:rounded-card z-10 max-h-[85vh] overflow-y-auto"
          >
            {/* Drag handle (mobile) */}
            <div className="flex justify-center pt-3 md:hidden">
              <div className="w-10 h-1 rounded-full bg-color-border" />
            </div>

            {/* Header */}
            <div className="flex items-start justify-between p-5 pb-0">
              <div className="flex items-center gap-3">
                <div className="h-12 w-12 rounded-full bg-bg-primary flex items-center justify-center text-xl border border-color-border">
                  {emoji}
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-text-primary">{merchantName}</h3>
                  <p className="text-xs text-text-secondary">{category}</p>
                </div>
              </div>
              <button
                onClick={onClose}
                className="p-1.5 rounded-md text-text-secondary hover:text-text-primary hover:bg-bg-primary transition-colors"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            {/* Amount */}
            <div className="p-5 text-center">
              <p
                className={cn(
                  'text-3xl font-bold tabular-nums',
                  isCredit ? 'text-accent-green' : 'text-text-primary'
                )}
              >
                {isCredit ? '+' : '-'}{formatCurrency(Math.abs(amount))}
              </p>
              <div className="flex items-center justify-center gap-2 mt-2">
                <Badge type={status}>{status}</Badge>
                <span className="text-xs text-text-secondary">
                  {formatDate(timestamp)}
                </span>
              </div>
            </div>

            {/* Details */}
            <div className="px-5 space-y-3">
              {[
                { icon: Tag, label: 'Category', value: category },
                { icon: MessageSquare, label: 'Description', value: description || 'No description' },
                { icon: MapPin, label: 'Location', value: 'Location data unavailable' },
              ].map((item) => (
                <div key={item.label} className="flex items-center gap-3 p-3 rounded-btn bg-bg-primary">
                  <item.icon className="h-4 w-4 text-text-secondary flex-shrink-0" />
                  <div className="min-w-0">
                    <p className="text-[10px] text-text-secondary uppercase tracking-wider">{item.label}</p>
                    <p className="text-sm text-text-primary truncate">{item.value}</p>
                  </div>
                </div>
              ))}
            </div>

            {/* Actions */}
            <div className="p-5 space-y-2">
              <Button
                variant="danger"
                size="md"
                className="w-full"
                onClick={() => { /* report fraud */ }}
              >
                <Flag className="h-4 w-4" />
                Report as Fraud
              </Button>
              <Button
                variant="outline"
                size="md"
                className="w-full"
                onClick={() => { /* split */ }}
              >
                <Users className="h-4 w-4" />
                Split with Friend
              </Button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
});

TransactionDetailModal.displayName = 'TransactionDetailModal';
export { TransactionDetailModal };
