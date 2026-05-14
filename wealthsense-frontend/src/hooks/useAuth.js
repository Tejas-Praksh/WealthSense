import { useCallback } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { setCredentials, logout as logoutAction, setLoading, setError } from '@/store/slices/authSlice';
import { useLoginMutation, useRegisterMutation } from '@/store/api/authApi';
import { ROUTES, STORAGE_KEYS } from '@/utils/constants';

export function useAuth() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user, isAuthenticated, isLoading, error } = useSelector((state) => state.auth);

  const [loginMutation] = useLoginMutation();
  const [registerMutation] = useRegisterMutation();

  const login = useCallback(
    async (credentials) => {
      dispatch(setLoading(true));
      dispatch(setError(null));
      try {
        const response = await loginMutation(credentials).unwrap();
        dispatch(
          setCredentials({
            user: response.user,
            accessToken: response.accessToken,
          })
        );
        if (response.refreshToken) {
          localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
        }
        navigate(ROUTES.DASHBOARD);
        return response;
      } catch (err) {
        const message = err?.data?.message || 'Login failed. Please try again.';
        dispatch(setError(message));
        throw err;
      } finally {
        dispatch(setLoading(false));
      }
    },
    [dispatch, loginMutation, navigate]
  );

  const register = useCallback(
    async (userData) => {
      dispatch(setLoading(true));
      dispatch(setError(null));
      try {
        const response = await registerMutation(userData).unwrap();
        dispatch(
          setCredentials({
            user: response.user,
            accessToken: response.accessToken,
          })
        );
        if (response.refreshToken) {
          localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
        }
        navigate(ROUTES.DASHBOARD);
        return response;
      } catch (err) {
        const message = err?.data?.message || 'Registration failed. Please try again.';
        dispatch(setError(message));
        throw err;
      } finally {
        dispatch(setLoading(false));
      }
    },
    [dispatch, registerMutation, navigate]
  );

  const logout = useCallback(() => {
    dispatch(logoutAction());
    navigate(ROUTES.LOGIN);
  }, [dispatch, navigate]);

  return {
    user,
    isAuthenticated,
    isLoading,
    error,
    login,
    register,
    logout,
  };
}
