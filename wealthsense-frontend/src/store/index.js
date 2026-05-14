import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import transactionReducer from './slices/transactionSlice';
import notificationReducer from './slices/notificationSlice';
import uiReducer from './slices/uiSlice';
import aiReducer from './slices/aiSlice';
import investmentReducer from './slices/investmentSlice';
import splitReducer from './slices/splitSlice';
import profileReducer from './slices/profileSlice';
import { apiSlice } from './api/apiSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    transactions: transactionReducer,
    notifications: notificationReducer,
    ui: uiReducer,
    ai: aiReducer,
    investments: investmentReducer,
    split: splitReducer,
    profile: profileReducer,
    [apiSlice.reducerPath]: apiSlice.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(apiSlice.middleware),
  devTools: import.meta.env.DEV,
});

