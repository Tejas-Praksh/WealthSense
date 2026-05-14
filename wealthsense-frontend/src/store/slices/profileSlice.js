import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  profile: {
    firstName: 'Raju',
    lastName: 'Sharma',
    email: 'raju.sharma@example.com',
    phone: '98XXXXXX90',
    dob: '2000-05-15',
    city: 'Mumbai',
    occupation: 'Student',
    incomeRange: '₹5,000 – ₹10,000',
    memberSince: '2025-09-01',
    avatar: null,
  },
  isEditing: false,
  isSaving: false,
  notifications: {
    fraudAlerts: true,
    transactions: true,
    weeklySummary: true,
    monthlyReport: true,
    goalReminders: true,
    aiInsights: true,
    marketing: false,
  },
};

const profileSlice = createSlice({
  name: 'profile',
  initialState,
  reducers: {
    setProfile: (state, action) => { state.profile = { ...state.profile, ...action.payload }; },
    setEditing: (state, action) => { state.isEditing = action.payload; },
    setSaving: (state, action) => { state.isSaving = action.payload; },
    toggleNotification: (state, action) => {
      const key = action.payload;
      if (key !== 'fraudAlerts') state.notifications[key] = !state.notifications[key];
    },
  },
});

export const { setProfile, setEditing, setSaving, toggleNotification } = profileSlice.actions;
export default profileSlice.reducer;
