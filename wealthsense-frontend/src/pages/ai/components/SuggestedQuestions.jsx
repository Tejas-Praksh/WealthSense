import { memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const defaultSuggestions = [
  { text: 'Why am I always broke? 💸', emoji: '💸' },
  { text: 'How can I save ₹5000/month?', emoji: '💰' },
  { text: 'Where is most of my money going?', emoji: '📊' },
  { text: 'Should I start a SIP?', emoji: '📈' },
  { text: 'How much can I invest this month?', emoji: '🤔' },
  { text: 'Help me make a budget plan', emoji: '📋' },
];

const contextSuggestions = {
  spending: [
    'Show me a category breakdown',
    'Which expenses can I cut?',
    'Compare with last month',
  ],
  saving: [
    'How much should I save monthly?',
    'Best savings account options?',
    'Set up auto-save rules',
  ],
  investing: [
    'What SIP amount suits me?',
    'Explain mutual funds simply',
    'Show tax-saving options',
  ],
  default: [
    'Tell me more about this',
    'What should I do next?',
    'Any other tips?',
  ],
};

const SuggestedQuestions = memo(({ onSelect, context, disabled }) => {
  const chips = context
    ? contextSuggestions[context] || contextSuggestions.default
    : defaultSuggestions.map((s) => s.text);

  return (
    <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-hide">
      <AnimatePresence mode="popLayout">
        {chips.map((text, i) => (
          <motion.button
            key={text}
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.9 }}
            transition={{ delay: i * 0.05 }}
            onClick={() => !disabled && onSelect(text)}
            disabled={disabled}
            className="flex-shrink-0 px-3 py-1.5 rounded-full text-xs font-medium bg-bg-card border border-color-border text-text-secondary hover:text-accent-green hover:border-accent-green/30 transition-all disabled:opacity-40 disabled:cursor-not-allowed whitespace-nowrap"
          >
            {text}
          </motion.button>
        ))}
      </AnimatePresence>
    </div>
  );
});

SuggestedQuestions.displayName = 'SuggestedQuestions';
export { SuggestedQuestions };
