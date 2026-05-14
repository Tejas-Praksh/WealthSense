import { memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, MapPin, Flag, Users, Tag, MessageSquare, Hash, Clock, CreditCard } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { formatCurrency, formatDate } from '@/utils/formatters';
import { categoryConfig } from './TransactionCard';
import { cn } from '@/lib/utils';

const TransactionDetailDrawer = memo(({ transaction, isOpen, onClose }) => {
  if (!transaction) return null;

  const {
    merchantName = 'Unknown',
    category = 'Other',
    amount = 0,
    type = 'DEBIT',
    status = 'COMPLETED',
    timestamp,
    description,
    referenceId,
    correlationId,
    upiId,
  } = transaction;

  const isCredit = type === 'CREDIT';
  const config = categoryConfig[category] || categoryConfig['Other'];

  const details = [
    { icon: Tag, label: 'Category', value: category },
    { icon: Clock, label: 'Date & Time', value: formatDate(timestamp) },
    { icon: CreditCard, label: 'Type', value: type },
    { icon: MessageSquare, label: 'Description', value: description || 'No description' },
    ...(upiId ? [{ icon: Hash, label: 'UPI ID', value: upiId }] : []),
    ...(referenceId ? [{ icon: Hash, label: 'Reference', value: referenceId }] : []),
    ...(correlationId ? [{ icon: Hash, label: 'Correlation ID', value: correlationId }] : []),
  ];

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex justify-end">
          {/* Overlay */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm"
            onClick={onClose}
          />

          {/* Drawer — right on desktop, bottom on mobile */}
          <motion.div
            initial={{ x: '100%' }}
            animate={{ x: 0 }}
            exit={{ x: '100%' }}
            transition={{ type: 'spring', damping: 30, stiffness: 300 }}
            className="relative w-full max-w-md bg-bg-card border-l border-color-border z-10 overflow-y-auto hidden md:block"
          >
            <DrawerContent
              transaction={transaction}
              config={config}
              isCredit={isCredit}
              details={details}
              onClose={onClose}
            />
          </motion.div>

          {/* Mobile — bottom sheet */}
          <motion.div
            initial={{ y: '100%' }}
            animate={{ y: 0 }}
            exit={{ y: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 300 }}
            className="fixed bottom-0 left-0 right-0 bg-bg-card border-t border-color-border rounded-t-2xl z-10 max-h-[85vh] overflow-y-auto md:hidden"
          >
            <div className="flex justify-center pt-3">
              <div className="w-10 h-1 rounded-full bg-color-border" />
            </div>
            <DrawerContent
              transaction={transaction}
              config={config}
              isCredit={isCredit}
              details={details}
              onClose={onClose}
            />
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
});

const DrawerContent = ({ transaction, config, isCredit, details, onClose }) => (
  <div className="p-5">
    {/* Header */}
    <div className="flex items-start justify-between mb-5">
      <div className="flex items-center gap-3">
        <div className={cn('h-12 w-12 rounded-full flex items-center justify-center text-xl border', config.color)}>
          {config.emoji}
        </div>
        <div>
          <h3 className="text-lg font-semibold text-text-primary">{transaction.merchantName}</h3>
          <Badge type={transaction.status}>{transaction.status}</Badge>
        </div>
      </div>
      <button onClick={onClose} className="p-1.5 rounded-md text-text-secondary hover:text-text-primary hover:bg-bg-primary transition-colors">
        <X className="h-4 w-4" />
      </button>
    </div>

    {/* Amount */}
    <div className="text-center py-4 mb-4 rounded-card bg-bg-primary">
      <p className={cn('text-3xl font-bold tabular-nums', isCredit ? 'text-accent-green' : 'text-text-primary')}>
        {isCredit ? '+' : '-'}{formatCurrency(Math.abs(transaction.amount))}
      </p>
    </div>

    {/* Details */}
    <div className="space-y-2 mb-6">
      {details.map((item) => (
        <div key={item.label} className="flex items-center gap-3 p-2.5 rounded-btn bg-bg-primary">
          <item.icon className="h-3.5 w-3.5 text-text-secondary flex-shrink-0" />
          <div className="min-w-0 flex-1">
            <p className="text-[9px] uppercase tracking-wider text-text-secondary">{item.label}</p>
            <p className="text-xs text-text-primary truncate">{item.value}</p>
          </div>
        </div>
      ))}
    </div>

    {/* Notes */}
    <div className="mb-6">
      <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Notes</label>
      <textarea
        placeholder="Add a note..."
        className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-xs text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50 resize-none h-16"
      />
    </div>

    {/* Actions */}
    <div className="space-y-2">
      <Button variant="danger" size="md" className="w-full">
        <Flag className="h-4 w-4" />
        Report as Fraud
      </Button>
      <Button variant="outline" size="md" className="w-full">
        <Users className="h-4 w-4" />
        Split Expense
      </Button>
    </div>
  </div>
);

TransactionDetailDrawer.displayName = 'TransactionDetailDrawer';
export { TransactionDetailDrawer };
