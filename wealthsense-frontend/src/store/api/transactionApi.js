import { apiSlice } from './apiSlice';

export const transactionApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    getTransactions: builder.query({
      query: ({ page = 0, size = 20, type, status, category, search, dateRange, minAmount, maxAmount } = {}) => ({
        url: '/transactions',
        params: { page, size, type, status, category, search, dateRange, minAmount, maxAmount },
      }),
      providesTags: ['Transactions'],
    }),
    getTransactionById: builder.query({
      query: (id) => `/transactions/${id}`,
      providesTags: (result, error, id) => [{ type: 'Transactions', id }],
    }),
    createTransaction: builder.mutation({
      query: (transaction) => ({
        url: '/transactions',
        method: 'POST',
        body: transaction,
      }),
      invalidatesTags: ['Transactions'],
    }),
    updateTransactionApi: builder.mutation({
      query: ({ id, ...data }) => ({
        url: `/transactions/${id}`,
        method: 'PATCH',
        body: data,
      }),
      invalidatesTags: ['Transactions'],
    }),
    getTransactionStats: builder.query({
      query: ({ dateRange } = {}) => ({
        url: '/transactions/stats',
        params: { dateRange },
      }),
      providesTags: ['Transactions'],
    }),
    uploadCSV: builder.mutation({
      query: (formData) => ({
        url: '/transactions/import',
        method: 'POST',
        body: formData,
      }),
      invalidatesTags: ['Transactions'],
    }),
    exportTransactions: builder.mutation({
      query: ({ format = 'csv', dateRange, category }) => ({
        url: '/transactions/export',
        method: 'POST',
        body: { format, dateRange, category },
        responseHandler: (response) => response.blob(),
      }),
    }),
  }),
});

export const {
  useGetTransactionsQuery,
  useGetTransactionByIdQuery,
  useCreateTransactionMutation,
  useUpdateTransactionApiMutation,
  useGetTransactionStatsQuery,
  useUploadCSVMutation,
  useExportTransactionsMutation,
} = transactionApi;
