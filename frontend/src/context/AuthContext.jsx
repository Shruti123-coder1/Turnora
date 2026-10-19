import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../api';
import { TOKEN_KEY, USER_KEY } from '../utils/constants';

const AuthContext = createContext(null);

const readUser = () => {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null');
  } catch {
    return null;
  }
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readUser);
  const [loading, setLoading] = useState(() => Boolean(localStorage.getItem(TOKEN_KEY)));

  const clear = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  const save = useCallback(({ token, user: u }) => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(u));
    setUser(u);
    return u;
  }, []);

  const refreshUser = useCallback(async () => {
    const refreshed = await authApi.me();
    localStorage.setItem(USER_KEY, JSON.stringify(refreshed));
    setUser(refreshed);
    return refreshed;
  }, []);

  // Check the saved token when the app opens
  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) {
      setLoading(false);
      return;
    }
    refreshUser()
      .catch(clear)
      .finally(() => setLoading(false));
  }, [clear, refreshUser]);

  const login = useCallback(async (email, password) => save(await authApi.login({ email, password })), [save]);
  const register = useCallback(async (form) => save(await authApi.register(form)), [save]);
  const logout = useCallback(() => {
    clear();
  }, [clear]);

  const value = useMemo(
    () => ({ user, loading, login, register, logout, refreshUser }),
    [user, loading, login, register, logout, refreshUser]
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
