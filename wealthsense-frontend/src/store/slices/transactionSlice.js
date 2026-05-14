import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  transactions: [],
  selectedTransaction: null,
  isDrawerOpen: false,
  filters: {
    dateRange: 'this_month',
    type: 'all',
    category: 'all',
    search: '',
    minAmount: '',
    maxAmount: '',
  },
  pagination: {
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    hasMore: true,
  },
};

const transactionSlice = createSlice({
  name: 'transactions',
  initialState,
  reducers: {
    setTransactions: (state, action) => {
      state.transactions = action.payload;
    },
    appendTransactions: (state, action) => {
      state.transactions = [...state.transactions, ...action.payload];
    },
    addTransaction: (state, action) => {
      state.transactions.unshift(action.payload);
    },
    updateTransaction: (state, action) => {
      const index = state.transactions.findIndex((t) => t.id === action.payload.id);
      if (index !== -1) {
        state.transactions[index] = action.payload;
      }
    },
    setSelectedTransaction: (state, action) => {
      state.selectedTransaction = action.payload;
      state.isDrawerOpen = !!action.payload;
    },
    toggleDrawer: (state) => {
      state.isDrawerOpen = !state.isDrawerOpen;
      if (!state.isDrawerOpen) state.selectedTransaction = null;
    },
    setFilter: (state, action) => {
      const { key, value } = action.payload;
      state.filters[key] = value;
      state.pagination.page = 0;
    },
    setFilters: (state, action) => {
      state.filters = { ...state.filters, ...action.payload };
      state.pagination.page = 0;
    },
    clearFilters: (state) => {
      state.filters = initialState.filters;
      state.pagination.page = 0;
    },
    setPage: (state, action) => {
      state.pagination.page = action.payload;
    },
    setPagination: (state, action) => {
      state.pagination = { ...state.pagination, ...action.payload };
    },
  },
});

export const {
  setTransactions,
  appendTransactions,
  addTransaction,
  updateTransaction,
  setSelectedTransaction,
  toggleDrawer,
  setFilter,
  setFilters,
  clearFilters,
  setPage,
  setPagination,
} = transactionSlice.actions;
export default transactionSlice.reducer;
