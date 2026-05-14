import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useSelector, useDispatch } from 'react-redux';
import { PanelLeftClose, PanelLeft, Menu, X } from 'lucide-react';
import { PageLayout } from '@/components/layout/PageLayout';
import { ChatPanel } from './components/ChatPanel';
import { ConversationHistory } from './components/ConversationHistory';
import { AiInsightBanner } from './components/AiInsightBanner';
import { startNewConversation, setActiveConversation } from '@/store/slices/aiSlice';
import { cn } from '@/lib/utils';

const AiAdvisor = () => {
  const dispatch = useDispatch();
  const conversations = useSelector((s) => s.ai.conversations);
  const activeId = useSelector((s) => s.ai.activeConversationId);

  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [mobileDrawer, setMobileDrawer] = useState(false);

  // Auto-start a new conversation if none
  useEffect(() => {
    if (!activeId && conversations.length === 0) {
      dispatch(startNewConversation());
    } else if (!activeId && conversations.length > 0) {
      dispatch(setActiveConversation(conversations[0].id));
    }
  }, [activeId, conversations, dispatch]);

  const handleConversationSelect = useCallback(() => {
    setMobileDrawer(false);
  }, []);

  return (
    <PageLayout noPadding>
      <div className="flex flex-col h-[calc(100vh-64px)]">
        {/* Insight banner */}
        <div className="px-4 pt-3">
          <AiInsightBanner />
        </div>

        {/* Main layout */}
        <div className="flex flex-1 min-h-0 mt-2">
          {/* Desktop sidebar */}
          <AnimatePresence mode="wait">
            {sidebarOpen && (
              <motion.div
                initial={{ width: 0, opacity: 0 }}
                animate={{ width: 280, opacity: 1 }}
                exit={{ width: 0, opacity: 0 }}
                transition={{ duration: 0.2 }}
                className="hidden md:flex flex-col border-r border-color-border bg-bg-card overflow-hidden flex-shrink-0"
              >
                <ConversationHistory onConversationSelect={handleConversationSelect} />
              </motion.div>
            )}
          </AnimatePresence>

          {/* Chat panel */}
          <div className="flex-1 flex flex-col min-w-0">
            {/* Toolbar */}
            <div className="flex items-center gap-2 px-4 pt-2">
              {/* Desktop toggle */}
              <button
                onClick={() => setSidebarOpen(!sidebarOpen)}
                className="hidden md:flex items-center justify-center p-1.5 rounded-md text-text-secondary hover:text-text-primary hover:bg-bg-primary transition-colors"
              >
                {sidebarOpen ? <PanelLeftClose className="h-4 w-4" /> : <PanelLeft className="h-4 w-4" />}
              </button>

              {/* Mobile menu */}
              <button
                onClick={() => setMobileDrawer(true)}
                className="md:hidden flex items-center justify-center p-1.5 rounded-md text-text-secondary hover:text-text-primary hover:bg-bg-primary transition-colors"
              >
                <Menu className="h-4 w-4" />
              </button>
            </div>

            <div className="flex-1 min-h-0">
              <ChatPanel />
            </div>
          </div>
        </div>

        {/* Mobile conversation drawer */}
        <AnimatePresence>
          {mobileDrawer && (
            <div className="fixed inset-0 z-50 md:hidden">
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="fixed inset-0 bg-black/50 backdrop-blur-sm"
                onClick={() => setMobileDrawer(false)}
              />
              <motion.div
                initial={{ x: '-100%' }}
                animate={{ x: 0 }}
                exit={{ x: '-100%' }}
                transition={{ type: 'spring', damping: 25, stiffness: 300 }}
                className="fixed left-0 top-0 bottom-0 w-72 bg-bg-card border-r border-color-border z-10"
              >
                <div className="flex items-center justify-between px-3 py-3 border-b border-color-border">
                  <p className="text-sm font-medium text-text-primary">Conversations</p>
                  <button
                    onClick={() => setMobileDrawer(false)}
                    className="p-1.5 rounded-md text-text-secondary hover:text-text-primary"
                  >
                    <X className="h-4 w-4" />
                  </button>
                </div>
                <ConversationHistory onConversationSelect={handleConversationSelect} />
              </motion.div>
            </div>
          )}
        </AnimatePresence>
      </div>
    </PageLayout>
  );
};

export default AiAdvisor;
