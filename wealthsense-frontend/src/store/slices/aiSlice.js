import { createSlice } from '@reduxjs/toolkit';

const STORAGE_KEY = 'ws_ai_conversations';

function loadConversations() {
  try {
    const data = localStorage.getItem(STORAGE_KEY);
    return data ? JSON.parse(data) : [];
  } catch { return []; }
}

function saveConversations(conversations) {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(conversations)); } catch {}
}

function createConversation() {
  return {
    id: `conv-${Date.now()}`,
    title: 'New conversation',
    messages: [],
    createdAt: Date.now(),
    updatedAt: Date.now(),
  };
}

const storedConversations = loadConversations();

const initialState = {
  conversations: storedConversations,
  activeConversationId: null,
  isTyping: false,
  rateLimit: { used: 0, max: 10 },
  error: null,
  dailyInsight: null,
};

const aiSlice = createSlice({
  name: 'ai',
  initialState,
  reducers: {
    startNewConversation: (state) => {
      const conv = createConversation();
      state.conversations.unshift(conv);
      state.activeConversationId = conv.id;
      saveConversations(state.conversations);
    },
    setActiveConversation: (state, action) => {
      state.activeConversationId = action.payload;
    },
    addUserMessage: (state, action) => {
      const conv = state.conversations.find((c) => c.id === state.activeConversationId);
      if (!conv) return;
      const msg = {
        id: `msg-${Date.now()}`,
        role: 'user',
        content: action.payload,
        timestamp: Date.now(),
      };
      conv.messages.push(msg);
      conv.title = action.payload.slice(0, 60);
      conv.updatedAt = Date.now();
      state.isTyping = true;
      state.rateLimit.used = Math.min(state.rateLimit.used + 1, state.rateLimit.max);
      saveConversations(state.conversations);
    },
    addAiMessage: (state, action) => {
      const conv = state.conversations.find((c) => c.id === state.activeConversationId);
      if (!conv) return;
      const msg = {
        id: `msg-${Date.now()}`,
        role: 'assistant',
        content: action.payload.content,
        type: action.payload.type || 'text',
        data: action.payload.data || null,
        timestamp: Date.now(),
      };
      conv.messages.push(msg);
      conv.updatedAt = Date.now();
      state.isTyping = false;
      saveConversations(state.conversations);
    },
    setTyping: (state, action) => {
      state.isTyping = action.payload;
    },
    deleteConversation: (state, action) => {
      state.conversations = state.conversations.filter((c) => c.id !== action.payload);
      if (state.activeConversationId === action.payload) {
        state.activeConversationId = state.conversations[0]?.id || null;
      }
      saveConversations(state.conversations);
    },
    setDailyInsight: (state, action) => {
      state.dailyInsight = action.payload;
    },
    setError: (state, action) => {
      state.error = action.payload;
      state.isTyping = false;
    },
  },
});

export const {
  startNewConversation,
  setActiveConversation,
  addUserMessage,
  addAiMessage,
  setTyping,
  deleteConversation,
  setDailyInsight,
  setError,
} = aiSlice.actions;
export default aiSlice.reducer;
