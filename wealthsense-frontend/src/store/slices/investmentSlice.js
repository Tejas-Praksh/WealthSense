import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  investments: [],
  goals: [],
  sipCalculation: {
    monthly: 2000,
    rate: 12,
    years: 10,
  },
  taxSaving: null,
  activeTab: 'overview',
};

const investmentSlice = createSlice({
  name: 'investments',
  initialState,
  reducers: {
    setActiveTab: (state, action) => {
      state.activeTab = action.payload;
    },
    setInvestments: (state, action) => {
      state.investments = action.payload;
    },
    addInvestment: (state, action) => {
      state.investments.unshift(action.payload);
    },
    setGoals: (state, action) => {
      state.goals = action.payload;
    },
    addGoal: (state, action) => {
      state.goals.unshift(action.payload);
    },
    updateGoal: (state, action) => {
      const idx = state.goals.findIndex((g) => g.id === action.payload.id);
      if (idx !== -1) state.goals[idx] = action.payload;
    },
    deleteGoal: (state, action) => {
      state.goals = state.goals.filter((g) => g.id !== action.payload);
    },
    setSipInput: (state, action) => {
      state.sipCalculation = { ...state.sipCalculation, ...action.payload };
    },
    setTaxSaving: (state, action) => {
      state.taxSaving = action.payload;
    },
  },
});

export const {
  setActiveTab,
  setInvestments,
  addInvestment,
  setGoals,
  addGoal,
  updateGoal,
  deleteGoal,
  setSipInput,
  setTaxSaving,
} = investmentSlice.actions;
export default investmentSlice.reducer;
