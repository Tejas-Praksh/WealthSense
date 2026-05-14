import { useState, useRef, useEffect, useCallback, memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useSelector, useDispatch } from 'react-redux';
import ReactMarkdown from 'react-markdown';
import {
  Send, Copy, ThumbsUp, ThumbsDown, Brain, Sparkles, Mic, Zap, Lock,
} from 'lucide-react';
import {
  addUserMessage, addAiMessage, startNewConversation, setTyping,
} from '@/store/slices/aiSlice';
import { formatCurrency } from '@/utils/formatters';
import { SuggestedQuestions } from './SuggestedQuestions';
import { FinancialContextCard } from './FinancialContextCard';
import { cn } from '@/lib/utils';

/* ── Demo AI responses ─────────────────────────────── */
const AI_RESPONSES = {
  'Why am I always broke? 💸': {
    content: `Looking at your May data, here's why you feel broke:\n\n**Your top 3 spending categories:**\n- 🍕 Food & Dining: **₹1,200** (33% of spending)\n- 🛍️ Shopping: **₹3,100** (Amazon + Flipkart)\n- 📱 Bills: **₹2,400** (recharge + electricity)\n\nYour food spending is **₹600 more** than the average college student. Cooking at home just 3 days a week could save you **₹600/month** — that's **₹7,200 a year**!\n\nWant me to create a meal planning budget?`,
    type: 'text',
    context: 'spending',
  },
  'How can I save ₹5000/month?': {
    content: `Here's a realistic plan to save ₹5,000/month from your ₹6,000 income:\n\n**Current spending: ₹3,660**\n\n1. 🍕 **Cut food to ₹800** (cook 4 days/week) → Save ₹400\n2. 🚗 **Walk/cycle short distances** → Save ₹200\n3. 🛍️ **No-buy challenge** for 2 months → Save ₹1,500\n4. 📱 **Switch to ₹199 plan** → Save ₹100\n\n**Total savings possible: ₹2,200/month**\n\nCombined with your freelance income (₹2,500–5,000), you can absolutely save ₹5,000!\n\n> 💡 Pro tip: Set up auto-transfer of ₹5,000 on salary day.`,
    type: 'text',
    context: 'saving',
  },
  'Where is most of my money going?': {
    content: `breakdown`,
    type: 'spending_breakdown',
    data: {
      title: 'Your May Spending Breakdown',
      items: [
        { category: 'Food & Dining', amount: 4200, pct: 23, bar: 'bg-orange-500' },
        { category: 'Shopping', amount: 3100, pct: 17, bar: 'bg-purple-500' },
        { category: 'Bills & Utilities', amount: 2400, pct: 13, bar: 'bg-yellow-500' },
        { category: 'Transport', amount: 1850, pct: 10, bar: 'bg-blue-500' },
        { category: 'Entertainment', amount: 1600, pct: 9, bar: 'bg-pink-500' },
        { category: 'Other', amount: 5270, pct: 28, bar: 'bg-gray-500' },
      ],
    },
    context: 'spending',
  },
  'Should I start a SIP?': {
    content: `action`,
    type: 'action',
    data: {
      title: '💡 Suggested Action',
      description: 'Starting a SIP of just ₹500/month in a Nifty 50 index fund could grow to **₹1.2 lakhs in 10 years** at 12% avg returns.',
      primaryAction: 'Start SIP Now',
      secondaryAction: 'Learn More',
    },
    context: 'investing',
  },
};

const DEFAULT_RESPONSE = {
  content: `That's a great question! Based on your financial profile:\n\n- Monthly income: **₹6,000** (allowance) + freelance\n- Average spending: **₹3,660/month**\n- Savings rate: **~8%**\n\nYou're doing better than 60% of students your age. However, there's room to improve your savings rate to the recommended 20%.\n\nWould you like me to create a personalized budget plan?`,
  type: 'text',
  context: 'default',
};

/* ── Typing indicator ─────────────────────────────── */
const TypingIndicator = () => (
  <motion.div
    initial={{ opacity: 0, y: 10 }}
    animate={{ opacity: 1, y: 0 }}
    exit={{ opacity: 0, y: 10 }}
    className="flex items-start gap-2.5"
  >
    <div className="h-8 w-8 rounded-full bg-gradient-to-br from-purple-500/20 to-blue-500/20 flex items-center justify-center text-sm flex-shrink-0 border border-purple-500/20">
      🧠
    </div>
    <div className="bg-bg-card border border-color-border rounded-card px-4 py-3">
      <div className="flex gap-1">
        {[0, 1, 2].map((i) => (
          <motion.div
            key={i}
            className="w-1.5 h-1.5 rounded-full bg-purple-400"
            animate={{ y: [0, -5, 0] }}
            transition={{ duration: 0.6, repeat: Infinity, delay: i * 0.15 }}
          />
        ))}
      </div>
    </div>
  </motion.div>
);

/* ── Spending Breakdown Card ─────────────────────── */
const SpendingBreakdownCard = memo(({ data }) => (
  <div className="bg-bg-primary rounded-card border border-color-border p-4 mt-2 max-w-sm">
    <h4 className="text-xs font-semibold text-text-primary mb-3">{data.title}</h4>
    <div className="space-y-2">
      {data.items.map((item) => (
        <div key={item.category} className="flex items-center gap-2">
          <span className="text-[10px] text-text-secondary w-24 truncate">{item.category}</span>
          <div className="flex-1 h-1.5 bg-bg-card rounded-full overflow-hidden">
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${item.pct}%` }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className={cn('h-full rounded-full', item.bar)}
            />
          </div>
          <span className="text-[10px] font-medium tabular-nums text-text-primary w-16 text-right">
            {formatCurrency(item.amount)}
          </span>
        </div>
      ))}
    </div>
  </div>
));
SpendingBreakdownCard.displayName = 'SpendingBreakdownCard';

/* ── Action Card ─────────────────────────────────── */
const ActionCard = memo(({ data }) => (
  <div className="bg-gradient-to-br from-accent-green/5 to-accent-blue/5 border border-accent-green/20 rounded-card p-4 mt-2 max-w-sm">
    <h4 className="text-sm font-semibold text-text-primary mb-1">{data.title}</h4>
    <ReactMarkdown
      components={{
        p: ({ children }) => <p className="text-xs text-text-secondary mb-3">{children}</p>,
        strong: ({ children }) => <strong className="text-accent-green">{children}</strong>,
      }}
    >
      {data.description}
    </ReactMarkdown>
    <div className="flex gap-2">
      <button className="px-3 py-1.5 rounded-btn bg-accent-green text-bg-primary text-xs font-medium hover:bg-accent-green/90 transition-colors">
        {data.primaryAction}
      </button>
      <button className="px-3 py-1.5 rounded-btn border border-color-border text-text-secondary text-xs font-medium hover:text-text-primary transition-colors">
        {data.secondaryAction}
      </button>
    </div>
  </div>
));
ActionCard.displayName = 'ActionCard';

/* ── Message Bubble ──────────────────────────────── */
const MessageBubble = memo(({ message }) => {
  const [copied, setCopied] = useState(false);
  const isUser = message.role === 'user';

  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(message.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  }, [message.content]);

  const timeStr = new Date(message.timestamp).toLocaleTimeString('en-IN', {
    hour: '2-digit',
    minute: '2-digit',
  });

  if (isUser) {
    return (
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex justify-end"
      >
        <div className="max-w-[75%]">
          <div className="bg-[#1E3A5F] rounded-2xl rounded-br-md px-4 py-2.5">
            <p className="text-sm text-text-primary">{message.content}</p>
          </div>
          <p className="text-[9px] text-text-secondary/50 text-right mt-0.5 pr-1">{timeStr}</p>
        </div>
      </motion.div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className="flex items-start gap-2.5"
    >
      <div className="h-8 w-8 rounded-full bg-gradient-to-br from-purple-500/20 to-blue-500/20 flex items-center justify-center text-sm flex-shrink-0 border border-purple-500/20">
        🧠
      </div>
      <div className="max-w-[75%]">
        <div className="bg-bg-card border border-purple-500/10 rounded-2xl rounded-bl-md px-4 py-3">
          {/* Text responses with markdown */}
          {message.type !== 'spending_breakdown' && message.type !== 'action' && (
            <div className="ai-markdown text-sm text-text-primary/90 space-y-2">
              <ReactMarkdown
                components={{
                  p: ({ children }) => <p className="text-sm leading-relaxed">{children}</p>,
                  strong: ({ children }) => {
                    const text = String(children);
                    if (text.startsWith('₹')) return <strong className="text-accent-green font-semibold">{children}</strong>;
                    if (text.endsWith('%')) return <strong className="text-accent-amber font-semibold">{children}</strong>;
                    return <strong className="font-semibold text-text-primary">{children}</strong>;
                  },
                  ul: ({ children }) => <ul className="space-y-1 ml-1">{children}</ul>,
                  li: ({ children }) => <li className="text-sm flex gap-1.5"><span className="text-text-secondary">•</span><span>{children}</span></li>,
                  blockquote: ({ children }) => <blockquote className="border-l-2 border-accent-amber/50 pl-3 text-xs text-accent-amber/80 italic">{children}</blockquote>,
                  h1: ({ children }) => <h3 className="font-bold text-base text-text-primary">{children}</h3>,
                  h2: ({ children }) => <h4 className="font-semibold text-sm text-text-primary">{children}</h4>,
                  ol: ({ children }) => <ol className="space-y-1 ml-1">{children}</ol>,
                }}
              >
                {message.content}
              </ReactMarkdown>
            </div>
          )}

          {/* Spending breakdown card */}
          {message.type === 'spending_breakdown' && message.data && (
            <SpendingBreakdownCard data={message.data} />
          )}

          {/* Action card */}
          {message.type === 'action' && message.data && (
            <ActionCard data={message.data} />
          )}
        </div>

        {/* Actions */}
        <div className="flex items-center gap-1.5 mt-1 pl-1">
          <p className="text-[9px] text-text-secondary/50 mr-1">{timeStr}</p>
          <button
            onClick={handleCopy}
            className="p-0.5 rounded text-text-secondary/40 hover:text-text-secondary transition-colors"
            title="Copy"
          >
            <Copy className="h-2.5 w-2.5" />
          </button>
          {copied && <span className="text-[9px] text-accent-green">Copied!</span>}
          <button className="p-0.5 rounded text-text-secondary/40 hover:text-accent-green transition-colors" title="Helpful">
            <ThumbsUp className="h-2.5 w-2.5" />
          </button>
          <button className="p-0.5 rounded text-text-secondary/40 hover:text-accent-red transition-colors" title="Not helpful">
            <ThumbsDown className="h-2.5 w-2.5" />
          </button>
        </div>
      </div>
    </motion.div>
  );
});
MessageBubble.displayName = 'MessageBubble';

/* ── Rate limit bar ──────────────────────────────── */
const RateLimitBar = memo(({ used, max }) => {
  const pct = (used / max) * 100;
  const remaining = max - used;
  const colorClass = pct >= 100 ? 'bg-accent-red' : pct >= 80 ? 'bg-accent-amber' : 'bg-accent-green';

  return (
    <div className="flex items-center gap-2 px-1">
      <div className="flex-1 h-1 bg-bg-primary rounded-full overflow-hidden">
        <motion.div
          className={cn('h-full rounded-full', colorClass)}
          initial={{ width: 0 }}
          animate={{ width: `${pct}%` }}
        />
      </div>
      <span className={cn('text-[9px] tabular-nums', pct >= 100 ? 'text-accent-red' : pct >= 80 ? 'text-accent-amber' : 'text-text-secondary')}>
        {remaining > 0 ? `${remaining} left today` : 'Limit reached'}
      </span>
    </div>
  );
});
RateLimitBar.displayName = 'RateLimitBar';

/* ── Main Chat Panel ─────────────────────────────── */
const ChatPanel = memo(() => {
  const dispatch = useDispatch();
  const conversations = useSelector((s) => s.ai.conversations);
  const activeId = useSelector((s) => s.ai.activeConversationId);
  const isTyping = useSelector((s) => s.ai.isTyping);
  const rateLimit = useSelector((s) => s.ai.rateLimit);

  const conversation = conversations.find((c) => c.id === activeId);
  const messages = conversation?.messages || [];

  const [input, setInput] = useState('');
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  const isLimitReached = rateLimit.used >= rateLimit.max;
  const charLimit = 500;

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages.length, isTyping]);

  const sendMessage = useCallback((text) => {
    const msg = text || input.trim();
    if (!msg || isLimitReached || isTyping) return;

    // Ensure conversation exists
    if (!activeId) dispatch(startNewConversation());

    dispatch(addUserMessage(msg));
    setInput('');

    // Simulate AI response
    const response = AI_RESPONSES[msg] || DEFAULT_RESPONSE;
    setTimeout(() => {
      dispatch(addAiMessage({
        content: response.content,
        type: response.type,
        data: response.data || null,
      }));
    }, 1200 + Math.random() * 800);
  }, [input, activeId, isLimitReached, isTyping, dispatch]);

  const handleKeyDown = useCallback((e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  }, [sendMessage]);

  const handleSuggestion = useCallback((text) => {
    sendMessage(text);
  }, [sendMessage]);

  // Determine context for suggestions
  const lastAiMsg = [...messages].reverse().find((m) => m.role === 'assistant');
  const suggestContext = lastAiMsg?.context || null;

  /* ── Empty state ─────────────────────────── */
  if (messages.length === 0 && !isTyping) {
    return (
      <div className="flex flex-col h-full">
        {/* Header */}
        <ChatHeader used={rateLimit.used} max={rateLimit.max} />

        {/* Empty centered content */}
        <div className="flex-1 flex flex-col items-center justify-center px-6">
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="text-center max-w-md"
          >
            <div className="relative inline-block mb-4">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-purple-500/20 to-blue-500/20 flex items-center justify-center border border-purple-500/20">
                <Brain className="h-8 w-8 text-purple-400" />
              </div>
              <motion.div
                className="absolute -top-1 -right-1 w-6 h-6 rounded-full bg-accent-amber flex items-center justify-center"
                animate={{ scale: [1, 1.2, 1] }}
                transition={{ duration: 2, repeat: Infinity }}
              >
                <Sparkles className="h-3 w-3 text-bg-primary" />
              </motion.div>
            </div>
            <h2 className="text-lg font-semibold text-text-primary mb-1">
              Ask me anything about your finances
            </h2>
            <p className="text-sm text-text-secondary mb-6">
              I know your spending, savings, and goals 🔒
            </p>

            {/* Financial context */}
            <div className="mb-5">
              <FinancialContextCard />
            </div>

            {/* Suggestions */}
            <SuggestedQuestions onSelect={handleSuggestion} disabled={isLimitReached} />
          </motion.div>
        </div>

        {/* Input */}
        <ChatInput
          input={input}
          setInput={setInput}
          onSend={sendMessage}
          onKeyDown={handleKeyDown}
          disabled={isLimitReached}
          charLimit={charLimit}
          inputRef={inputRef}
          rateLimit={rateLimit}
        />
      </div>
    );
  }

  /* ── Active chat ─────────────────────────── */
  return (
    <div className="flex flex-col h-full">
      <ChatHeader used={rateLimit.used} max={rateLimit.max} />

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4">
        {messages.map((msg) => (
          <MessageBubble key={msg.id} message={msg} />
        ))}

        <AnimatePresence>
          {isTyping && <TypingIndicator />}
        </AnimatePresence>

        <div ref={messagesEndRef} />
      </div>

      {/* Suggestions */}
      {!isTyping && messages.length > 0 && (
        <div className="px-4 pb-2">
          <SuggestedQuestions
            onSelect={handleSuggestion}
            context={suggestContext}
            disabled={isLimitReached}
          />
        </div>
      )}

      {/* Input */}
      <ChatInput
        input={input}
        setInput={setInput}
        onSend={sendMessage}
        onKeyDown={handleKeyDown}
        disabled={isLimitReached}
        charLimit={charLimit}
        inputRef={inputRef}
        rateLimit={rateLimit}
      />
    </div>
  );
});
ChatPanel.displayName = 'ChatPanel';

/* ── Chat header ─────────────────────────────────── */
const ChatHeader = memo(({ used, max }) => (
  <div className="px-4 py-3 border-b border-color-border flex-shrink-0">
    <div className="flex items-center justify-between">
      <div className="flex items-center gap-2.5">
        <div className="h-8 w-8 rounded-full bg-gradient-to-br from-purple-500/20 to-blue-500/20 flex items-center justify-center text-sm border border-purple-500/20">
          🧠
        </div>
        <div>
          <div className="flex items-center gap-1.5">
            <h3 className="text-sm font-semibold text-text-primary">WealthSense AI</h3>
            <span className="h-1.5 w-1.5 rounded-full bg-accent-green animate-pulse" />
          </div>
          <p className="text-[9px] text-text-secondary">Powered by Claude · Your personal CA</p>
        </div>
      </div>
      <div className="w-28">
        <RateLimitBar used={used} max={max} />
      </div>
    </div>
  </div>
));
ChatHeader.displayName = 'ChatHeader';

/* ── Chat input ──────────────────────────────────── */
const ChatInput = memo(({ input, setInput, onSend, onKeyDown, disabled, charLimit, inputRef, rateLimit }) => (
  <div className="px-4 py-3 border-t border-color-border flex-shrink-0">
    {disabled ? (
      <div className="text-center py-2">
        <div className="flex items-center justify-center gap-1.5 text-accent-red text-xs mb-2">
          <Lock className="h-3 w-3" />
          Daily limit reached. Resets at midnight.
        </div>
        <div className="bg-gradient-to-r from-purple-500/10 to-blue-500/10 border border-purple-500/20 rounded-card p-3">
          <p className="text-xs font-medium text-text-primary mb-0.5">Get unlimited AI advice</p>
          <p className="text-[10px] text-text-secondary mb-2">WealthSense Pro — ₹99/month</p>
          <button className="px-3 py-1.5 rounded-btn bg-purple-500 text-white text-xs font-medium hover:bg-purple-400 transition-colors">
            <Zap className="h-3 w-3 inline mr-1" />
            Upgrade to Pro
          </button>
        </div>
      </div>
    ) : (
      <div className="flex items-end gap-2">
        <div className="flex-1 relative">
          <textarea
            ref={inputRef}
            value={input}
            onChange={(e) => setInput(e.target.value.slice(0, charLimit))}
            onKeyDown={onKeyDown}
            placeholder="Ask about your finances..."
            rows={1}
            className="w-full bg-bg-card border border-color-border rounded-btn px-3 py-2.5 text-sm text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-1 focus:ring-purple-500/50 focus:border-purple-500/50 resize-none transition-all"
            style={{ minHeight: '42px', maxHeight: '120px' }}
          />
          {input.length > 0 && (
            <span className={cn(
              'absolute bottom-1.5 right-2 text-[9px] tabular-nums',
              input.length > charLimit * 0.9 ? 'text-accent-red' : 'text-text-secondary/40'
            )}>
              {input.length}/{charLimit}
            </span>
          )}
        </div>
        <button
          onClick={() => onSend()}
          disabled={!input.trim()}
          className="h-[42px] w-[42px] flex items-center justify-center rounded-btn bg-accent-green text-bg-primary hover:bg-accent-green/90 disabled:opacity-30 disabled:cursor-not-allowed transition-all flex-shrink-0"
        >
          <Send className="h-4 w-4" />
        </button>
      </div>
    )}
  </div>
));
ChatInput.displayName = 'ChatInput';

export { ChatPanel };
