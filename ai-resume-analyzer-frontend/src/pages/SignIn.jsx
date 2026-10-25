import React, { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { GOOGLE_AUTH_URL } from "../services/api";
import { Loader } from "../components/Loader";

const Logo = () => (
  <div className="flex items-center gap-2.5">
    <div
      className="w-9 h-9 rounded-xl flex items-center justify-center shadow-lg"
      style={{ background: "linear-gradient(135deg, #ff6b35, #ffbe0b)" }}
    >
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#050508" strokeWidth="2.5">
        <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
      </svg>
    </div>
    <span className="font-display font-bold text-xl" style={{ color: "#f0f0f5" }}>
      Resume<span style={{ color: "#ff6b35" }}>AI</span>
    </span>
  </div>
);

const GoogleIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24">
    <path
      fill="#4285F4"
      d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
    />
    <path
      fill="#34A853"
      d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
    />
    <path
      fill="#FBBC05"
      d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
    />
    <path
      fill="#EA4335"
      d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
    />
  </svg>
);

const SignIn = () => {
  const { login, loginAsGuest, error: contextError } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [guestLoading, setGuestLoading] = useState(false);
  const [error, setError] = useState(contextError || "");

  const redirectTarget = location.state?.from?.pathname || "/";

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!email.trim() || !password) {
      setError("Please fill in both email and password.");
      return;
    }

    try {
      setLoading(true);
      await login(email.trim(), password, rememberMe);
      navigate(redirectTarget, { replace: true });
    } catch (err) {
      setError(err.message || "Invalid credentials. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleGuestLogin = async () => {
    setError("");
    try {
      setGuestLoading(true);
      await loginAsGuest();
      navigate(redirectTarget, { replace: true });
    } catch (err) {
      setError(err.message || "Failed to initialize guest session.");
    } finally {
      setGuestLoading(false);
    }
  };

  const handleSocialLogin = (url) => {
    window.location.href = url;
  };

  return (
    <div className="min-h-screen relative flex items-center justify-center p-4" style={{ background: "var(--bg-primary)" }}>
      {/* Ambient background glows */}
      <div
        className="fixed top-0 left-1/4 w-96 h-96 rounded-full pointer-events-none"
        style={{
          background: "radial-gradient(circle, rgba(255,107,53,0.08) 0%, transparent 70%)",
          filter: "blur(50px)",
          transform: "translate(-50%, -30%)",
        }}
      />
      <div
        className="fixed bottom-0 right-1/4 w-96 h-96 rounded-full pointer-events-none"
        style={{
          background: "radial-gradient(circle, rgba(255,190,11,0.06) 0%, transparent 70%)",
          filter: "blur(50px)",
        }}
      />

      <div className="relative z-10 w-full max-w-md animate-fade-up">
        {/* Brand Header */}
        <div className="flex flex-col items-center text-center mb-8">
          <Logo />
          <h1 className="font-display text-3xl font-bold mt-4" style={{ color: "#f0f0f5" }}>
            Welcome back
          </h1>
          <p className="text-sm mt-1" style={{ color: "#6b6b85" }}>
            Sign in to access your resumes, RAG chat, and insights
          </p>
        </div>

        {/* Card */}
        <div className="glass-card gradient-border rounded-3xl p-8 shadow-2xl">
          {error && (
            <div
              className="rounded-xl px-4 py-3 text-sm mb-6 flex items-start gap-3 animate-fade-up"
              style={{
                background: "rgba(239,68,68,0.08)",
                border: "1px solid rgba(239,68,68,0.25)",
                color: "#ef4444",
              }}
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="flex-shrink-0 mt-0.5">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              <span className="flex-1 text-xs leading-relaxed">{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider mb-2" style={{ color: "#8a8aa5" }}>
                Email Address
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@company.com"
                required
                disabled={loading || guestLoading}
                className="w-full px-4 py-3 rounded-xl text-sm transition-all duration-200 outline-none"
                style={{
                  background: "rgba(255,255,255,0.04)",
                  border: "1px solid rgba(255,255,255,0.09)",
                  color: "#f0f0f5",
                }}
                onFocus={(e) => (e.target.style.borderColor = "var(--ember)")}
                onBlur={(e) => (e.target.style.borderColor = "rgba(255,255,255,0.09)")}
              />
            </div>

            {/* Password */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-xs font-semibold uppercase tracking-wider" style={{ color: "#8a8aa5" }}>
                  Password
                </label>
                <Link
                  to="/forgot-password"
                  className="text-xs transition-colors hover:underline"
                  style={{ color: "#ff6b35" }}
                >
                  Forgot password?
                </Link>
              </div>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  disabled={loading || guestLoading}
                  className="w-full px-4 py-3 rounded-xl text-sm pr-11 transition-all duration-200 outline-none"
                  style={{
                    background: "rgba(255,255,255,0.04)",
                    border: "1px solid rgba(255,255,255,0.09)",
                    color: "#f0f0f5",
                  }}
                  onFocus={(e) => (e.target.style.borderColor = "var(--ember)")}
                  onBlur={(e) => (e.target.style.borderColor = "rgba(255,255,255,0.09)")}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? "Hide password" : "Show password"}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-xs p-1 rounded transition-colors"
                  style={{ color: "#6b6b85" }}
                  tabIndex={-1}
                >
                  {showPassword ? (
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                      <line x1="1" y1="1" x2="23" y2="23" />
                    </svg>
                  ) : (
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            {/* Remember Me */}
            <div className="flex items-center gap-2 pt-1">
              <input
                type="checkbox"
                id="rememberMe"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="w-4 h-4 rounded cursor-pointer accent-[#ff6b35]"
              />
              <label htmlFor="rememberMe" className="text-xs cursor-pointer select-none" style={{ color: "#8a8aa5" }}>
                Remember me for 30 days
              </label>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading || guestLoading}
              className="w-full py-3.5 rounded-xl font-semibold text-sm transition-all duration-200 mt-2 flex items-center justify-center gap-2 disabled:opacity-50"
              style={{
                background: "linear-gradient(135deg, #ff6b35, #ffbe0b)",
                color: "#050508",
                boxShadow: "0 4px 20px rgba(255,107,53,0.25)",
              }}
            >
              {loading ? (
                <>
                  <Loader size="sm" />
                  <span>Signing in...</span>
                </>
              ) : (
                "Sign In"
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="relative my-6 text-center">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full" style={{ borderTop: "1px solid rgba(255,255,255,0.07)" }} />
            </div>
            <span className="relative px-3 text-xs uppercase tracking-widest" style={{ background: "#0c0c16", color: "#6b6b85" }}>
              or continue with
            </span>
          </div>

          {/* Social Logins */}
          <div className="space-y-2.5">
            <button
              type="button"
              onClick={() => handleSocialLogin(GOOGLE_AUTH_URL)}
              disabled={loading || guestLoading}
              className="w-full py-2.5 px-4 rounded-xl text-xs font-semibold transition-all duration-200 flex items-center justify-center gap-3 hover:scale-[1.01] active:scale-[0.99] disabled:opacity-50"
              style={{
                background: "rgba(255,255,255,0.04)",
                border: "1px solid rgba(255,255,255,0.09)",
                color: "#f0f0f5",
              }}
            >
              <GoogleIcon />
              Continue with Google
            </button>

            <button
              type="button"
              onClick={handleGuestLogin}
              disabled={loading || guestLoading}
              className="w-full py-2.5 px-4 rounded-xl text-xs font-semibold transition-all duration-200 flex items-center justify-center gap-2 hover:scale-[1.01] active:scale-[0.99] disabled:opacity-50"
              style={{
                background: "rgba(255,107,53,0.07)",
                border: "1px dashed rgba(255,107,53,0.3)",
                color: "#ff6b35",
              }}
            >
              {guestLoading ? (
                <>
                  <Loader size="sm" />
                  <span>Starting guest session...</span>
                </>
              ) : (
                <>
                  <span>⚡</span>
                  Continue as Guest
                </>
              )}
            </button>
          </div>

          {/* Footer link */}
          <p className="text-center text-xs mt-6" style={{ color: "#6b6b85" }}>
            Don't have an account?{" "}
            <Link to="/signup" className="font-semibold underline underline-offset-2" style={{ color: "#ff6b35" }}>
              Create an account
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default SignIn;
