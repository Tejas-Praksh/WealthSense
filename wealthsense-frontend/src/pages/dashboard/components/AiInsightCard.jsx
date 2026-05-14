import { useState, memo } from 'react';
import { motion } from 'framer-motion';
import { Brain, Sparkles, RefreshCw, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/Button';
import { Skeleton } from '@/components/ui/SkeletonLoader';
import { ROUTES } from '@/utils/constants';

const fallbackInsights = [
  "💡 You spent 40% more on food this week. Cooking at home 2 days could save ₹800/month.",
  "💡 Your transport spending dropped 15% — great job! Consider investing the savings in a liquid fund.",
  "💡 You have 3 recurring subscriptions totalling ₹897/month. Review if you still use all of them.",
  "💡 Setting aside ₹100/day into a savings goal would give you ₹36,500 by year end.",
];

const AiInsightCard = memo(({ insight, isLoading, onRefresh }) => {
  const navigate = useNavigate();
  const [currentInsight, setCurrentInsight] = useState(0);

  const displayText = insight || fallbackInsights[currentInsight];

  const handleRefresh = () => {
    if (onRefresh) {
      onRefresh();
    } else {
      setCurrentInsight((prev) => (prev + 1) % fallbackInsights.length);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: 0.2 }}
      className="relative rounded-card overflow-hidden"
    >
      {/* Purple gradient border effect */}
      <div
        className="absolute inset-0 rounded-card"
        style={{
          background: 'linear-gradient(135deg, #A855F7, #6366F1, #3B82F6)',
          padding: '1px',
        }}
      />
      <div className="relative bg-bg-card rounded-card p-5 m-[1px]">
        {/* Header */}
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <div className="relative">
              <Brain className="h-5 w-5 text-purple-400" />
              <motion.div
                animate={{ scale: [1, 1.3, 1], opacity: [1, 0.6, 1] }}
                transition={{ duration: 2, repeat: Infinity }}
                className="absolute -top-1 -right-1"
              >
                <Sparkles className="h-3 w-3 text-amber-400" />
              </motion.div>
            </div>
            <h3 className="text-sm font-semibold text-text-primary">AI Insight</h3>
          </div>
          <button
            onClick={handleRefresh}
            className="p-1.5 rounded-md text-text-secondary hover:text-text-primary hover:bg-bg-primary transition-colors"
            title="Get new insight"
          >
            <RefreshCw className="h-3.5 w-3.5" />
          </button>
        </div>

        {/* Insight text */}
        {isLoading ? (
          <div className="space-y-2 mb-4">
            <Skeleton className="h-3 w-full" />
            <Skeleton className="h-3 w-4/5" />
          </div>
        ) : (
          <motion.p
            key={displayText}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="text-sm text-text-secondary leading-relaxed mb-4"
          >
            {displayText}
          </motion.p>
        )}

        {/* CTA */}
        <Button
          variant="ghost"
          size="sm"
          onClick={() => navigate(ROUTES.AI_ADVISOR)}
          className="text-purple-400 hover:text-purple-300 hover:bg-purple-500/10 border-purple-500/20"
        >
          Ask AI for more
          <ArrowRight className="h-3.5 w-3.5" />
        </Button>
      </div>
    </motion.div>
  );
});

AiInsightCard.displayName = 'AiInsightCard';
export { AiInsightCard };
