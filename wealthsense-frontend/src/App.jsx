import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ErrorBoundary } from '@/components/shared/ErrorBoundary';
import { ProtectedRoute } from '@/components/shared/ProtectedRoute';
import { LoadingScreen } from '@/components/shared/LoadingScreen';
import { ROUTES } from '@/utils/constants';

// Lazy load pages for code splitting
const Login = lazy(() => import('@/pages/auth/Login'));
const Register = lazy(() => import('@/pages/auth/Register'));
const ForgotPassword = lazy(() => import('@/pages/auth/ForgotPassword'));
const Dashboard = lazy(() => import('@/pages/dashboard/Dashboard'));
const Transactions = lazy(() => import('@/pages/transactions/Transactions'));
const AiAdvisor = lazy(() => import('@/pages/ai/AiAdvisor'));
const Investments = lazy(() => import('@/pages/investments/Investments'));
const SplitExpenses = lazy(() => import('@/pages/split/SplitExpenses'));
const Profile = lazy(() => import('@/pages/profile/Profile'));

function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Suspense fallback={<LoadingScreen />}>
          <Routes>
            {/* Public routes */}
            <Route path={ROUTES.LOGIN} element={<Login />} />
            <Route path={ROUTES.REGISTER} element={<Register />} />
            <Route path={ROUTES.FORGOT_PASSWORD} element={<ForgotPassword />} />

            {/* Protected routes */}
            <Route
              path={ROUTES.DASHBOARD}
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path={ROUTES.TRANSACTIONS}
              element={
                <ProtectedRoute>
                  <Transactions />
                </ProtectedRoute>
              }
            />
            <Route
              path={ROUTES.AI_ADVISOR}
              element={
                <ProtectedRoute>
                  <AiAdvisor />
                </ProtectedRoute>
              }
            />
            <Route
              path={ROUTES.INVESTMENTS}
              element={
                <ProtectedRoute>
                  <Investments />
                </ProtectedRoute>
              }
            />
            <Route
              path={ROUTES.SPLIT_EXPENSES}
              element={
                <ProtectedRoute>
                  <SplitExpenses />
                </ProtectedRoute>
              }
            />
            <Route
              path={ROUTES.PROFILE}
              element={
                <ProtectedRoute>
                  <Profile />
                </ProtectedRoute>
              }
            />

            {/* Root redirect */}
            <Route path={ROUTES.HOME} element={<Navigate to={ROUTES.DASHBOARD} replace />} />

            {/* 404 fallback */}
            <Route
              path="*"
              element={
                <div className="min-h-screen bg-bg-primary flex items-center justify-center">
                  <div className="text-center">
                    <h1 className="text-6xl font-bold text-accent-green mb-2">404</h1>
                    <p className="text-text-secondary">Page not found</p>
                  </div>
                </div>
              }
            />
          </Routes>
        </Suspense>
      </BrowserRouter>
    </ErrorBoundary>
  );
}

export default App;
