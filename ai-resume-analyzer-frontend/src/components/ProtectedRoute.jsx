import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Loader } from "./Loader";

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div
        className="min-h-screen flex flex-col items-center justify-center"
        style={{ background: "var(--bg-primary)" }}
      >
        <div className="flex items-center gap-3 mb-6">
          <div
            className="w-10 h-10 rounded-xl flex items-center justify-center animate-pulse"
            style={{ background: "linear-gradient(135deg, #ff6b35, #ffbe0b)" }}
          >
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#050508"
              strokeWidth="2.5"
            >
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
            </svg>
          </div>
          <span className="font-display font-bold text-2xl" style={{ color: "#f0f0f5" }}>
            Resume<span style={{ color: "#ff6b35" }}>AI</span>
          </span>
        </div>
        <Loader size="lg" />
        <p className="text-xs mt-4 tracking-widest uppercase" style={{ color: "#6b6b85" }}>
          Authenticating session...
        </p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/signin" state={{ from: location }} replace />;
  }

  return children;
};

export default ProtectedRoute;
