import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import {
  signInApi,
  signUpApi,
  continueAsGuestApi,
  getCurrentUserApi,
  logoutApi,
  transferGuestDataApi,
} from "../services/api";

const AuthContext = createContext(null);

export const GUEST_TOKEN_KEY = "resumeai_guest_token";

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchCurrentUser = useCallback(async () => {
    try {
      const userData = await getCurrentUserApi();
      setUser(userData);
      if (userData.isGuest && userData.guestSessionToken) {
        sessionStorage.setItem(GUEST_TOKEN_KEY, userData.guestSessionToken);
      }
      return userData;
    } catch (err) {
      setUser(null);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // Check if returning from OAuth redirect
    const params = new URLSearchParams(window.location.search);
    const oauthSuccess = params.get("oauth_success");
    const oauthError = params.get("error");

    if (oauthSuccess) {
      // Clear query params from browser URL without refresh
      window.history.replaceState({}, document.title, window.location.pathname);
      fetchCurrentUser();
    } else if (oauthError) {
      setError(decodeURIComponent(oauthError));
      setLoading(false);
    } else {
      fetchCurrentUser();
    }
  }, [fetchCurrentUser]);

  const login = async (email, password, rememberMe = false) => {
    setError(null);
    try {
      const res = await signInApi({ email, password, rememberMe });
      setUser(res.user);
      return res;
    } catch (err) {
      setError(err.message);
      throw err;
    }
  };

  const signup = async (fullName, email, password) => {
    setError(null);
    try {
      const guestSessionToken = sessionStorage.getItem(GUEST_TOKEN_KEY) || undefined;
      const res = await signUpApi({ fullName, email, password, guestSessionToken });
      setUser(res.user);
      sessionStorage.removeItem(GUEST_TOKEN_KEY);
      return res;
    } catch (err) {
      setError(err.message);
      throw err;
    }
  };

  const loginAsGuest = async () => {
    setError(null);
    try {
      const res = await continueAsGuestApi();
      setUser(res.user);
      if (res.user?.guestSessionToken) {
        sessionStorage.setItem(GUEST_TOKEN_KEY, res.user.guestSessionToken);
      }
      return res;
    } catch (err) {
      setError(err.message);
      throw err;
    }
  };

  const logout = async () => {
    try {
      await logoutApi();
    } catch (err) {
      console.warn("Logout error:", err);
    } finally {
      setUser(null);
      sessionStorage.removeItem(GUEST_TOKEN_KEY);
    }
  };

  const transferGuestData = async () => {
    const guestToken = sessionStorage.getItem(GUEST_TOKEN_KEY);
    if (!guestToken) return 0;
    try {
      const res = await transferGuestDataApi(guestToken);
      sessionStorage.removeItem(GUEST_TOKEN_KEY);
      await fetchCurrentUser();
      return res.transferredCount || 0;
    } catch (err) {
      console.error("Failed to transfer guest data", err);
      return 0;
    }
  };

  const value = {
    user,
    loading,
    error,
    isAuthenticated: !!user,
    isGuest: !!user?.isGuest,
    login,
    signup,
    loginAsGuest,
    logout,
    refreshUser: fetchCurrentUser,
    transferGuestData,
    clearError: () => setError(null),
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
