import { useState, useCallback, memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useSelector, useDispatch } from 'react-redux';
import { PageLayout } from '@/components/layout/PageLayout';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { ProgressBar } from '@/components/ui/ProgressBar';
import {
  User, Mail, Phone, MapPin, Briefcase, Calendar, Camera, Pencil, Check, X,
  Shield, Lock, Smartphone, LogOut, Trash2, Bell, BellOff, Globe, Palette, Download,
  TrendingUp, Target, MessageSquare, IndianRupee, ChevronRight, AlertTriangle,
} from 'lucide-react';
import { setProfile, setEditing, toggleNotification } from '@/store/slices/profileSlice';
import { formatCurrency } from '@/utils/formatters';
import { cn } from '@/lib/utils';

/* ── Toggle Switch ───────────────────────────────── */
const Switch = memo(({ enabled, onChange, disabled }) => (
  <button
    onClick={onChange}
    disabled={disabled}
    className={cn(
      'relative inline-flex h-5 w-9 items-center rounded-full transition-all',
      enabled ? 'bg-accent-green' : 'bg-bg-primary',
      disabled && 'opacity-50 cursor-not-allowed'
    )}
  >
    <motion.span
      layout
      className={cn('inline-block h-3.5 w-3.5 rounded-full bg-white shadow-sm transform',
        enabled ? 'translate-x-[18px]' : 'translate-x-[3px]'
      )}
      transition={{ type: 'spring', stiffness: 500, damping: 30 }}
    />
  </button>
));
Switch.displayName = 'Switch';

/* ── Section Container ───────────────────────────── */
const Section = memo(({ title, icon: Icon, children, delay = 0 }) => (
  <motion.div
    initial={{ opacity: 0, y: 12 }}
    animate={{ opacity: 1, y: 0 }}
    transition={{ delay }}
    className="bg-bg-card rounded-card border border-color-border p-5"
  >
    {title && (
      <div className="flex items-center gap-2 mb-4">
        {Icon && <Icon className="h-4 w-4 text-text-secondary" />}
        <h3 className="text-sm font-semibold text-text-primary">{title}</h3>
      </div>
    )}
    {children}
  </motion.div>
));
Section.displayName = 'Section';

/* ── Demo Stats ──────────────────────────────────── */
const STATS = [
  { label: 'Transactions Tracked', value: '247', icon: IndianRupee },
  { label: 'Amount Tracked', value: '₹3,42,180', icon: TrendingUp },
  { label: 'Goals Completed', value: '2', icon: Target },
  { label: 'AI Conversations', value: '18', icon: MessageSquare },
];

const SESSIONS = [
  { device: 'Chrome — Windows', location: 'Mumbai, IN', lastActive: 'Now', current: true },
  { device: 'Safari — iPhone', location: 'Mumbai, IN', lastActive: '2 hours ago', current: false },
];

/* ── Main Profile Page ───────────────────────────── */
const Profile = () => {
  const dispatch = useDispatch();
  const { profile, isEditing, notifications } = useSelector((s) => s.profile);
  const [editData, setEditData] = useState(profile);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteText, setDeleteText] = useState('');
  const [activeSection, setActiveSection] = useState('all'); // mobile nav

  const handleEdit = useCallback(() => {
    setEditData(profile);
    dispatch(setEditing(true));
  }, [profile, dispatch]);

  const handleSave = useCallback(() => {
    dispatch(setProfile(editData));
    dispatch(setEditing(false));
  }, [editData, dispatch]);

  const handleCancel = useCallback(() => dispatch(setEditing(false)), [dispatch]);

  return (
    <PageLayout>
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-5 max-w-3xl mx-auto">
        {/* Header */}
        <div>
          <h1 className="text-xl font-bold text-text-primary">Profile & Settings</h1>
          <p className="text-text-secondary text-xs mt-0.5">Manage your account</p>
        </div>

        {/* ── Profile Header ─────────────────────── */}
        <Section delay={0}>
          <div className="flex items-center gap-4">
            <div className="relative">
              <Avatar name={`${profile.firstName} ${profile.lastName}`} size="xl" />
              <button className="absolute -bottom-1 -right-1 h-6 w-6 rounded-full bg-accent-green flex items-center justify-center shadow-md">
                <Camera className="h-3 w-3 text-white" />
              </button>
            </div>
            <div className="flex-1">
              <h2 className="text-lg font-bold text-text-primary">{profile.firstName} {profile.lastName}</h2>
              <p className="text-xs text-text-secondary">{profile.email}</p>
              <p className="text-[10px] text-text-secondary mt-0.5">Member since {new Date(profile.memberSince).toLocaleDateString('en-IN', { month: 'long', year: 'numeric' })}</p>
            </div>
            {!isEditing && (
              <Button variant="outline" size="sm" onClick={handleEdit}>
                <Pencil className="h-3 w-3" /> Edit
              </Button>
            )}
          </div>
        </Section>

        {/* ── Personal Info ──────────────────────── */}
        <Section title="Personal Information" icon={User} delay={0.05}>
          {isEditing ? (
            <div className="space-y-3">
              {[
                { key: 'firstName', label: 'First Name', type: 'text' },
                { key: 'lastName', label: 'Last Name', type: 'text' },
                { key: 'phone', label: 'Phone', type: 'tel' },
                { key: 'dob', label: 'Date of Birth', type: 'date' },
                { key: 'city', label: 'City', type: 'text' },
                { key: 'occupation', label: 'Occupation', type: 'text' },
              ].map(({ key, label, type }) => (
                <div key={key}>
                  <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1 block">{label}</label>
                  <input
                    type={type}
                    value={editData[key] || ''}
                    onChange={(e) => setEditData((d) => ({ ...d, [key]: e.target.value }))}
                    className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-1 focus:ring-accent-green/50"
                  />
                </div>
              ))}
              <div>
                <label className="text-[10px] uppercase tracking-wider text-text-secondary font-medium mb-1 block">Monthly Income Range</label>
                <select
                  value={editData.incomeRange}
                  onChange={(e) => setEditData((d) => ({ ...d, incomeRange: e.target.value }))}
                  className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-1 focus:ring-accent-green/50"
                >
                  {['₹5,000 – ₹10,000', '₹10,000 – ₹25,000', '₹25,000 – ₹50,000', '₹50,000 – ₹1,00,000', '₹1,00,000+'].map((r) => (
                    <option key={r} value={r}>{r}</option>
                  ))}
                </select>
              </div>
              <div className="flex gap-2 pt-2">
                <Button variant="primary" size="sm" onClick={handleSave}><Check className="h-3 w-3" /> Save</Button>
                <Button variant="ghost" size="sm" onClick={handleCancel}><X className="h-3 w-3" /> Cancel</Button>
              </div>
            </div>
          ) : (
            <div className="space-y-2">
              {[
                { icon: User, label: 'Name', value: `${profile.firstName} ${profile.lastName}` },
                { icon: Mail, label: 'Email', value: profile.email },
                { icon: Phone, label: 'Phone', value: profile.phone },
                { icon: Calendar, label: 'Date of Birth', value: profile.dob ? new Date(profile.dob).toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' }) : '—' },
                { icon: MapPin, label: 'City', value: profile.city },
                { icon: Briefcase, label: 'Occupation', value: profile.occupation },
                { icon: IndianRupee, label: 'Monthly Income', value: profile.incomeRange },
              ].map((item) => (
                <div key={item.label} className="flex items-center gap-3 py-2 px-3 rounded-btn bg-bg-primary">
                  <item.icon className="h-3.5 w-3.5 text-text-secondary flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <p className="text-[10px] text-text-secondary">{item.label}</p>
                    <p className="text-xs text-text-primary truncate">{item.value}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Section>

        {/* ── Financial Profile (Read-only) ──────── */}
        <Section title="Financial Profile" icon={TrendingUp} delay={0.1}>
          <div className="grid grid-cols-2 gap-2">
            {STATS.map((s) => (
              <div key={s.label} className="bg-bg-primary rounded-btn p-3 flex items-center gap-2.5">
                <s.icon className="h-4 w-4 text-accent-green flex-shrink-0" />
                <div>
                  <p className="text-sm font-bold tabular-nums text-text-primary">{s.value}</p>
                  <p className="text-[9px] text-text-secondary">{s.label}</p>
                </div>
              </div>
            ))}
          </div>
        </Section>

        {/* ── Security Settings ──────────────────── */}
        <Section title="Security" icon={Shield} delay={0.15}>
          <div className="space-y-3">
            <button className="w-full flex items-center justify-between py-2.5 px-3 rounded-btn bg-bg-primary hover:bg-bg-secondary transition-all">
              <div className="flex items-center gap-2">
                <Lock className="h-3.5 w-3.5 text-text-secondary" />
                <span className="text-xs text-text-primary">Change Password</span>
              </div>
              <ChevronRight className="h-3.5 w-3.5 text-text-secondary" />
            </button>
            <div className="flex items-center justify-between py-2.5 px-3 rounded-btn bg-bg-primary">
              <div className="flex items-center gap-2">
                <Smartphone className="h-3.5 w-3.5 text-text-secondary" />
                <span className="text-xs text-text-primary">Two-Factor Authentication</span>
              </div>
              <span className="px-1.5 py-0.5 rounded text-[9px] bg-accent-amber/15 text-accent-amber font-medium">Coming Soon</span>
            </div>
            <div className="space-y-2">
              <p className="text-[10px] uppercase tracking-wider text-text-secondary font-medium">Active Sessions</p>
              {SESSIONS.map((s) => (
                <div key={s.device} className="flex items-center justify-between py-2 px-3 rounded-btn bg-bg-primary">
                  <div>
                    <p className="text-xs text-text-primary">{s.device}</p>
                    <p className="text-[9px] text-text-secondary">{s.location} · {s.lastActive}</p>
                  </div>
                  {s.current ? (
                    <span className="px-1.5 py-0.5 rounded text-[9px] bg-accent-green/15 text-accent-green font-medium">This device</span>
                  ) : (
                    <button className="text-[10px] text-accent-red hover:underline">Logout</button>
                  )}
                </div>
              ))}
              <Button variant="ghost" size="sm" className="w-full"><LogOut className="h-3 w-3" /> Logout Other Sessions</Button>
            </div>
          </div>
        </Section>

        {/* ── Notification Settings ──────────────── */}
        <Section title="Notifications" icon={Bell} delay={0.2}>
          <div className="space-y-1">
            {[
              { key: 'fraudAlerts', label: 'Fraud Alerts', desc: 'Always on for your protection', disabled: true },
              { key: 'transactions', label: 'Transaction Notifications', desc: 'Get notified on every transaction' },
              { key: 'weeklySummary', label: 'Weekly Spending Summary', desc: 'Every Monday morning' },
              { key: 'monthlyReport', label: 'Monthly Financial Report', desc: 'Detailed monthly overview' },
              { key: 'goalReminders', label: 'Goal Reminders', desc: 'Stay on track with savings goals' },
              { key: 'aiInsights', label: 'AI Insights', desc: 'Smart spending tips' },
              { key: 'marketing', label: 'Marketing Emails', desc: 'Product updates and offers' },
            ].map((setting) => (
              <div key={setting.key} className="flex items-center justify-between py-2.5 px-3 rounded-btn hover:bg-bg-primary transition-all">
                <div>
                  <p className="text-xs text-text-primary">{setting.label}</p>
                  <p className="text-[9px] text-text-secondary">{setting.desc}</p>
                </div>
                <Switch
                  enabled={notifications[setting.key]}
                  onChange={() => dispatch(toggleNotification(setting.key))}
                  disabled={setting.disabled}
                />
              </div>
            ))}
          </div>
        </Section>

        {/* ── App Settings ───────────────────────── */}
        <Section title="App Settings" icon={Globe} delay={0.25}>
          <div className="space-y-2">
            {[
              { label: 'Currency', value: 'INR (₹)', locked: true },
              { label: 'Number Format', value: 'Indian (₹1,23,456)', locked: false },
              { label: 'Language', value: 'English', locked: false },
              { label: 'Theme', value: 'Dark', locked: true },
            ].map((s) => (
              <div key={s.label} className="flex items-center justify-between py-2 px-3 rounded-btn bg-bg-primary">
                <span className="text-xs text-text-primary">{s.label}</span>
                <div className="flex items-center gap-1.5">
                  <span className="text-xs text-text-secondary">{s.value}</span>
                  {s.locked && <Lock className="h-2.5 w-2.5 text-text-secondary/50" />}
                </div>
              </div>
            ))}
            <div className="flex gap-2 pt-2">
              <Button variant="outline" size="sm" className="flex-1"><Download className="h-3 w-3" /> Export Data</Button>
              <Button variant="ghost" size="sm" className="flex-1">Clear Cache</Button>
            </div>
          </div>
        </Section>

        {/* ── Danger Zone ────────────────────────── */}
        <Section title="Danger Zone" icon={AlertTriangle} delay={0.3}>
          <div className="border border-accent-red/20 rounded-btn p-4">
            <p className="text-xs text-text-primary font-medium mb-1">Delete Account</p>
            <p className="text-[10px] text-text-secondary mb-3">This will permanently delete all your data. 30-day recovery period.</p>
            <Button variant="ghost" size="sm" className="text-accent-red hover:bg-accent-red/10" onClick={() => setShowDeleteModal(true)}>
              <Trash2 className="h-3 w-3" /> Delete Account
            </Button>
          </div>
        </Section>

        {/* Delete Confirmation Modal */}
        <Modal isOpen={showDeleteModal} onClose={() => { setShowDeleteModal(false); setDeleteText(''); }} title="Delete Account" size="sm">
          <div className="space-y-4">
            <p className="text-xs text-text-secondary">This will permanently delete all your data including transactions, goals, and conversations.</p>
            <div className="bg-accent-red/5 border border-accent-red/20 rounded-btn p-3">
              <p className="text-[10px] text-accent-red font-medium">Type "DELETE" to confirm</p>
            </div>
            <input
              value={deleteText}
              onChange={(e) => setDeleteText(e.target.value)}
              placeholder="DELETE"
              className="w-full bg-bg-primary border border-color-border rounded-btn px-3 py-2 text-sm text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-accent-red/50"
            />
            <Button
              variant="ghost"
              className="w-full text-accent-red hover:bg-accent-red/10"
              disabled={deleteText !== 'DELETE'}
              onClick={() => { setShowDeleteModal(false); setDeleteText(''); }}
            >
              <Trash2 className="h-3.5 w-3.5" /> Permanently Delete Account
            </Button>
          </div>
        </Modal>
      </motion.div>
    </PageLayout>
  );
};

export default Profile;
