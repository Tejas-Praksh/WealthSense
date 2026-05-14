import { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Mail, ArrowLeft, CheckCircle2 } from 'lucide-react';
import { AuthLayout } from '@/components/layout/AuthLayout';
import { Card, CardContent } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { validateEmail } from '@/utils/validators';
import { useForgotPasswordMutation } from '@/store/api/authApi';
import { ROUTES } from '@/utils/constants';
import logo from '@/assets/logo.svg';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [error, setError] = useState(null);
  const [sent, setSent] = useState(false);
  const [forgotPassword, { isLoading }] = useForgotPasswordMutation();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const emailErr = validateEmail(email);
    if (emailErr) {
      setError(emailErr);
      return;
    }
    setError(null);
    try {
      await forgotPassword(email).unwrap();
      setSent(true);
    } catch (err) {
      setError(err?.data?.message || 'Something went wrong. Please try again.');
    }
  };

  return (
    <AuthLayout>
      <Card animate={false} className="p-8">
        <CardContent>
          {/* Logo */}
          <div className="text-center mb-6">
            <div className="flex justify-center mb-4">
              <img src={logo} alt="WealthSense" className="h-10" />
            </div>
          </div>

          {sent ? (
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              className="text-center py-4"
            >
              <div className="mx-auto w-14 h-14 rounded-full bg-accent-green/10 flex items-center justify-center mb-4">
                <CheckCircle2 className="h-7 w-7 text-accent-green" />
              </div>
              <h2 className="text-lg font-semibold text-text-primary mb-2">
                Check your email
              </h2>
              <p className="text-sm text-text-secondary mb-6">
                We've sent a password reset link to <br />
                <span className="text-text-primary font-medium">{email}</span>
              </p>
              <Link to={ROUTES.LOGIN}>
                <Button variant="outline" className="w-full">
                  <ArrowLeft className="h-4 w-4" />
                  Back to Login
                </Button>
              </Link>
            </motion.div>
          ) : (
            <>
              <div className="text-center mb-6">
                <h2 className="text-lg font-semibold text-text-primary mb-1">
                  Reset your password
                </h2>
                <p className="text-sm text-text-secondary">
                  Enter your email and we'll send you a reset link
                </p>
              </div>

              {error && (
                <div className="mb-4 p-3 rounded-btn bg-accent-red/10 border border-accent-red/20 text-accent-red text-sm">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4">
                <Input
                  id="forgot-email"
                  name="email"
                  type="email"
                  label="Email address"
                  placeholder="arjun@example.com"
                  icon={Mail}
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value);
                    if (error) setError(null);
                  }}
                  error={null}
                />

                <Button
                  type="submit"
                  isLoading={isLoading}
                  className="w-full"
                  size="lg"
                >
                  {!isLoading && 'Send Reset Link'}
                  {isLoading && 'Sending...'}
                </Button>
              </form>

              <p className="mt-5 text-center text-sm text-text-secondary">
                <Link
                  to={ROUTES.LOGIN}
                  className="text-accent-green hover:text-accent-green/80 font-medium transition-colors inline-flex items-center gap-1"
                >
                  <ArrowLeft className="h-3 w-3" />
                  Back to Login
                </Link>
              </p>
            </>
          )}
        </CardContent>
      </Card>
    </AuthLayout>
  );
};

export default ForgotPassword;
