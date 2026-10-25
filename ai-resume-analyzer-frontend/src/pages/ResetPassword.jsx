import React, { useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { resetPasswordApi } from "../services/api";
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

const ResetPassword = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const [token, setToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const tokenParam = params.get("token");
    if (!tokenParam) {
      setError("Reset token is missing from the URL. Please request a new password reset link.");
    } else {
      setToken(tokenParam);
    }
  }, [location.search]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!token) {
      setError("Invalid or missing reset token.");
      return;
    }
    if (newPassword.length < 8) {
      setError("Password must be at least 8 characters long.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    try {
      setLoading(true);
      await resetPasswordApi(token, newPassword);
      setSuccess(true);
      setTimeout(() => {
        navigate("/signin", { replace: true });
      }, 2500);
    } catch (err) {
      setError(err.message || "Failed to reset password. The link may have expired.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen relative flex items-center justify-center p-4" style={{ background: "var(--bg-primary)" }}>
      {/* Ambient background glows */}
      <div
        className="fixed top-0 right-1/3 w-96 h-96 rounded-full pointer-events-none"
        style={{
          background: "radial-gradient(circle, rgba(255,107,53,0.08) 0%, transparent 70%)",
          filter: "blur(50px)",
        }}
      />

      <div className="relative z-10 w-full max-w-md animate-fade-up">
        {/* Brand Header */}
        <div className="flex flex-col items-center text-center mb-8">
          <Logo />
          <h1 className="font-display text-3xl font-bold mt-4" style={{ color: "#f0f0f5" }}>
            Set new password
          </h1>
          <p className="text-sm mt-1" style={{ color: "#6b6b85" }}>
            Please choose a secure password with at least 8 characters
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

          {success ? (
            <div className="space-y-4 animate-fade-up text-center">
              <div
                className="w-12 h-12 rounded-2xl mx-auto flex items-center justify-center text-xl"
                style={{ background: "rgba(34,197,94,0.12)", border: "1px solid rgba(34,197,94,0.25)", color: "#22c55e" }}
              >
                ✓
              </div>
              <h3 className="font-semibold text-lg" style={{ color: "#f0f0f5" }}>
                Password Reset Successfully!
              </h3>
              <p className="text-xs" style={{ color: "#6b6b85" }}>
                Redirecting you to the sign in page in a moment...
              </p>
              <div className="pt-2">
                <Link
                  to="/signin"
                  className="inline-block py-2.5 px-6 rounded-xl text-xs font-semibold"
                  style={{
                    background: "linear-gradient(135deg, #ff6b35, #ffbe0b)",
                    color: "#050508",
                  }}
                >
                  Sign In Now →
                </Link>
              </div>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider mb-2" style={{ color: "#8a8aa5" }}>
                  New Password
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? "text" : "password"}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="At least 8 characters"
                    required
                    disabled={loading || !token}
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

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider mb-2" style={{ color: "#8a8aa5" }}>
                  Confirm New Password
                </label>
                <input
                  type={showPassword ? "text" : "password"}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Re-enter new password"
                  required
                  disabled={loading || !token}
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

              <button
                type="submit"
                disabled={loading || !token}
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
                    <span>Updating password...</span>
                  </>
                ) : (
                  "Update Password"
                )}
              </button>

              <p className="text-center text-xs mt-6" style={{ color: "#6b6b85" }}>
                <Link to="/signin" className="underline underline-offset-2" style={{ color: "#ff6b35" }}>
                  Back to Sign In
                </Link>
              </p>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResetPassword;
