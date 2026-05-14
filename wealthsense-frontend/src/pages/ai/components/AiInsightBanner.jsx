import { memo, useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Lightbulb } from 'lucide-react';

const INSIGHTS = [
  '💡 You have ₹340 more than last week at this time. Keep it up!',
  '💡 Your food spending dropped 18% — great discipline this week!',
  '💡 At this rate, you can save ₹2,000 by month end.',
  '💡 Tip: Moving ₹1,000 to a savings account earns ₹60/year interest.',
  '💡 You have 3 subscriptions totaling ₹1,647/month. Review them?',
];

const AiInsightBanner = memo(() => {
  const dayIndex = new Date().getDate() % INSIGHTS.length;
  const storageKey = 'ws_ai_insight_dismissed';
  const [dismissed, setDismissed] = useState(() => {
    try {
      return localStorage.getItem(storageKey) === new Date().toDateString();
    } catch { return false; }
  });

  const handleDismiss = useCallback(() => {
    setDismissed(true);
    try { localStorage.setItem(storageKey, new Date().toDateString()); } catch {}
  }, []);

  return (
    <AnimatePresence>
      {!dismissed && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          exit={{ opacity: 0, height: 0 }}
          className="bg-gradient-to-r from-purple-500/10 via-blue-500/10 to-cyan-500/10 border border-purple-500/20 rounded-card px-4 py-2.5 flex items-center gap-3"
        >
          <Lightbulb className="h-4 w-4 text-accent-amber flex-shrink-0" />
          <p className="text-xs text-text-primary flex-1">{INSIGHTS[dayIndex]}</p>
          <button
            onClick={handleDismiss}
            className="p-1 rounded-md text-text-secondary hover:text-text-primary hover:bg-bg-primary transition-colors flex-shrink-0"
          >
            <X className="h-3.5 w-3.5" />
          </button>
        </motion.div>
      )}
    </AnimatePresence>
  );
});

AiInsightBanner.displayName = 'AiInsightBanner';
export { AiInsightBanner };
