import { useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useSelector, useDispatch } from 'react-redux';
import { LayoutDashboard, Calculator, Target, Shield, Plus } from 'lucide-react';
import { PageLayout } from '@/components/layout/PageLayout';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { InvestmentOverview } from './components/InvestmentOverview';
import { SIPCalculator } from './components/SIPCalculator';
import { MyGoals } from './components/MyGoals';
import { TaxSaving } from './components/TaxSaving';
import { setActiveTab } from '@/store/slices/investmentSlice';
import { cn } from '@/lib/utils';

const TABS = [
  { id: 'overview', label: 'Overview', icon: LayoutDashboard },
  { id: 'sip', label: 'SIP Calculator', icon: Calculator },
  { id: 'goals', label: 'My Goals', icon: Target },
  { id: 'tax', label: 'Tax Saving', icon: Shield },
];

/* ── Add Investment Modal ────────────────────────── */
const AddInvestmentModal = ({ isOpen, onClose }) => {
  const [step, setStep] = useState(1);
  const [type, setType] = useState('SIP');

  const types = [
    { id: 'SIP', label: 'SIP (Systematic Investment Plan)', desc: 'Invest monthly in mutual funds', rec: true },
    { id: 'LUMPSUM', label: 'Lump Sum', desc: 'One-time investment' },
    { id: 'PPF', label: 'Public Provident Fund', desc: '15-year government scheme at 7.1%' },
    { id: 'FD', label: 'Fixed Deposit', desc: 'Guaranteed returns from banks' },
    { id: 'ELSS', label: 'ELSS Tax Saver', desc: 'Tax-saving mutual fund, 3yr lock-in' },
  ];

  return (
    <Modal isOpen={isOpen} onClose={() => { onClose(); setStep(1); }} title={`Add Investment — Step ${step}/3`} size="md">
      <div className="space-y-4">
        {step === 1 && (
          <div className="space-y-2">
            {types.map((t) => (
              <button
                key={t.id}
                onClick={() => setType(t.id)}
                className={cn(
                  'w-full text-left p-3 rounded-btn border transition-all',
                  type === t.id ? 'bg-accent-green/10 border-accent-green/30' : 'bg-bg-primary border-color-border hover:border-text-secondary/30'
                )}
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-text-primary">{t.label}</p>
                    <p className="text-[10px] text-text-secondary">{t.desc}</p>
                  </div>
                  {t.rec && <span className="px-1.5 py-0.5 rounded text-[9px] bg-accent-green/15 text-accent-green font-medium">Recommended</span>}
                </div>
              </button>
            ))}
          </div>
        )}

        {step === 2 && (
          <div className="space-y-3">
            <div>
              <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Amount (₹)</label>
              <input type="number" placeholder="2000" className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50" />
            </div>
            {(type === 'SIP' || type === 'ELSS') && (
              <div>
                <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Fund Category</label>
                <select className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-1 focus:ring-accent-green/50">
                  <option>Nifty 50 Index Fund</option>
                  <option>Parag Parikh Flexi Cap</option>
                  <option>Mirae Asset Large Cap</option>
                  <option>Axis Small Cap</option>
                </select>
              </div>
            )}
            <div>
              <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Duration</label>
              <select className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-1 focus:ring-accent-green/50">
                <option>3 years</option>
                <option>5 years</option>
                <option>10 years</option>
                <option>20 years</option>
              </select>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="text-center py-4">
            <p className="text-sm font-medium text-text-primary mb-2">You're about to track a new {type} investment</p>
            <p className="text-[10px] text-text-secondary mb-4">⚠️ This is simulated tracking only — no real money is being invested.</p>
            <Button variant="primary" className="w-full" onClick={() => { onClose(); setStep(1); }}>
              Confirm & Start Tracking
            </Button>
          </div>
        )}

        {step < 3 && (
          <div className="flex gap-2">
            {step > 1 && <Button variant="ghost" className="flex-1" onClick={() => setStep(step - 1)}>Back</Button>}
            <Button variant="primary" className="flex-1" onClick={() => setStep(step + 1)}>Next</Button>
          </div>
        )}
      </div>
    </Modal>
  );
};

/* ── Main Investments Page ───────────────────────── */
const Investments = () => {
  const dispatch = useDispatch();
  const activeTab = useSelector((s) => s.investments.activeTab);
  const [showAddModal, setShowAddModal] = useState(false);

  const handleTab = useCallback((id) => dispatch(setActiveTab(id)), [dispatch]);

  return (
    <PageLayout>
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-5">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-text-primary">Investments</h1>
            <p className="text-text-secondary text-xs mt-0.5">Grow your wealth, one SIP at a time</p>
          </div>
          <Button variant="primary" size="sm" onClick={() => setShowAddModal(true)}>
            <Plus className="h-3.5 w-3.5" /> Add Investment
          </Button>
        </div>

        {/* Tabs */}
        <div className="flex gap-1 bg-bg-card rounded-card border border-color-border p-1 overflow-x-auto">
          {TABS.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => handleTab(tab.id)}
                className={cn(
                  'flex items-center gap-1.5 px-3 py-2 rounded-btn text-xs font-medium transition-all whitespace-nowrap flex-shrink-0',
                  isActive
                    ? 'bg-accent-green/10 text-accent-green border border-accent-green/20'
                    : 'text-text-secondary hover:text-text-primary hover:bg-bg-primary'
                )}
              >
                <Icon className="h-3.5 w-3.5" />
                {tab.label}
              </button>
            );
          })}
        </div>

        {/* Tab content */}
        <AnimatePresence mode="wait">
          <motion.div
            key={activeTab}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.15 }}
          >
            {activeTab === 'overview' && <InvestmentOverview onStartSIP={() => setShowAddModal(true)} />}
            {activeTab === 'sip' && <SIPCalculator />}
            {activeTab === 'goals' && <MyGoals />}
            {activeTab === 'tax' && <TaxSaving />}
          </motion.div>
        </AnimatePresence>
      </motion.div>

      <AddInvestmentModal isOpen={showAddModal} onClose={() => setShowAddModal(false)} />
    </PageLayout>
  );
};

export default Investments;
