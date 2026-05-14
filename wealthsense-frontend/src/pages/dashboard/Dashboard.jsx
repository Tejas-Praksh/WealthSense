import { useState, useCallback } from 'react';
import { motion } from 'framer-motion';
import { useSelector } from 'react-redux';
import { PageLayout } from '@/components/layout/PageLayout';
import { HeroCard } from './components/HeroCard';
import { SpendingChart } from './components/SpendingChart';
import { AiInsightCard } from './components/AiInsightCard';
import { RecentTransactions } from './components/RecentTransactions';
import { TransactionDetailModal } from './components/TransactionDetailModal';
import { GoalsProgress } from './components/GoalsProgress';
import { QuickActions } from './components/QuickActions';

const Dashboard = () => {
  const user = useSelector((state) => state.auth.user);
  const [fraudAlert, setFraudAlert] = useState(true);
  const [selectedTxn, setSelectedTxn] = useState(null);

  // Determine greeting based on time
  const hour = new Date().getHours();
  const greeting =
    hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
  const firstName = user?.firstName || 'Raju';

  const handleTransactionClick = useCallback((txn) => {
    setSelectedTxn(txn);
  }, []);

  return (
    <PageLayout>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.3 }}
        className="space-y-5"
      >
        {/* Greeting */}
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.05 }}
        >
          <h1 className="text-xl md:text-2xl font-bold text-text-primary">
            {greeting}, {firstName} 👋
          </h1>
          <p className="text-text-secondary text-sm mt-0.5">
            Here's your financial snapshot
          </p>
        </motion.div>

        {/* Section 1: Hero Card */}
        <HeroCard
          isLoading={false}
          fraudAlert={fraudAlert}
          onDismissAlert={() => setFraudAlert(false)}
        />

        {/* Quick Actions */}
        <QuickActions />

        {/* Section 2 + 3: Chart + AI (side by side on desktop) */}
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-5">
          <div className="lg:col-span-3">
            <SpendingChart isLoading={false} />
          </div>
          <div className="lg:col-span-2">
            <AiInsightCard isLoading={false} />
          </div>
        </div>

        {/* Section 4 + 5: Transactions + Goals (side by side on desktop) */}
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-5">
          <div className="lg:col-span-3">
            <RecentTransactions
              isLoading={false}
              onTransactionClick={handleTransactionClick}
            />
          </div>
          <div className="lg:col-span-2">
            <GoalsProgress isLoading={false} />
          </div>
        </div>
      </motion.div>

      {/* Transaction Detail Modal */}
      <TransactionDetailModal
        transaction={selectedTxn}
        isOpen={!!selectedTxn}
        onClose={() => setSelectedTxn(null)}
      />
    </PageLayout>
  );
};

export default Dashboard;
