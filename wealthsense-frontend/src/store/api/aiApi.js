import { apiSlice } from './apiSlice';

export const aiApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    sendMessage: builder.mutation({
      query: ({ conversationId, message }) => ({
        url: '/ai/chat',
        method: 'POST',
        body: { conversationId, message },
      }),
    }),
    getConversations: builder.query({
      query: () => '/ai/conversations',
      providesTags: ['AiConversations'],
    }),
    getConversationById: builder.query({
      query: (id) => `/ai/conversations/${id}`,
      providesTags: (result, error, id) => [{ type: 'AiConversations', id }],
    }),
    deleteConversationApi: builder.mutation({
      query: (id) => ({
        url: `/ai/conversations/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['AiConversations'],
    }),
    getDailyInsight: builder.query({
      query: () => '/ai/insight',
    }),
    getRateLimit: builder.query({
      query: () => '/ai/rate-limit',
    }),
  }),
});

export const {
  useSendMessageMutation,
  useGetConversationsQuery,
  useGetConversationByIdQuery,
  useDeleteConversationApiMutation,
  useGetDailyInsightQuery,
  useGetRateLimitQuery,
} = aiApi;
