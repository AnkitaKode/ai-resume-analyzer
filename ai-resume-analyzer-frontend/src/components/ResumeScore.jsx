import React, { useEffect, useRef, useState } from "react";
import { getScoreLabel } from "../utils/constants";
import { SkeletonCard } from "./Loader";

const ScoreRing = ({ score }) => {
  const [animatedScore, setAnimatedScore] = useState(0);
  const { color } = getScoreLabel(score);

  useEffect(() => {
    let start = 0;
    const end = score;
    const duration = 1400;
    const step = (timestamp) => {
      if (!start) start = timestamp;
      const progress = Math.min((timestamp - start) / duration, 1);
      const ease = 1 - Math.pow(1 - progress, 3);
      setAnimatedScore(Math.round(ease * end));
      if (progress < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  }, [score]);

  const radius = 54;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (animatedScore / 100) * circumference;

  return (
    <div className="relative flex items-center justify-center" style={{ width: 140, height: 140 }}>
      <svg width="140" height="140" viewBox="0 0 140 140" style={{ transform: "rotate(-90deg)" }}>
        <circle cx="70" cy="70" r={radius} fill="none" stroke="rgba(255,255,255,0.05)" strokeWidth="10" />
        <circle
          cx="70" cy="70" r={radius} fill="none"
          stroke={color} strokeWidth="10"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          style={{ transition: "stroke-dashoffset 0.05s linear", filter: `drop-shadow(0 0 8px ${color}60)` }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="text-3xl font-bold font-display" style={{ color: color }}>{animatedScore}</span>
        <span className="text-xs" style={{ color: "#6b6b85" }}>/ 100</span>
      </div>
    </div>
  );
};

const TagList = ({ items, color }) => (
  <div className="flex flex-wrap gap-2 mt-2">
    {items?.map((item, i) => (
      <span
        key={i}
        className="text-xs px-3 py-1 rounded-full"
        style={{
          background: `${color}12`,
          border: `1px solid ${color}30`,
          color: color,
        }}
      >
        {item}
      </span>
    ))}
  </div>
);

const Section = ({ title, items, color, icon }) => (
  <div className="glass-card rounded-xl p-4">
    <div className="flex items-center gap-2 mb-3">
      <span style={{ color }}>{icon}</span>
      <h4 className="text-sm font-semibold" style={{ color: "#f0f0f5" }}>{title}</h4>
    </div>
    <ul className="space-y-2">
      {items?.map((item, i) => (
        <li key={i} className="flex gap-2 text-sm" style={{ color: "#8a8aa5" }}>
          <span style={{ color, flexShrink: 0 }}>›</span>
          {item}
        </li>
      ))}
    </ul>
  </div>
);

const ResumeScore = ({ analysis, loading }) => {
  if (loading) return (
    <div className="space-y-4">
      <SkeletonCard />
      <SkeletonCard />
      <SkeletonCard />
    </div>
  );
  if (!analysis) return null;

  const { score, strengths, weaknesses, suggestions, atsKeywords } = analysis;
  const { label, color } = getScoreLabel(score);

  return (
    <div className="space-y-4 animate-fade-up">
      {/* Score Hero */}
      <div className="glass-card gradient-border rounded-2xl p-6 flex items-center gap-6">
        <ScoreRing score={score} />
        <div className="flex-1">
          <p className="text-xs uppercase tracking-widest mb-1" style={{ color: "#6b6b85" }}>Resume Score</p>
          <h3 className="text-2xl font-display font-bold" style={{ color }}>{label}</h3>
          <p className="text-sm mt-1" style={{ color: "#6b6b85" }}>
            Your resume scored <strong style={{ color }}>{score}/100</strong> based on AI analysis.
          </p>
          {/* Bar */}
          <div className="mt-4 h-1.5 rounded-full w-full" style={{ background: "rgba(255,255,255,0.06)" }}>
            <div
              className="h-full rounded-full score-bar-fill"
              style={{ width: `${score}%`, transition: "width 1.5s cubic-bezier(0.34, 1.56, 0.64, 1)" }}
            />
          </div>
        </div>
      </div>

      {/* Strengths */}
      <Section
        title="Strengths"
        items={strengths}
        color="#22c55e"
        icon={<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="20 6 9 17 4 12"/></svg>}
      />

      {/* Weaknesses */}
      <Section
        title="Weaknesses"
        items={weaknesses}
        color="#f97316"
        icon={<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>}
      />

      {/* Suggestions */}
      <Section
        title="Improvement Suggestions"
        items={suggestions}
        color="#ffbe0b"
        icon={<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>}
      />

      {/* ATS Keywords */}
      {atsKeywords?.length > 0 && (
        <div className="glass-card rounded-xl p-4">
          <div className="flex items-center gap-2 mb-3">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#a78bfa" strokeWidth="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <h4 className="text-sm font-semibold" style={{ color: "#f0f0f5" }}>ATS Keywords to Add</h4>
          </div>
          <TagList items={atsKeywords} color="#a78bfa" />
        </div>
      )}
    </div>
  );
};

export default ResumeScore;