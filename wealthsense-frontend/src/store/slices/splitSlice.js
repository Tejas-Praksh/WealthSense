import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  groups: [],
  activeGroup: null,
  balances: { owed: [], owing: [] },
  isAddExpenseOpen: false,
};

const splitSlice = createSlice({
  name: 'split',
  initialState,
  reducers: {
    setGroups: (state, action) => { state.groups = action.payload; },
    setActiveGroup: (state, action) => { state.activeGroup = action.payload; },
    addGroup: (state, action) => { state.groups.unshift(action.payload); },
    setBalances: (state, action) => { state.balances = action.payload; },
    toggleAddExpense: (state, action) => { state.isAddExpenseOpen = action.payload ?? !state.isAddExpenseOpen; },
    addExpenseToGroup: (state, action) => {
      const g = state.groups.find((g) => g.id === action.payload.groupId);
      if (g) g.expenses.unshift(action.payload.expense);
    },
    settleBalance: (state, action) => {
      state.balances.owed = state.balances.owed.filter((b) => b.id !== action.payload);
      state.balances.owing = state.balances.owing.filter((b) => b.id !== action.payload);
    },
  },
});

export const { setGroups, setActiveGroup, addGroup, setBalances, toggleAddExpense, addExpenseToGroup, settleBalance } = splitSlice.actions;
export default splitSlice.reducer;
