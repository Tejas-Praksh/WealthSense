import { useMemo, memo } from 'react';
import { motion } from 'framer-motion';
import { ArrowRightLeft, Upload } from 'lucide-react';
import { TransactionCard } from './TransactionCard';
import { SkeletonTransaction } from '@/components/ui/SkeletonLoader';
import { formatCurrency } from '@/utils/formatters';
import { Button } from '@/components/ui/Button';

// Group transactions by date
function groupByDate(transactions) {
  const groups = {};
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  transactions.forEach((txn) => {
    const date = new Date(txn.timestamp);
    date.setHours(0, 0, 0, 0);

    let label;
    if (date.getTime() === today.getTime()) {
      label = 'Today';
    } else if (date.getTime() === yesterday.getTime()) {
      label = 'Yesterday';
    } else {
      label = date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
    }

    if (!groups[label]) {
      groups[label] = { label, dateTs: date.getTime(), transactions: [], total: 0 };
    }
    groups[label].transactions.push(txn);
    groups[label].total += txn.type === 'CREDIT' ? txn.amount : -txn.amount;
  });

  return Object.values(groups).sort((a, b) => b.dateTs - a.dateTs);
}

const TransactionList = memo(({ transactions, isLoading, searchTerm, onTransactionClick, onUploadClick, hasMore, onLoadMore }) => {
  const groups = useMemo(() => groupByDate(transactions || []), [transactions]);

  if (isLoading && (!transactions || transactions.length === 0)) {
    return (
      <div className="space-y-1">
        {Array.from({ length: 8 }).map((_, i) => (
          <SkeletonTransaction key={i} />
        ))}
      </div>
    );
  }

  if (!transactions || transactions.length === 0) {
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="bg-bg-card rounded-card border border-color-border"
      >
        <div className="flex flex-col items-center justify-center py-16 px-6">
          <div className="w-16 h-16 rounded-full bg-accent-blue/10 flex items-center justify-center mb-4">
            {searchTerm ? (
              <ArrowRightLeft className="h-8 w-8 text-accent-blue" />
            ) : (
              <Upload className="h-8 w-8 text-accent-blue" />
            )}
          </div>
          <h3 className="text-lg font-semibold text-text-primary mb-1">
            {searchTerm ? `No results for "${searchTerm}"` : 'No transactions yet'}
          </h3>
          <p className="text-sm text-text-secondary text-center max-w-xs mb-4">
            {searchTerm
              ? 'Try different search terms or adjust your filters'
              : 'Upload your bank statement to get started'}
          </p>
          <Button
            variant={searchTerm ? 'outline' : 'primary'}
            size="sm"
            onClick={searchTerm ? undefined : onUploadClick}
          >
            {searchTerm ? 'Clear filters' : 'Upload CSV'}
          </Button>
        </div>
      </motion.div>
    );
  }

  let globalIndex = 0;

  return (
    <div className="space-y-4">
      {groups.map((group) => (
        <div key={group.label}>
          {/* Date group header */}
          <div className="flex items-center justify-between px-1 mb-1 sticky top-0 bg-bg-primary/95 backdrop-blur-sm z-10 py-1">
            <h4 className="text-xs font-semibold text-text-secondary uppercase tracking-wider">
              {group.label}
            </h4>
            <span className={`text-xs font-medium tabular-nums ${group.total >= 0 ? 'text-accent-green' : 'text-text-secondary'}`}>
              {group.total >= 0 ? '+' : ''}{formatCurrency(group.total)}
            </span>
          </div>

          {/* Transaction cards */}
          <div className="bg-bg-card rounded-card border border-color-border divide-y divide-color-border/40">
            {group.transactions.map((txn) => {
              const idx = globalIndex++;
              return (
                <TransactionCard
                  key={txn.id}
                  transaction={txn}
                  index={idx}
                  onClick={onTransactionClick}
                  searchTerm={searchTerm}
                />
              );
            })}
          </div>
        </div>
      ))}

      {/* Load more */}
      {hasMore && (
        <div className="flex justify-center pt-2">
          <Button variant="ghost" size="sm" onClick={onLoadMore} isLoading={isLoading}>
            Load more
          </Button>
        </div>
      )}

      {/* Count */}
      <p className="text-center text-[10px] text-text-secondary pb-2">
        Showing {transactions.length} transaction{transactions.length !== 1 && 's'}
      </p>
    </div>
  );
});

TransactionList.displayName = 'TransactionList';
export { TransactionList };
