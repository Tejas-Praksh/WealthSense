import { useState, useCallback, memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { PageLayout } from '@/components/layout/PageLayout';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { Badge } from '@/components/ui/Badge';
import { ProgressBar } from '@/components/ui/ProgressBar';
import {
  Users, Plus, ChevronRight, Send, Copy, Phone, ArrowUpRight, ArrowDownLeft,
  Check, X, Wallet, MessageCircle, Share2, Receipt, UserPlus,
} from 'lucide-react';
import { formatCurrency } from '@/utils/formatters';
import { cn } from '@/lib/utils';

/* ── Demo Data ────────────────────────────────────── */
const DEMO_GROUPS = [
  {
    id: 'g1', name: 'Goa Trip 2026', emoji: '🏖️', members: ['You', 'Amit', 'Priya', 'Rahul', 'Neha'],
    total: 24000, youOwe: 0, owedToYou: 2400, lastActivity: '2 days ago',
    expenses: [
      { id: 'e1', desc: 'Hotel booking', amount: 8000, paidBy: 'You', date: '2026-04-20', category: 'Stay', splits: { 'You': 1600, 'Amit': 1600, 'Priya': 1600, 'Rahul': 1600, 'Neha': 1600 } },
      { id: 'e2', desc: 'Dinner at Thalassa', amount: 4500, paidBy: 'Priya', date: '2026-04-21', category: 'Food', splits: { 'You': 900, 'Amit': 900, 'Priya': 900, 'Rahul': 900, 'Neha': 900 } },
      { id: 'e3', desc: 'Cab rental', amount: 6000, paidBy: 'Amit', date: '2026-04-20', category: 'Transport', splits: { 'You': 1200, 'Amit': 1200, 'Priya': 1200, 'Rahul': 1200, 'Neha': 1200 } },
      { id: 'e4', desc: 'Water sports', amount: 5500, paidBy: 'You', date: '2026-04-22', category: 'Activity', splits: { 'You': 1100, 'Amit': 1100, 'Priya': 1100, 'Rahul': 1100, 'Neha': 1100 } },
    ],
  },
  {
    id: 'g2', name: 'Flat Rent', emoji: '🏠', members: ['You', 'Amit', 'Rahul'],
    total: 30000, youOwe: 0, owedToYou: 0, lastActivity: '1 week ago',
    expenses: [
      { id: 'e5', desc: 'May rent', amount: 30000, paidBy: 'Amit', date: '2026-05-01', category: 'Rent', splits: { 'You': 10000, 'Amit': 10000, 'Rahul': 10000 } },
    ],
  },
  {
    id: 'g3', name: 'Movie Night', emoji: '🎬', members: ['You', 'Priya'],
    total: 1200, youOwe: 600, owedToYou: 0, lastActivity: '5 days ago',
    expenses: [
      { id: 'e6', desc: 'Tickets + Popcorn', amount: 1200, paidBy: 'Priya', date: '2026-05-07', category: 'Entertainment', splits: { 'You': 600, 'Priya': 600 } },
    ],
  },
];

const BALANCES_OWED = [
  { id: 'b1', name: 'Priya', amount: 600, group: 'Movie Night', avatar: '🎬' },
];

const BALANCES_OWING = [
  { id: 'b2', name: 'Amit', amount: 800, group: 'Goa Trip 2026', avatar: '🏖️' },
  { id: 'b3', name: 'Priya', amount: 1200, group: 'Goa Trip 2026', avatar: '🏖️' },
  { id: 'b4', name: 'Neha', amount: 400, group: 'Goa Trip 2026', avatar: '🏖️' },
];

const TABS = [
  { id: 'groups', label: 'Groups', icon: Users },
  { id: 'owe', label: 'You Owe', icon: ArrowUpRight },
  { id: 'owed', label: 'Owed to You', icon: ArrowDownLeft },
  { id: 'settle', label: 'Settle Up', icon: Check },
];

/* ── Group Card ──────────────────────────────────── */
const GroupCard = memo(({ group, onClick, index }) => {
  const borderColor = group.owedToYou > 0 ? 'border-accent-green/30' : group.youOwe > 0 ? 'border-accent-red/30' : 'border-color-border';
  return (
    <motion.button
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.06 }}
      onClick={onClick}
      className={cn('w-full text-left bg-bg-card rounded-card border p-4 hover:bg-bg-primary transition-all', borderColor)}
    >
      <div className="flex items-start justify-between mb-2">
        <div className="flex items-center gap-2">
          <span className="text-xl">{group.emoji}</span>
          <div>
            <p className="text-sm font-semibold text-text-primary">{group.name}</p>
            <p className="text-[10px] text-text-secondary">{group.members.length} members</p>
          </div>
        </div>
        <ChevronRight className="h-4 w-4 text-text-secondary" />
      </div>
      <div className="flex items-center justify-between text-[10px]">
        {group.owedToYou > 0 && <span className="text-accent-green font-medium">You are owed {formatCurrency(group.owedToYou)}</span>}
        {group.youOwe > 0 && <span className="text-accent-red font-medium">You owe {formatCurrency(group.youOwe)}</span>}
        {group.youOwe === 0 && group.owedToYou === 0 && <span className="text-text-secondary">All settled ✓</span>}
        <span className="text-text-secondary">{group.lastActivity}</span>
      </div>
    </motion.button>
  );
});
GroupCard.displayName = 'GroupCard';

/* ── Balance Card ────────────────────────────────── */
const BalanceCard = memo(({ item, type, onAction, index }) => (
  <motion.div
    initial={{ opacity: 0, x: type === 'owe' ? -12 : 12 }}
    animate={{ opacity: 1, x: 0 }}
    transition={{ delay: index * 0.06 }}
    className="bg-bg-card rounded-card border border-color-border p-4 flex items-center gap-3"
  >
    <div className="h-10 w-10 rounded-full bg-bg-primary flex items-center justify-center text-lg flex-shrink-0">{item.avatar}</div>
    <div className="flex-1 min-w-0">
      <p className="text-sm font-medium text-text-primary">{item.name}</p>
      <p className="text-[10px] text-text-secondary">{item.group}</p>
    </div>
    <div className="text-right flex-shrink-0">
      <p className={cn('text-sm font-bold tabular-nums', type === 'owe' ? 'text-accent-red' : 'text-accent-green')}>
        {formatCurrency(item.amount)}
      </p>
      <button onClick={() => onAction(item)} className={cn('text-[10px] font-medium hover:underline', type === 'owe' ? 'text-accent-red' : 'text-accent-green')}>
        {type === 'owe' ? 'Pay Now' : 'Remind'}
      </button>
    </div>
  </motion.div>
));
BalanceCard.displayName = 'BalanceCard';

/* ── Group Detail Drawer ─────────────────────────── */
const GroupDetail = memo(({ group, onClose, onAddExpense }) => {
  if (!group) return null;
  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="fixed inset-0 z-50 flex justify-end">
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} />
      <motion.div initial={{ x: '100%' }} animate={{ x: 0 }} exit={{ x: '100%' }} transition={{ type: 'spring', damping: 25 }}
        className="relative w-full max-w-md bg-bg-card border-l border-color-border overflow-y-auto z-10"
      >
        <div className="sticky top-0 bg-bg-card border-b border-color-border p-4 flex items-center justify-between z-10">
          <div className="flex items-center gap-2">
            <span className="text-xl">{group.emoji}</span>
            <div>
              <p className="text-sm font-bold text-text-primary">{group.name}</p>
              <p className="text-[10px] text-text-secondary">{group.members.length} members · {formatCurrency(group.total)} total</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-md text-text-secondary hover:text-text-primary"><X className="h-4 w-4" /></button>
        </div>

        {/* Members */}
        <div className="px-4 py-3 border-b border-color-border">
          <div className="flex -space-x-2">
            {group.members.map((m, i) => (
              <div key={m} className="h-8 w-8 rounded-full bg-bg-primary border-2 border-bg-card flex items-center justify-center text-[10px] font-bold text-text-primary" style={{ zIndex: group.members.length - i }}>
                {m.slice(0, 2).toUpperCase()}
              </div>
            ))}
          </div>
        </div>

        {/* Invite */}
        <div className="px-4 py-2.5 bg-accent-green/5 border-b border-accent-green/10 flex items-center gap-2">
          <UserPlus className="h-3.5 w-3.5 text-accent-green" />
          <span className="text-[10px] text-accent-green flex-1">Invite friends to WealthSense</span>
          <button className="px-2 py-1 rounded text-[9px] bg-accent-green/15 text-accent-green font-medium flex items-center gap-1">
            <MessageCircle className="h-2.5 w-2.5" /> WhatsApp
          </button>
          <button className="px-2 py-1 rounded text-[9px] bg-bg-primary text-text-secondary font-medium flex items-center gap-1">
            <Copy className="h-2.5 w-2.5" /> Link
          </button>
        </div>

        <div className="p-4">
          <Button variant="primary" size="sm" className="w-full mb-4" onClick={onAddExpense}><Plus className="h-3.5 w-3.5" /> Add Expense</Button>

          <h4 className="text-xs font-medium text-text-secondary mb-2">Expenses</h4>
          <div className="space-y-2">
            {group.expenses.map((exp) => (
              <div key={exp.id} className="bg-bg-primary rounded-btn p-3">
                <div className="flex items-center justify-between mb-1">
                  <p className="text-xs font-medium text-text-primary">{exp.desc}</p>
                  <p className="text-xs font-bold tabular-nums text-text-primary">{formatCurrency(exp.amount)}</p>
                </div>
                <div className="flex items-center justify-between text-[9px] text-text-secondary">
                  <span>Paid by <strong className="text-text-primary">{exp.paidBy}</strong></span>
                  <span>{new Date(exp.date).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' })}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </motion.div>
    </motion.div>
  );
});
GroupDetail.displayName = 'GroupDetail';

/* ── Add Expense Modal ───────────────────────────── */
const AddExpenseModal = memo(({ isOpen, onClose, groupMembers = [] }) => {
  const [splitType, setSplitType] = useState('equal');
  const members = groupMembers.length > 0 ? groupMembers : ['You', 'Amit', 'Priya', 'Rahul', 'Neha'];

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add Expense" size="md">
      <div className="space-y-4">
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Description</label>
          <input placeholder="e.g. Dinner" className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50" />
        </div>
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Amount (₹)</label>
          <input type="number" placeholder="2000" className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary tabular-nums placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50" />
        </div>
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Paid by</label>
          <select className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-1 focus:ring-accent-green/50">
            {members.map((m) => <option key={m}>{m}</option>)}
          </select>
        </div>
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-2 block">Split Type</label>
          <div className="flex gap-1.5">
            {['equal', 'exact', 'percentage'].map((t) => (
              <button key={t} onClick={() => setSplitType(t)}
                className={cn('flex-1 px-2 py-1.5 rounded-btn text-xs font-medium transition-all capitalize',
                  splitType === t ? 'bg-accent-green/15 text-accent-green border border-accent-green/20' : 'bg-bg-primary text-text-secondary border border-transparent'
                )}>{t}</button>
            ))}
          </div>
        </div>
        {splitType === 'equal' && (
          <div className="bg-accent-green/5 border border-accent-green/20 rounded-btn p-2.5">
            <p className="text-[10px] text-accent-green">Split equally: ₹400 per person ({members.length} people)</p>
          </div>
        )}
        <Button variant="primary" className="w-full" onClick={onClose}>Add Expense</Button>
      </div>
    </Modal>
  );
});
AddExpenseModal.displayName = 'AddExpenseModal';

/* ── Settle Modal ────────────────────────────────── */
const SettleModal = memo(({ isOpen, onClose, person }) => {
  const [method, setMethod] = useState('upi');
  if (!person) return null;
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Settle with ${person.name}`} size="sm">
      <div className="space-y-4">
        <div className="text-center py-3">
          <p className="text-2xl font-bold text-accent-green tabular-nums">{formatCurrency(person.amount)}</p>
          <p className="text-[10px] text-text-secondary mt-0.5">{person.group}</p>
        </div>
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-2 block">Payment Method</label>
          <div className="flex gap-1.5">
            {[{ id: 'upi', label: 'UPI' }, { id: 'cash', label: 'Cash' }, { id: 'bank', label: 'Bank Transfer' }].map((m) => (
              <button key={m.id} onClick={() => setMethod(m.id)}
                className={cn('flex-1 px-2 py-2 rounded-btn text-xs font-medium transition-all',
                  method === m.id ? 'bg-accent-green/15 text-accent-green border border-accent-green/20' : 'bg-bg-primary text-text-secondary border border-transparent'
                )}>{m.label}</button>
            ))}
          </div>
        </div>
        <input placeholder="Add a note (optional)" className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50" />
        <Button variant="primary" className="w-full" onClick={onClose}>
          <Check className="h-3.5 w-3.5" /> Mark as Settled
        </Button>
      </div>
    </Modal>
  );
});
SettleModal.displayName = 'SettleModal';

/* ── New Group Modal ─────────────────────────────── */
const NewGroupModal = memo(({ isOpen, onClose }) => {
  const [emoji, setEmoji] = useState('🏖️');
  const emojis = ['🏖️', '🏠', '🎬', '🍕', '✈️', '🎓', '🚗', '💼', '🎉', '💰'];
  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create Group" size="md">
      <div className="space-y-4">
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-2 block">Group Icon</label>
          <div className="flex flex-wrap gap-2">
            {emojis.map((e) => (
              <button key={e} onClick={() => setEmoji(e)}
                className={cn('h-9 w-9 rounded-md flex items-center justify-center text-lg transition-all',
                  emoji === e ? 'bg-accent-green/15 border border-accent-green/30 scale-110' : 'bg-bg-primary'
                )}>{e}</button>
            ))}
          </div>
        </div>
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Group Name</label>
          <input placeholder="e.g. Goa Trip 2026" className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50" />
        </div>
        <div>
          <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1.5 block">Add Members (phone or email)</label>
          <input placeholder="amit@gmail.com" className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-green/50" />
        </div>
        <Button variant="primary" className="w-full" onClick={onClose}>
          <Plus className="h-3.5 w-3.5" /> Create Group
        </Button>
      </div>
    </Modal>
  );
});
NewGroupModal.displayName = 'NewGroupModal';

/* ── Main Page ───────────────────────────────────── */
const SplitExpenses = () => {
  const [activeTab, setActiveTab] = useState('groups');
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [showAddExpense, setShowAddExpense] = useState(false);
  const [showSettle, setShowSettle] = useState(false);
  const [settlePerson, setSettlePerson] = useState(null);
  const [showNewGroup, setShowNewGroup] = useState(false);

  const handleSettle = useCallback((person) => {
    setSettlePerson(person);
    setShowSettle(true);
  }, []);

  const totalOwed = BALANCES_OWED.reduce((s, b) => s + b.amount, 0);
  const totalOwing = BALANCES_OWING.reduce((s, b) => s + b.amount, 0);

  return (
    <PageLayout>
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-5">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-text-primary">Split Expenses</h1>
            <p className="text-text-secondary text-xs mt-0.5">No more awkward money talks</p>
          </div>
          <Button variant="primary" size="sm" onClick={() => setShowNewGroup(true)}>
            <Plus className="h-3.5 w-3.5" /> New Group
          </Button>
        </div>

        {/* Summary */}
        <div className="grid grid-cols-2 gap-3">
          <div className="bg-bg-card rounded-card border border-accent-red/20 p-3">
            <p className="text-[10px] uppercase text-text-secondary">You Owe</p>
            <p className="text-lg font-bold tabular-nums text-accent-red">{formatCurrency(totalOwed)}</p>
          </div>
          <div className="bg-bg-card rounded-card border border-accent-green/20 p-3">
            <p className="text-[10px] uppercase text-text-secondary">Owed to You</p>
            <p className="text-lg font-bold tabular-nums text-accent-green">{formatCurrency(totalOwing)}</p>
          </div>
        </div>

        {/* Tabs */}
        <div className="flex gap-1 bg-bg-card rounded-card border border-color-border p-1 overflow-x-auto">
          {TABS.map((tab) => {
            const Icon = tab.icon;
            return (
              <button key={tab.id} onClick={() => setActiveTab(tab.id)}
                className={cn('flex items-center gap-1.5 px-3 py-2 rounded-btn text-xs font-medium transition-all whitespace-nowrap flex-shrink-0',
                  activeTab === tab.id ? 'bg-accent-green/10 text-accent-green border border-accent-green/20' : 'text-text-secondary hover:text-text-primary hover:bg-bg-primary'
                )}>
                <Icon className="h-3.5 w-3.5" />{tab.label}
              </button>
            );
          })}
        </div>

        {/* Tab content */}
        <AnimatePresence mode="wait">
          <motion.div key={activeTab} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }}>
            {activeTab === 'groups' && (
              <div className="space-y-2">
                {DEMO_GROUPS.map((g, i) => (
                  <GroupCard key={g.id} group={g} index={i} onClick={() => setSelectedGroup(g)} />
                ))}
              </div>
            )}

            {activeTab === 'owe' && (
              <div className="space-y-2">
                {BALANCES_OWED.length === 0 ? (
                  <div className="text-center py-12"><Check className="h-10 w-10 text-accent-green mx-auto mb-2" /><p className="text-sm text-text-primary font-medium">You're all settled up! 🎉</p></div>
                ) : (
                  <>
                    {BALANCES_OWED.map((b, i) => <BalanceCard key={b.id} item={b} type="owe" index={i} onAction={handleSettle} />)}
                    <Button variant="outline" className="w-full mt-3"><Wallet className="h-3.5 w-3.5" /> Pay All ({formatCurrency(totalOwed)})</Button>
                  </>
                )}
              </div>
            )}

            {activeTab === 'owed' && (
              <div className="space-y-2">
                {BALANCES_OWING.map((b, i) => <BalanceCard key={b.id} item={b} type="owed" index={i} onAction={(p) => alert(`Reminder sent to ${p.name}!`)} />)}
                <Button variant="outline" className="w-full mt-3"><Send className="h-3.5 w-3.5" /> Remind All</Button>
              </div>
            )}

            {activeTab === 'settle' && (
              <div className="space-y-3">
                <div className="bg-bg-card rounded-card border border-color-border p-5 text-center">
                  <p className="text-sm text-text-secondary mb-2">Net Balance</p>
                  <p className={cn('text-2xl font-bold tabular-nums', totalOwing - totalOwed >= 0 ? 'text-accent-green' : 'text-accent-red')}>
                    {totalOwing - totalOwed >= 0 ? '+' : ''}{formatCurrency(totalOwing - totalOwed)}
                  </p>
                  <p className="text-[10px] text-text-secondary mt-1">
                    {totalOwing > totalOwed ? 'Overall, friends owe you money' : 'Overall, you owe friends money'}
                  </p>
                </div>
                <h4 className="text-xs font-medium text-text-secondary">Quick Settle</h4>
                {[...BALANCES_OWED, ...BALANCES_OWING].map((b, i) => (
                  <BalanceCard key={b.id} item={b} type={BALANCES_OWED.includes(b) ? 'owe' : 'owed'} index={i} onAction={handleSettle} />
                ))}
              </div>
            )}
          </motion.div>
        </AnimatePresence>
      </motion.div>

      {/* Modals & Drawers */}
      <AnimatePresence>
        {selectedGroup && <GroupDetail group={selectedGroup} onClose={() => setSelectedGroup(null)} onAddExpense={() => { setSelectedGroup(null); setShowAddExpense(true); }} />}
      </AnimatePresence>
      <AddExpenseModal isOpen={showAddExpense} onClose={() => setShowAddExpense(false)} />
      <SettleModal isOpen={showSettle} onClose={() => setShowSettle(false)} person={settlePerson} />
      <NewGroupModal isOpen={showNewGroup} onClose={() => setShowNewGroup(false)} />
    </PageLayout>
  );
};

export default SplitExpenses;
