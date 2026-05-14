import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  LayoutDashboard,
  ArrowRightLeft,
  Brain,
  TrendingUp,
  Users,
  User,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { ROUTES } from '@/utils/constants';

const navItems = [
  { path: ROUTES.DASHBOARD, label: 'Dashboard', icon: LayoutDashboard },
  { path: ROUTES.TRANSACTIONS, label: 'Transactions', icon: ArrowRightLeft },
  { path: ROUTES.AI_ADVISOR, label: 'AI Advisor', icon: Brain },
  { path: ROUTES.INVESTMENTS, label: 'Investments', icon: TrendingUp },
  { path: ROUTES.SPLIT_EXPENSES, label: 'Split', icon: Users },
  { path: ROUTES.PROFILE, label: 'Profile', icon: User },
];

const Sidebar = () => {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();

  return (
    <>
      {/* Desktop sidebar */}
      <motion.aside
        initial={false}
        animate={{ width: collapsed ? 72 : 240 }}
        transition={{ duration: 0.2, ease: 'easeInOut' }}
        className="hidden md:flex fixed left-0 top-16 bottom-0 bg-bg-secondary border-r border-color-border z-30 flex-col"
      >
        <nav className="flex-1 py-4 px-2 space-y-1">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            const Icon = item.icon;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={cn(
                  'flex items-center gap-3 px-3 py-2.5 rounded-btn text-sm font-medium transition-all duration-200',
                  isActive
                    ? 'bg-accent-green/10 text-accent-green border border-accent-green/20'
                    : 'text-text-secondary hover:text-text-primary hover:bg-bg-primary border border-transparent'
                )}
              >
                <Icon className={cn('h-5 w-5 flex-shrink-0', isActive && 'text-accent-green')} />
                <AnimatePresence>
                  {!collapsed && (
                    <motion.span
                      initial={{ opacity: 0, width: 0 }}
                      animate={{ opacity: 1, width: 'auto' }}
                      exit={{ opacity: 0, width: 0 }}
                      className="overflow-hidden whitespace-nowrap"
                    >
                      {item.label}
                    </motion.span>
                  )}
                </AnimatePresence>
              </Link>
            );
          })}
        </nav>

        <button
          onClick={() => setCollapsed(!collapsed)}
          className="flex items-center justify-center h-10 border-t border-color-border text-text-secondary hover:text-text-primary transition-colors"
        >
          {collapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
        </button>
      </motion.aside>

      {/* Mobile bottom navigation */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 h-16 bg-bg-secondary/95 backdrop-blur-md border-t border-color-border z-40 flex items-center justify-around px-2 safe-area-bottom">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path;
          const Icon = item.icon;
          return (
            <Link
              key={item.path}
              to={item.path}
              className={cn(
                'flex flex-col items-center justify-center gap-0.5 py-1 px-3 rounded-lg transition-colors min-w-[44px] min-h-[44px]',
                isActive ? 'text-accent-green' : 'text-text-secondary'
              )}
            >
              <Icon className="h-5 w-5" />
              {/* Active indicator dot */}
              {isActive && (
                <motion.div
                  layoutId="mobileNavIndicator"
                  className="h-1 w-1 rounded-full bg-accent-green"
                  transition={{ type: 'spring', stiffness: 400, damping: 30 }}
                />
              )}
            </Link>
          );
        })}
      </nav>
    </>
  );
};

export { Sidebar };
