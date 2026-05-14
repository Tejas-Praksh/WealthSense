import { memo } from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, ArrowRightLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { TransactionRow } from './TransactionRow';
import { Button } from '@/components/ui/Button';
import { SkeletonTransaction } from '@/components/ui/SkeletonLoader';
import { ROUTES } from '@/utils/constants';

// Demo data
const demoTransactions = [
  {
    id: '1',
    merchantName: 'Zomato',
    category: 'Food & Dining',
    amount: 340,
    type: 'DEBIT',
    status: 'COMPLETED',
    timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: '2',
    merchantName: 'Ola Auto',
    category: 'Transport',
    amount: 85,
    type: 'DEBIT',
    status: 'COMPLETED',
    timestamp: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: '3',
    merchantName: 'Freelance — Logo Design',
    category: 'Freelance',
    amount: 2500,
    type: 'CREDIT',
    status: 'COMPLETED',
    timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: '4',
    merchantName: 'Amazon',
    category: 'Shopping',
    amount: 1299,
    type: 'DEBIT',
    status: 'PENDING',
    timestamp: new Date(Date.now() - 26 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: '5',
    merchantName: 'Unknown UPI — Dubai',
    category: 'Other',
    amount: 14999,
    type: 'DEBIT',
    status: 'FLAGGED',
    timestamp: new Date(Date.now() - 48 * 60 * 60 * 1000).toISOString(),
  },
];

const RecentTransactions = memo(({ transactions, isLoading, onTransactionClick }) => {
  const navigate = useNavigate();
  const items = transactions?.length ? transactions : demoTransactions;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: 0.3 }}
      className="bg-bg-card rounded-card border border-color-border p-5"
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-text-primary">Recent Transactions</h3>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => navigate(ROUTES.TRANSACTIONS)}
          className="text-xs"
        >
          View All
          <ArrowRight className="h-3 w-3" />
        </Button>
      </div>

      {/* Transaction list */}
      {isLoading ? (
        <div className="space-y-1">
          {[0, 1, 2, 3, 4].map((i) => (
            <SkeletonTransaction key={i} />
          ))}
        </div>
      ) : items.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-8">
          <div className="w-12 h-12 rounded-full bg-accent-blue/10 flex items-center justify-center mb-3">
            <ArrowRightLeft className="h-6 w-6 text-accent-blue" />
          </div>
          <p className="text-sm font-medium text-text-primary mb-1">No transactions yet</p>
          <p className="text-xs text-text-secondary">Your spending will appear here</p>
        </div>
      ) : (
        <div className="space-y-0.5">
          {items.slice(0, 5).map((txn, i) => (
            <TransactionRow
              key={txn.id}
              transaction={txn}
              index={i}
              onClick={onTransactionClick}
            />
          ))}
        </div>
      )}
    </motion.div>
  );
});

RecentTransactions.displayName = 'RecentTransactions';
export { RecentTransactions };
