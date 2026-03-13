import React from "react";

export const Loader = ({ size = "md", text = "" }) => {
  const sizes = { sm: "w-4 h-4", md: "w-8 h-8", lg: "w-12 h-12" };
  return (
    <div className="flex flex-col items-center gap-3">
      <div className={`${sizes[size]} relative`}>
        <div className="absolute inset-0 rounded-full border-2 border-white/10" />
        <div className="absolute inset-0 rounded-full border-2 border-transparent border-t-[#ff6b35] animate-spin" />
        <div
          className="absolute inset-1 rounded-full border-2 border-transparent border-t-[#ffbe0b] animate-spin"
          style={{ animationDirection: "reverse", animationDuration: "0.8s" }}
        />
      </div>
      {text && (
        <p className="text-sm font-body" style={{ color: "#6b6b85" }}>
          {text}
        </p>
      )}
    </div>
  );
};

export const TypingIndicator = () => (
  <div className="flex items-center gap-1 px-4 py-3">
    <div className="typing-dot" />
    <div className="typing-dot" />
    <div className="typing-dot" />
  </div>
);

export const SkeletonCard = () => (
  <div className="glass-card rounded-2xl p-6 space-y-3 animate-pulse">
    <div className="h-4 rounded-full w-1/3" style={{ background: "rgba(255,255,255,0.06)" }} />
    <div className="h-3 rounded-full w-full" style={{ background: "rgba(255,255,255,0.04)" }} />
    <div className="h-3 rounded-full w-4/5" style={{ background: "rgba(255,255,255,0.04)" }} />
    <div className="h-3 rounded-full w-2/3" style={{ background: "rgba(255,255,255,0.04)" }} />
  </div>
);

export default Loader;