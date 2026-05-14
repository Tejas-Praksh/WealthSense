import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Mail, Lock, ArrowRight, Chrome, Rocket } from 'lucide-react';
import { useDispatch } from 'react-redux';
import { AuthLayout } from '@/components/layout/AuthLayout';
import { Card, CardContent } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { useAuth } from '@/hooks/useAuth';
import { validateEmail, validatePassword } from '@/utils/validators';
import { setCredentials } from '@/store/slices/authSlice';
import { ROUTES } from '@/utils/constants';
import logo from '@/assets/logo.svg';

const DEMO_USER = {
  user: {
    id: 'demo-001',
    firstName: 'Raju',
    lastName: 'Sharma',
    email: 'raju.sharma@example.com',
    phone: '9876543210',
  },
  accessToken: 'demo-access-token-wealthsense',
};

const Login = () => {
  const { login, isLoading, error } = useAuth();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (errors[e.target.name]) {
      setErrors({ ...errors, [e.target.name]: null });
    }
  };

  const validate = () => {
    const newErrors = {};
    const emailErr = validateEmail(form.email);
    const passErr = validatePassword(form.password);
    if (emailErr) newErrors.email = emailErr;
    if (passErr) newErrors.password = passErr;
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      await login(form);
    } catch {
      // Error is set in useAuth
    }
  };

  const handleDemoLogin = () => {
    dispatch(setCredentials(DEMO_USER));
    navigate(ROUTES.DASHBOARD);
  };

  return (
    <AuthLayout>
      <Card animate={false} className="p-8">
        <CardContent>
          {/* Logo + Tagline */}
          <div className="text-center mb-8">
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: 0.1 }}
              className="flex justify-center mb-4"
            >
              <img src={logo} alt="WealthSense" className="h-10" />
            </motion.div>
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.2 }}
              className="text-text-secondary text-sm"
            >
              Your financial brain
            </motion.p>
          </div>

          {/* API Error */}
          {error && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              className="mb-4 p-3 rounded-btn bg-accent-red/10 border border-accent-red/20 text-accent-red text-sm"
            >
              {error}
            </motion.div>
          )}

          {/* ===== Demo Mode CTA ===== */}
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.25 }}
            className="mb-6"
          >
            <button
              type="button"
              onClick={handleDemoLogin}
              className="w-full group relative overflow-hidden rounded-btn p-[1px] transition-all duration-300 hover:shadow-lg hover:shadow-purple-500/20"
            >
              {/* Animated gradient border */}
              <div className="absolute inset-0 rounded-btn bg-gradient-to-r from-purple-500 via-blue-500 to-cyan-500 opacity-80 group-hover:opacity-100 transition-opacity" />
              <div className="relative flex items-center justify-center gap-2.5 rounded-[7px] bg-bg-card px-4 py-3 transition-colors group-hover:bg-bg-card/90">
                <Rocket className="h-4.5 w-4.5 text-purple-400 group-hover:text-purple-300 transition-colors" />
                <span className="font-semibold text-sm text-text-primary">
                  Try Live Demo
                </span>
                <ArrowRight className="h-3.5 w-3.5 text-text-secondary group-hover:text-purple-300 group-hover:translate-x-0.5 transition-all" />
              </div>
            </button>
            <p className="text-[11px] text-text-secondary text-center mt-1.5">
              No account needed — explore the full app instantly
            </p>
          </motion.div>

          {/* Divider */}
          <div className="relative mb-5">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-color-border" />
            </div>
            <div className="relative flex justify-center text-xs">
              <span className="bg-bg-card px-3 text-text-secondary">or sign in</span>
            </div>
          </div>

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              id="login-email"
              name="email"
              type="email"
              label="Email address"
              placeholder="arjun@example.com"
              icon={Mail}
              value={form.email}
              onChange={handleChange}
              error={errors.email}
              autoComplete="email"
            />

            <Input
              id="login-password"
              name="password"
              type="password"
              label="Password"
              placeholder="Enter your password"
              icon={Lock}
              value={form.password}
              onChange={handleChange}
              error={errors.password}
              autoComplete="current-password"
            />

            <div className="flex justify-end">
              <Link
                to={ROUTES.FORGOT_PASSWORD}
                className="text-xs text-accent-green hover:text-accent-green/80 transition-colors"
              >
                Forgot password?
              </Link>
            </div>

            <Button
              type="submit"
              isLoading={isLoading}
              className="w-full"
              size="lg"
            >
              {!isLoading && (
                <>
                  Log In
                  <ArrowRight className="h-4 w-4" />
                </>
              )}
              {isLoading && 'Signing in...'}
            </Button>
          </form>

          {/* Divider */}
          <div className="relative my-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-color-border" />
            </div>
            <div className="relative flex justify-center text-xs">
              <span className="bg-bg-card px-3 text-text-secondary">or continue with</span>
            </div>
          </div>

          {/* Google OAuth */}
          <Button
            type="button"
            variant="outline"
            className="w-full"
            size="lg"
            onClick={handleDemoLogin}
          >
            <Chrome className="h-4 w-4" />
            Continue with Google
          </Button>

          {/* Register link */}
          <p className="mt-6 text-center text-sm text-text-secondary">
            Don't have an account?{' '}
            <Link
              to={ROUTES.REGISTER}
              className="text-accent-green hover:text-accent-green/80 font-medium transition-colors"
            >
              Create one
            </Link>
          </p>
        </CardContent>
      </Card>
    </AuthLayout>
  );
};

export default Login;
