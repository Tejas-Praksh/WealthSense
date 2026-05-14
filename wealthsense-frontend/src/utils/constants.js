// API Gateway base URL
export const API_BASE_URL = '/api';
export const WS_URL = 'ws://localhost:8080/ws';

// Route paths
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  FORGOT_PASSWORD: '/forgot-password',
  DASHBOARD: '/dashboard',
  TRANSACTIONS: '/transactions',
  AI_ADVISOR: '/ai',
  INVESTMENTS: '/investments',
  SPLIT_EXPENSES: '/split',
  PROFILE: '/profile',
};

// Transaction types
export const TRANSACTION_TYPES = {
  CREDIT: 'CREDIT',
  DEBIT: 'DEBIT',
  TRANSFER: 'TRANSFER',
};

// Transaction statuses
export const TRANSACTION_STATUS = {
  COMPLETED: 'COMPLETED',
  PENDING: 'PENDING',
  FAILED: 'FAILED',
  FLAGGED: 'FLAGGED',
};

// Badge color mappings
export const BADGE_COLORS = {
  CREDIT: { bg: 'bg-accent-green/15', text: 'text-accent-green', border: 'border-accent-green/30' },
  DEBIT: { bg: 'bg-accent-red/15', text: 'text-accent-red', border: 'border-accent-red/30' },
  TRANSFER: { bg: 'bg-accent-blue/15', text: 'text-accent-blue', border: 'border-accent-blue/30' },
  PENDING: { bg: 'bg-accent-amber/15', text: 'text-accent-amber', border: 'border-accent-amber/30' },
  FLAGGED: { bg: 'bg-accent-red/15', text: 'text-accent-red', border: 'border-accent-red/30' },
  COMPLETED: { bg: 'bg-accent-green/15', text: 'text-accent-green', border: 'border-accent-green/30' },
  FAILED: { bg: 'bg-accent-red/15', text: 'text-accent-red', border: 'border-accent-red/30' },
};

// Categories
export const EXPENSE_CATEGORIES = [
  'Food & Dining',
  'Transport',
  'Shopping',
  'Entertainment',
  'Education',
  'Bills & Utilities',
  'Health',
  'Travel',
  'Groceries',
  'Rent',
  'Other',
];

// Password strength levels
export const PASSWORD_STRENGTH = {
  WEAK: { label: 'Weak', color: 'bg-accent-red', width: '33%' },
  MEDIUM: { label: 'Medium', color: 'bg-accent-amber', width: '66%' },
  STRONG: { label: 'Strong', color: 'bg-accent-green', width: '100%' },
};

// Token storage keys
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'ws_access_token',
  REFRESH_TOKEN: 'ws_refresh_token',
  USER: 'ws_user',
};
