import { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Mail, Lock, User, Phone, ArrowRight, Check } from 'lucide-react';
import { AuthLayout } from '@/components/layout/AuthLayout';
import { Card, CardContent } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { ProgressBar } from '@/components/ui/ProgressBar';
import { useAuth } from '@/hooks/useAuth';
import {
  validateEmail,
  validatePassword,
  validatePhone,
  validateName,
  validatePasswordMatch,
  getPasswordStrength,
} from '@/utils/validators';
import { PASSWORD_STRENGTH, ROUTES } from '@/utils/constants';
import logo from '@/assets/logo.svg';

const Register = () => {
  const { register, isLoading, error } = useAuth();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    acceptTerms: false,
  });
  const [errors, setErrors] = useState({});

  const passwordStrength = getPasswordStrength(form.password);
  const strengthInfo = passwordStrength ? PASSWORD_STRENGTH[passwordStrength] : null;

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value });
    if (errors[name]) {
      setErrors({ ...errors, [name]: null });
    }
  };

  const validate = () => {
    const newErrors = {};
    const checks = [
      ['firstName', validateName(form.firstName, 'First name')],
      ['lastName', validateName(form.lastName, 'Last name')],
      ['email', validateEmail(form.email)],
      ['phone', validatePhone(form.phone)],
      ['password', validatePassword(form.password)],
      ['confirmPassword', validatePasswordMatch(form.password, form.confirmPassword)],
    ];

    checks.forEach(([field, err]) => {
      if (err) newErrors[field] = err;
    });

    if (!form.acceptTerms) {
      newErrors.acceptTerms = 'You must accept the terms and conditions';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      await register({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        phone: form.phone,
        password: form.password,
      });
    } catch {
      // Error set in useAuth
    }
  };

  return (
    <AuthLayout>
      <Card animate={false} className="p-8">
        <CardContent>
          {/* Logo + Tagline */}
          <div className="text-center mb-6">
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: 0.1 }}
              className="flex justify-center mb-3"
            >
              <img src={logo} alt="WealthSense" className="h-10" />
            </motion.div>
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.15 }}
              className="text-text-secondary text-sm"
            >
              Start your financial journey
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

          {/* Register Form */}
          <form onSubmit={handleSubmit} className="space-y-3.5">
            {/* Name row */}
            <div className="grid grid-cols-2 gap-3">
              <Input
                id="register-firstName"
                name="firstName"
                label="First name"
                placeholder="Arjun"
                icon={User}
                value={form.firstName}
                onChange={handleChange}
                error={errors.firstName}
              />
              <Input
                id="register-lastName"
                name="lastName"
                label="Last name"
                placeholder="Patel"
                icon={User}
                value={form.lastName}
                onChange={handleChange}
                error={errors.lastName}
              />
            </div>

            <Input
              id="register-email"
              name="email"
              type="email"
              label="Email address"
              placeholder="arjun.patel@example.com"
              icon={Mail}
              value={form.email}
              onChange={handleChange}
              error={errors.email}
              autoComplete="email"
            />

            <Input
              id="register-phone"
              name="phone"
              type="tel"
              label="Phone number"
              placeholder="9876543210"
              icon={Phone}
              value={form.phone}
              onChange={handleChange}
              error={errors.phone}
            />

            <div>
              <Input
                id="register-password"
                name="password"
                type="password"
                label="Password"
                placeholder="Minimum 8 characters"
                icon={Lock}
                value={form.password}
                onChange={handleChange}
                error={errors.password}
                autoComplete="new-password"
              />
              {/* Password strength indicator */}
              {form.password && strengthInfo && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  className="mt-2"
                >
                  <ProgressBar
                    value={passwordStrength === 'WEAK' ? 33 : passwordStrength === 'MEDIUM' ? 66 : 100}
                    color={passwordStrength === 'WEAK' ? 'red' : passwordStrength === 'MEDIUM' ? 'amber' : 'green'}
                    size="sm"
                    label={`Password strength: ${strengthInfo.label}`}
                  />
                </motion.div>
              )}
            </div>

            <Input
              id="register-confirmPassword"
              name="confirmPassword"
              type="password"
              label="Confirm password"
              placeholder="Re-enter your password"
              icon={Lock}
              value={form.confirmPassword}
              onChange={handleChange}
              error={errors.confirmPassword}
              autoComplete="new-password"
            />

            {/* Terms checkbox */}
            <div className="flex items-start gap-2 pt-1">
              <input
                id="register-terms"
                type="checkbox"
                name="acceptTerms"
                checked={form.acceptTerms}
                onChange={handleChange}
                className="mt-1 h-4 w-4 rounded border-color-border bg-bg-primary text-accent-green focus:ring-accent-green focus:ring-offset-bg-primary accent-accent-green"
              />
              <label htmlFor="register-terms" className="text-xs text-text-secondary leading-relaxed">
                I agree to the{' '}
                <span className="text-accent-green cursor-pointer hover:underline">
                  Terms of Service
                </span>{' '}
                and{' '}
                <span className="text-accent-green cursor-pointer hover:underline">
                  Privacy Policy
                </span>
              </label>
            </div>
            {errors.acceptTerms && (
              <p className="text-xs text-accent-red">{errors.acceptTerms}</p>
            )}

            <Button
              type="submit"
              isLoading={isLoading}
              className="w-full"
              size="lg"
            >
              {!isLoading && (
                <>
                  Create Account
                  <ArrowRight className="h-4 w-4" />
                </>
              )}
              {isLoading && 'Creating account...'}
            </Button>
          </form>

          {/* Login link */}
          <p className="mt-5 text-center text-sm text-text-secondary">
            Already have an account?{' '}
            <Link
              to={ROUTES.LOGIN}
              className="text-accent-green hover:text-accent-green/80 font-medium transition-colors"
            >
              Log in
            </Link>
          </p>
        </CardContent>
      </Card>
    </AuthLayout>
  );
};

export default Register;
