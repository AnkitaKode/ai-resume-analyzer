import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { forgotPasswordApi } from "../services/api";
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

const ForgotPassword = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!email.trim() || !/\S+@\S+\.\S+/.test(email)) {
      setError("Please enter a valid email address.");
      return;
    }

    try {
      setLoading(true);
      const res = await forgotPasswordApi(email.trim());
      setResult(res);
    } catch (err) {
      setError(err.message || "Failed to process request. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen relative flex items-center justify-center p-4" style={{ background: "var(--bg-primary)" }}>
      {/* Ambient background glows */}
      <div
        className="fixed top-0 left-1/3 w-96 h-96 rounded-full pointer-events-none"
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
            Reset your password
          </h1>
          <p className="text-sm mt-1" style={{ color: "#6b6b85" }}>
            Enter your email and we'll send you recovery instructions
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

          {result ? (
            <div className="space-y-5 animate-fade-up text-center">
              <div
                className="w-12 h-12 rounded-2xl mx-auto flex items-center justify-center text-xl"
                style={{ background: "rgba(34,197,94,0.12)", border: "1px solid rgba(34,197,94,0.25)", color: "#22c55e" }}
              >
                ✓
              </div>
              <div>
                <h3 className="font-semibold text-lg" style={{ color: "#f0f0f5" }}>
                  Instructions Generated
                </h3>
                <p className="text-xs mt-1" style={{ color: "#6b6b85" }}>
                  {result.message}
                </p>
              </div>

              {/* Development Mode Direct Access */}
              {result.devMode && result.resetToken && (
                <div
                  className="rounded-2xl p-4 text-left space-y-3"
                  style={{
                    background: "rgba(255,190,11,0.07)",
                    border: "1px solid rgba(255,190,11,0.2)",
                  }}
                >
                  <div className="flex items-center gap-2">
                    <span className="text-xs px-2 py-0.5 rounded font-mono font-bold uppercase" style={{ background: "rgba(255,190,11,0.2)", color: "#ffbe0b" }}>
                      Dev Mode
                    </span>
                    <span className="text-xs font-medium" style={{ color: "#ffbe0b" }}>
                      Local development flow
                    </span>
                  </div>
                  <p className="text-xs leading-relaxed" style={{ color: "#a0a0b5" }}>
                    SMTP email delivery is disabled for local development. Use the generated reset link below:
                  </p>
                  <button
                    onClick={() => navigate(`/reset-password?token=${result.resetToken}`)}
                    className="w-full py-2.5 rounded-xl text-xs font-semibold transition-all duration-200 hover:scale-[1.01]"
                    style={{
                      background: "linear-gradient(135deg, #ff6b35, #ffbe0b)",
                      color: "#050508",
                    }}
                  >
                    Proceed to Reset Password Now →
                  </button>
                </div>
              )}

              <div className="pt-2">
                <Link
                  to="/signin"
                  className="text-xs font-semibold underline underline-offset-2"
                  style={{ color: "#ff6b35" }}
                >
                  Return to Sign In
                </Link>
              </div>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider mb-2" style={{ color: "#8a8aa5" }}>
                  Registered Email Address
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@company.com"
                  required
                  disabled={loading}
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
                disabled={loading}
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
                    <span>Sending instructions...</span>
                  </>
                ) : (
                  "Send Recovery Link"
                )}
              </button>

              <p className="text-center text-xs mt-6" style={{ color: "#6b6b85" }}>
                Remember your password?{" "}
                <Link to="/signin" className="font-semibold underline underline-offset-2" style={{ color: "#ff6b35" }}>
                  Sign in
                </Link>
              </p>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
