import { useState, useCallback, useEffect, useRef, memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useDispatch, useSelector } from 'react-redux';
import { Search, SlidersHorizontal, X, Calendar, Tag, ArrowDownUp, IndianRupee } from 'lucide-react';
import { setFilter, clearFilters } from '@/store/slices/transactionSlice';
import { cn } from '@/lib/utils';

const DATE_RANGES = [
  { value: 'today', label: 'Today' },
  { value: 'this_week', label: 'This Week' },
  { value: 'this_month', label: 'This Month' },
  { value: 'last_month', label: 'Last Month' },
  { value: 'last_3_months', label: '3 Months' },
];

const TYPES = [
  { value: 'all', label: 'All' },
  { value: 'DEBIT', label: 'Debit' },
  { value: 'CREDIT', label: 'Credit' },
  { value: 'TRANSFER', label: 'Transfer' },
  { value: 'REFUND', label: 'Refund' },
];

const CATEGORIES = [
  { value: 'all', label: 'All Categories' },
  { value: 'Food & Dining', label: '🍕 Food' },
  { value: 'Transport', label: '🚗 Transport' },
  { value: 'Shopping', label: '🛍️ Shopping' },
  { value: 'Bills & Utilities', label: '📱 Bills' },
  { value: 'Entertainment', label: '🎬 Entertainment' },
  { value: 'Education', label: '📚 Education' },
  { value: 'Groceries', label: '🛒 Groceries' },
  { value: 'Health', label: '💊 Health' },
  { value: 'Salary', label: '💼 Salary' },
];

const FilterChip = memo(({ label, onRemove }) => (
  <motion.span
    initial={{ opacity: 0, scale: 0.8 }}
    animate={{ opacity: 1, scale: 1 }}
    exit={{ opacity: 0, scale: 0.8 }}
    className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-accent-green/10 text-accent-green border border-accent-green/20"
  >
    {label}
    <button onClick={onRemove} className="hover:bg-accent-green/20 rounded-full p-0.5 transition-colors">
      <X className="h-2.5 w-2.5" />
    </button>
  </motion.span>
));
FilterChip.displayName = 'FilterChip';

const TransactionFilters = memo(({ totalCount }) => {
  const dispatch = useDispatch();
  const filters = useSelector((s) => s.transactions.filters);
  const [showFilters, setShowFilters] = useState(false);
  const searchRef = useRef(null);
  const debounceRef = useRef(null);

  const handleSearch = useCallback((e) => {
    const value = e.target.value;
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      dispatch(setFilter({ key: 'search', value }));
    }, 300);
  }, [dispatch]);

  useEffect(() => {
    return () => clearTimeout(debounceRef.current);
  }, []);

  const activeFilterCount = [
    filters.dateRange !== 'this_month',
    filters.type !== 'all',
    filters.category !== 'all',
    filters.minAmount !== '',
    filters.maxAmount !== '',
  ].filter(Boolean).length;

  const activeChips = [];
  if (filters.type !== 'all') activeChips.push({ key: 'type', label: `Type: ${filters.type}`, reset: 'all' });
  if (filters.category !== 'all') activeChips.push({ key: 'category', label: filters.category, reset: 'all' });
  if (filters.dateRange !== 'this_month') {
    const dr = DATE_RANGES.find((d) => d.value === filters.dateRange);
    activeChips.push({ key: 'dateRange', label: dr?.label || filters.dateRange, reset: 'this_month' });
  }
  if (filters.minAmount) activeChips.push({ key: 'minAmount', label: `Min: ₹${filters.minAmount}`, reset: '' });
  if (filters.maxAmount) activeChips.push({ key: 'maxAmount', label: `Max: ₹${filters.maxAmount}`, reset: '' });

  return (
    <div className="space-y-3">
      {/* Search + Filter toggle */}
      <div className="flex gap-2">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-text-secondary" />
          <input
            ref={searchRef}
            type="text"
            placeholder="Search transactions..."
            defaultValue={filters.search}
            onChange={handleSearch}
            className="w-full pl-9 pr-3 py-2.5 bg-bg-card border border-color-border rounded-btn text-sm text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50 focus:border-accent-green/50 transition-all"
          />
        </div>
        <button
          onClick={() => setShowFilters(!showFilters)}
          className={cn(
            'flex items-center gap-1.5 px-3 py-2.5 rounded-btn border text-sm font-medium transition-all',
            showFilters || activeFilterCount > 0
              ? 'bg-accent-green/10 border-accent-green/20 text-accent-green'
              : 'bg-bg-card border-color-border text-text-secondary hover:text-text-primary'
          )}
        >
          <SlidersHorizontal className="h-4 w-4" />
          <span className="hidden sm:inline">Filters</span>
          {activeFilterCount > 0 && (
            <span className="h-4 w-4 rounded-full bg-accent-green text-bg-primary text-[10px] flex items-center justify-center font-bold">
              {activeFilterCount}
            </span>
          )}
        </button>
      </div>

      {/* Expandable filters */}
      <AnimatePresence>
        {showFilters && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="overflow-hidden"
          >
            <div className="bg-bg-card rounded-card border border-color-border p-4 space-y-4">
              {/* Date Range */}
              <div>
                <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium flex items-center gap-1.5 mb-2">
                  <Calendar className="h-3 w-3" /> Date Range
                </label>
                <div className="flex flex-wrap gap-1.5">
                  {DATE_RANGES.map((dr) => (
                    <button
                      key={dr.value}
                      onClick={() => dispatch(setFilter({ key: 'dateRange', value: dr.value }))}
                      className={cn(
                        'px-2.5 py-1 rounded-md text-xs font-medium transition-all',
                        filters.dateRange === dr.value
                          ? 'bg-accent-green/15 text-accent-green border border-accent-green/20'
                          : 'bg-bg-primary text-text-secondary hover:text-text-primary border border-transparent'
                      )}
                    >
                      {dr.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Type + Category row */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium flex items-center gap-1.5 mb-2">
                    <ArrowDownUp className="h-3 w-3" /> Type
                  </label>
                  <div className="flex flex-wrap gap-1.5">
                    {TYPES.map((t) => (
                      <button
                        key={t.value}
                        onClick={() => dispatch(setFilter({ key: 'type', value: t.value }))}
                        className={cn(
                          'px-2.5 py-1 rounded-md text-xs font-medium transition-all',
                          filters.type === t.value
                            ? 'bg-accent-green/15 text-accent-green border border-accent-green/20'
                            : 'bg-bg-primary text-text-secondary hover:text-text-primary border border-transparent'
                        )}
                      >
                        {t.label}
                      </button>
                    ))}
                  </div>
                </div>
                <div>
                  <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium flex items-center gap-1.5 mb-2">
                    <Tag className="h-3 w-3" /> Category
                  </label>
                  <select
                    value={filters.category}
                    onChange={(e) => dispatch(setFilter({ key: 'category', value: e.target.value }))}
                    className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-1.5 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-accent-green/50"
                  >
                    {CATEGORIES.map((c) => (
                      <option key={c.value} value={c.value}>{c.label}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Amount range */}
              <div>
                <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium flex items-center gap-1.5 mb-2">
                  <IndianRupee className="h-3 w-3" /> Amount Range
                </label>
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    placeholder="Min"
                    value={filters.minAmount}
                    onChange={(e) => dispatch(setFilter({ key: 'minAmount', value: e.target.value }))}
                    className="flex-1 bg-bg-primary border border-color-border rounded-btn px-3 py-1.5 text-xs text-text-primary tabular-nums focus:outline-none focus:ring-1 focus:ring-accent-green/50"
                  />
                  <span className="text-text-secondary text-xs">to</span>
                  <input
                    type="number"
                    placeholder="Max"
                    value={filters.maxAmount}
                    onChange={(e) => dispatch(setFilter({ key: 'maxAmount', value: e.target.value }))}
                    className="flex-1 bg-bg-primary border border-color-border rounded-btn px-3 py-1.5 text-xs text-text-primary tabular-nums focus:outline-none focus:ring-1 focus:ring-accent-green/50"
                  />
                </div>
              </div>

              {/* Clear all */}
              {activeFilterCount > 0 && (
                <button
                  onClick={() => dispatch(clearFilters())}
                  className="text-xs text-accent-red hover:text-accent-red/80 transition-colors"
                >
                  Clear all filters
                </button>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Active filter chips */}
      <AnimatePresence>
        {activeChips.length > 0 && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="flex flex-wrap gap-1.5"
          >
            {activeChips.map((chip) => (
              <FilterChip
                key={chip.key}
                label={chip.label}
                onRemove={() => dispatch(setFilter({ key: chip.key, value: chip.reset }))}
              />
            ))}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
});

TransactionFilters.displayName = 'TransactionFilters';
export { TransactionFilters };
