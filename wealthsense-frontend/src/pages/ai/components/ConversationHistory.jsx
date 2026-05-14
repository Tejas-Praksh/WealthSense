import { memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useSelector, useDispatch } from 'react-redux';
import { Plus, Trash2, MessageCircle } from 'lucide-react';
import { startNewConversation, setActiveConversation, deleteConversation } from '@/store/slices/aiSlice';
import { cn } from '@/lib/utils';

const ConversationHistory = memo(({ onConversationSelect }) => {
  const dispatch = useDispatch();
  const conversations = useSelector((s) => s.ai.conversations);
  const activeId = useSelector((s) => s.ai.activeConversationId);

  const handleNew = () => {
    dispatch(startNewConversation());
  };

  const handleSelect = (id) => {
    dispatch(setActiveConversation(id));
    onConversationSelect?.();
  };

  const handleDelete = (e, id) => {
    e.stopPropagation();
    dispatch(deleteConversation(id));
  };

  const formatDate = (ts) => {
    const d = new Date(ts);
    const now = new Date();
    const diff = now.getTime() - d.getTime();
    if (diff < 86400000) return 'Today';
    if (diff < 172800000) return 'Yesterday';
    return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
  };

  return (
    <div className="flex flex-col h-full">
      {/* New chat button */}
      <div className="p-3">
        <button
          onClick={handleNew}
          className="w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-btn bg-accent-green/10 text-accent-green text-sm font-medium hover:bg-accent-green/20 transition-colors border border-accent-green/20"
        >
          <Plus className="h-4 w-4" />
          New Chat
        </button>
      </div>

      {/* Conversation list */}
      <div className="flex-1 overflow-y-auto px-2 space-y-0.5">
        <AnimatePresence>
          {conversations.length === 0 ? (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-center py-12 px-4"
            >
              <MessageCircle className="h-8 w-8 text-text-secondary/30 mx-auto mb-2" />
              <p className="text-xs text-text-secondary">No conversations yet</p>
              <p className="text-[10px] text-text-secondary/60 mt-0.5">Start your first conversation</p>
            </motion.div>
          ) : (
            conversations.map((conv) => (
              <motion.button
                key={conv.id}
                initial={{ opacity: 0, x: -10 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -10 }}
                onClick={() => handleSelect(conv.id)}
                className={cn(
                  'group w-full text-left px-3 py-2.5 rounded-btn transition-all flex items-start gap-2.5',
                  activeId === conv.id
                    ? 'bg-accent-green/10 border border-accent-green/20'
                    : 'hover:bg-bg-primary border border-transparent'
                )}
              >
                <MessageCircle className={cn('h-3.5 w-3.5 mt-0.5 flex-shrink-0', activeId === conv.id ? 'text-accent-green' : 'text-text-secondary/50')} />
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-medium text-text-primary truncate">
                    {conv.title}
                  </p>
                  <div className="flex items-center gap-1.5 mt-0.5">
                    <span className="text-[9px] text-text-secondary">{formatDate(conv.updatedAt)}</span>
                    <span className="text-[9px] text-text-secondary/40">·</span>
                    <span className="text-[9px] text-text-secondary">{conv.messages.length} msgs</span>
                  </div>
                </div>
                <button
                  onClick={(e) => handleDelete(e, conv.id)}
                  className="p-1 rounded-md text-text-secondary/0 group-hover:text-text-secondary/50 hover:!text-accent-red hover:bg-accent-red/10 transition-all flex-shrink-0"
                >
                  <Trash2 className="h-3 w-3" />
                </button>
              </motion.button>
            ))
          )}
        </AnimatePresence>
      </div>

      {/* Footer */}
      <div className="p-3 border-t border-color-border">
        <p className="text-[9px] text-text-secondary/50 text-center">
          Conversations stored locally 🔒
        </p>
      </div>
    </div>
  );
});

ConversationHistory.displayName = 'ConversationHistory';
export { ConversationHistory };
