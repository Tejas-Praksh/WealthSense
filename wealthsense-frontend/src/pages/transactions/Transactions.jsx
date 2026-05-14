import { useState, useCallback, useMemo } from 'react';
import { motion } from 'framer-motion';
import { useSelector, useDispatch } from 'react-redux';
import { Upload, Download } from 'lucide-react';
import { PageLayout } from '@/components/layout/PageLayout';
import { Button } from '@/components/ui/Button';
import { TransactionFilters } from './components/TransactionFilters';
import { TransactionStats } from './components/TransactionStats';
import { TransactionChart } from './components/TransactionChart';
import { TransactionList } from './components/TransactionList';
import { TransactionDetailDrawer } from './components/TransactionDetailDrawer';
import { CSVUploadModal } from './components/CSVUploadModal';
import { ExportModal } from './components/ExportModal';
import { setSelectedTransaction, setFilter } from '@/store/slices/transactionSlice';

/* ── Demo transactions ────────────────────────────── */
const now = Date.now();
const h = (hours) => now - hours * 3600_000;

const DEMO_TRANSACTIONS = [
  { id: '1', merchantName: 'Zomato', category: 'Food & Dining', amount: 340, type: 'DEBIT', status: 'COMPLETED', timestamp: h(2), description: 'Biryani order', upiId: 'zomato@upi' },
  { id: '2', merchantName: 'Ola Auto', category: 'Transport', amount: 85, type: 'DEBIT', status: 'COMPLETED', timestamp: h(5), description: 'Auto to college' },
  { id: '3', merchantName: 'Freelance — Logo Design', category: 'Freelance', amount: 2500, type: 'CREDIT', status: 'COMPLETED', timestamp: h(28), description: 'Logo design for client' },
  { id: '4', merchantName: 'Amazon', category: 'Shopping', amount: 1299, type: 'DEBIT', status: 'PENDING', timestamp: h(30), description: 'Wireless earbuds', referenceId: 'AMZ-87832' },
  { id: '5', merchantName: 'Unknown UPI — Dubai', category: 'Other', amount: 14999, type: 'DEBIT', status: 'FLAGGED', timestamp: h(32), description: 'Suspicious transaction', upiId: 'unknown@ybl', correlationId: 'COR-5523' },
  { id: '6', merchantName: 'Swiggy', category: 'Food & Dining', amount: 180, type: 'DEBIT', status: 'COMPLETED', timestamp: h(48), description: 'Dinner order', upiId: 'swiggy@axisbank' },
  { id: '7', merchantName: 'Netflix India', category: 'Entertainment', amount: 649, type: 'DEBIT', status: 'COMPLETED', timestamp: h(50), description: 'Monthly subscription' },
  { id: '8', merchantName: 'Salary Credit', category: 'Salary', amount: 26000, type: 'CREDIT', status: 'COMPLETED', timestamp: h(72), description: 'May salary', referenceId: 'SAL-2026-05' },
  { id: '9', merchantName: 'PhonePe Recharge', category: 'Bills & Utilities', amount: 299, type: 'DEBIT', status: 'COMPLETED', timestamp: h(96), description: 'Jio prepaid recharge', upiId: 'phonepe@ybl' },
  { id: '10', merchantName: 'Uber Auto', category: 'Transport', amount: 120, type: 'DEBIT', status: 'COMPLETED', timestamp: h(100), description: 'Ride to market' },
  { id: '11', merchantName: 'Reliance Smart', category: 'Groceries', amount: 870, type: 'DEBIT', status: 'COMPLETED', timestamp: h(120), description: 'Weekly groceries' },
  { id: '12', merchantName: 'BookMyShow', category: 'Entertainment', amount: 350, type: 'DEBIT', status: 'COMPLETED', timestamp: h(140), description: '2x movie tickets' },
  { id: '13', merchantName: 'Flipkart', category: 'Shopping', amount: 2499, type: 'DEBIT', status: 'COMPLETED', timestamp: h(145), description: 'Phone case + charger', referenceId: 'FK-448823' },
  { id: '14', merchantName: 'Dominos', category: 'Food & Dining', amount: 450, type: 'DEBIT', status: 'COMPLETED', timestamp: h(168), description: 'Party pizza order' },
  { id: '15', merchantName: 'College Tuition', category: 'Education', amount: 15000, type: 'DEBIT', status: 'COMPLETED', timestamp: h(200), description: 'Semester fee partial' },
  { id: '16', merchantName: 'Freelance — Website', category: 'Freelance', amount: 5000, type: 'CREDIT', status: 'COMPLETED', timestamp: h(220), description: 'Portfolio website build' },
  { id: '17', merchantName: 'Paytm Electricity', category: 'Bills & Utilities', amount: 1200, type: 'DEBIT', status: 'COMPLETED', timestamp: h(250), description: 'Electricity bill May', upiId: 'paytm@paytm' },
  { id: '18', merchantName: 'Chai Point', category: 'Food & Dining', amount: 80, type: 'DEBIT', status: 'COMPLETED', timestamp: h(4), description: 'Masala chai x2' },
  { id: '19', merchantName: 'Pharmacy', category: 'Health', amount: 240, type: 'DEBIT', status: 'COMPLETED', timestamp: h(52), description: 'Paracetamol + vitamins' },
  { id: '20', merchantName: 'GPay — Rent', category: 'Rent', amount: 8000, type: 'DEBIT', status: 'COMPLETED', timestamp: h(300), description: 'Room rent May', upiId: 'landlord@oksbi' },
];

const Transactions = () => {
  const dispatch = useDispatch();
  const filters = useSelector((s) => s.transactions.filters);
  const selectedTransaction = useSelector((s) => s.transactions.selectedTransaction);
  const isDrawerOpen = useSelector((s) => s.transactions.isDrawerOpen);

  const [showCSV, setShowCSV] = useState(false);
  const [showExport, setShowExport] = useState(false);
  const [showChart, setShowChart] = useState(true);

  // Filter demo data client-side
  const filtered = useMemo(() => {
    let list = [...DEMO_TRANSACTIONS];

    if (filters.type !== 'all') {
      list = list.filter((t) => t.type === filters.type);
    }
    if (filters.category !== 'all') {
      list = list.filter((t) => t.category === filters.category);
    }
    if (filters.search && filters.search.length >= 2) {
      const q = filters.search.toLowerCase();
      list = list.filter((t) => t.merchantName.toLowerCase().includes(q));
    }
    if (filters.minAmount) {
      list = list.filter((t) => t.amount >= Number(filters.minAmount));
    }
    if (filters.maxAmount) {
      list = list.filter((t) => t.amount <= Number(filters.maxAmount));
    }

    return list.sort((a, b) => b.timestamp - a.timestamp);
  }, [filters]);

  const handleTransactionClick = useCallback((txn) => {
    dispatch(setSelectedTransaction(txn));
  }, [dispatch]);

  const handleCloseDrawer = useCallback(() => {
    dispatch(setSelectedTransaction(null));
  }, [dispatch]);

  const handleCategoryClick = useCallback((category) => {
    dispatch(setFilter({ key: 'category', value: category }));
  }, [dispatch]);

  return (
    <PageLayout>
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-5">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-text-primary">Transactions</h1>
            <p className="text-text-secondary text-sm mt-0.5">Track every rupee, in and out</p>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" onClick={() => setShowCSV(true)}>
              <Upload className="h-3.5 w-3.5" />
              <span className="hidden sm:inline">Upload CSV</span>
            </Button>
            <Button variant="ghost" size="sm" onClick={() => setShowExport(true)}>
              <Download className="h-3.5 w-3.5" />
              <span className="hidden sm:inline">Export</span>
            </Button>
          </div>
        </div>

        {/* Filters */}
        <TransactionFilters totalCount={filtered.length} />

        {/* Stats */}
        <TransactionStats />

        {/* Chart (toggleable) */}
        {showChart && (
          <TransactionChart onCategoryClick={handleCategoryClick} />
        )}

        {/* Transaction list */}
        <TransactionList
          transactions={filtered}
          searchTerm={filters.search}
          onTransactionClick={handleTransactionClick}
          onUploadClick={() => setShowCSV(true)}
          hasMore={false}
        />
      </motion.div>

      {/* Detail drawer */}
      <TransactionDetailDrawer
        transaction={selectedTransaction}
        isOpen={isDrawerOpen}
        onClose={handleCloseDrawer}
      />

      {/* Modals */}
      <CSVUploadModal isOpen={showCSV} onClose={() => setShowCSV(false)} />
      <ExportModal isOpen={showExport} onClose={() => setShowExport(false)} />
    </PageLayout>
  );
};

export default Transactions;
