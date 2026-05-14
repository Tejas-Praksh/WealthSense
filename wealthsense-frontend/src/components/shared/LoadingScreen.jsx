import { motion } from 'framer-motion';
import { Spinner } from '@/components/ui/Spinner';

const LoadingScreen = () => {
  return (
    <div className="min-h-screen bg-bg-primary flex flex-col items-center justify-center">
      <motion.div
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3 }}
        className="flex flex-col items-center gap-6"
      >
        {/* Logo mark */}
        <div className="relative">
          <div className="w-16 h-16 rounded-full bg-accent-green/10 flex items-center justify-center border border-accent-green/20">
            <span className="text-2xl font-bold text-accent-green">₹</span>
          </div>
          <motion.div
            className="absolute inset-0 rounded-full border-2 border-accent-green/30"
            animate={{ scale: [1, 1.3, 1], opacity: [0.5, 0, 0.5] }}
            transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut' }}
          />
        </div>

        <Spinner size="md" />

        <p className="text-sm text-text-secondary">Loading WealthSense...</p>
      </motion.div>
    </div>
  );
};

export { LoadingScreen };
