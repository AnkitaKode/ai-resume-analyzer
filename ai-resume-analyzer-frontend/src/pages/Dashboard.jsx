import React, { useState } from "react";
import UploadResume from "../components/UploadResume";
import ResumeScore from "../components/ResumeScore";
import Chatbot from "../components/Chatbot";

const Logo = () => (
  <div className="flex items-center gap-2.5">
    <div
      className="w-8 h-8 rounded-lg flex items-center justify-center"
      style={{ background: "linear-gradient(135deg, #ff6b35, #ffbe0b)" }}
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#050508" strokeWidth="2.5">
        <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
      </svg>
    </div>
    <span className="font-display font-bold text-lg" style={{ color: "#f0f0f5" }}>
      Resume<span style={{ color: "#ff6b35" }}>AI</span>
    </span>
  </div>
);

const StatBadge = ({ value, label, color }) => (
  <div className="flex items-center gap-2">
    <div className="w-1.5 h-1.5 rounded-full" style={{ background: color }} />
    <span className="text-xs" style={{ color: "#6b6b85" }}>{label}</span>
    <span className="text-xs font-semibold" style={{ color }}>{value}</span>
  </div>
);

const TabButton = ({ active, onClick, children, disabled }) => (
  <button
    onClick={onClick}
    disabled={disabled}
    className="relative px-5 py-2.5 text-sm font-medium rounded-xl transition-all duration-200 disabled:opacity-40 disabled:cursor-not-allowed"
    style={{
      color: active ? "#f0f0f5" : "#6b6b85",
      background: active ? "rgba(255,107,53,0.12)" : "transparent",
      border: active ? "1px solid rgba(255,107,53,0.25)" : "1px solid transparent",
    }}
  >
    {children}
  </button>
);

const Dashboard = () => {
  const [activeTab, setActiveTab] = useState("upload"); // upload | analysis | chat
  const [resumeData, setResumeData] = useState(null); // { resumeId, analysis }

  const handleAnalysisComplete = (data) => {
    setResumeData(data);
    setActiveTab("analysis");
  };

  const handleNewResume = () => {
    setResumeData(null);
    setActiveTab("upload");
  };

  return (
    <div className="min-h-screen relative" style={{ background: "var(--bg-primary)" }}>
      {/* Ambient background blobs */}
      <div
        className="fixed top-0 left-1/4 w-96 h-96 rounded-full pointer-events-none"
        style={{
          background: "radial-gradient(circle, rgba(255,107,53,0.06) 0%, transparent 70%)",
          filter: "blur(40px)",
          transform: "translate(-50%, -30%)",
        }}
      />
      <div
        className="fixed bottom-0 right-1/4 w-80 h-80 rounded-full pointer-events-none"
        style={{
          background: "radial-gradient(circle, rgba(255,190,11,0.04) 0%, transparent 70%)",
          filter: "blur(40px)",
        }}
      />

      <div className="relative z-10 max-w-6xl mx-auto px-4 py-8">
        {/* Header */}
        <header className="flex items-center justify-between mb-10 animate-fade-up">
          <Logo />
          <div className="flex items-center gap-4">
            <StatBadge value="GPT-4o" label="Model" color="#22c55e" />
            <StatBadge value="RAG" label="Search" color="#a78bfa" />
            {resumeData && (
              <button
                onClick={handleNewResume}
                className="text-xs px-4 py-2 rounded-xl transition-all duration-200 hover:scale-105 active:scale-95"
                style={{
                  background: "rgba(255,255,255,0.04)",
                  border: "1px solid rgba(255,255,255,0.08)",
                  color: "#6b6b85",
                }}
              >
                New Resume
              </button>
            )}
          </div>
        </header>

        {/* Hero */}
        <div className="text-center mb-10 animate-fade-up stagger-1">
          <h1 className="font-display text-5xl font-bold mb-3 leading-tight">
            <span style={{ color: "#f0f0f5" }}>Analyze Your Resume</span>
            <br />
            <span
              style={{
                background: "linear-gradient(90deg, #ff6b35, #ffbe0b)",
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
              }}
            >
              With AI Precision
            </span>
          </h1>
          <p className="text-base max-w-xl mx-auto" style={{ color: "#6b6b85" }}>
            Upload your resume, get an instant AI-powered score, and chat with your resume using advanced RAG technology.
          </p>
        </div>

        {/* Tab Navigation */}
        <div
          className="flex justify-center gap-2 mb-8 animate-fade-up stagger-2"
        >
          <div
            className="inline-flex gap-1 p-1 rounded-2xl"
            style={{ background: "rgba(255,255,255,0.03)", border: "1px solid rgba(255,255,255,0.07)" }}
          >
            <TabButton active={activeTab === "upload"} onClick={() => setActiveTab("upload")}>
              📄 Upload
            </TabButton>
            <TabButton
              active={activeTab === "analysis"}
              onClick={() => setActiveTab("analysis")}
              disabled={!resumeData}
            >
              📊 Analysis
            </TabButton>
            <TabButton
              active={activeTab === "chat"}
              onClick={() => setActiveTab("chat")}
              disabled={!resumeData}
            >
              💬 Chat
            </TabButton>
          </div>
        </div>

        {/* Main Content */}
        <div className="animate-fade-up stagger-3">
          {activeTab === "upload" && (
            <div className="max-w-2xl mx-auto">
              <div className="glass-card gradient-border rounded-3xl p-8">
                <h2 className="font-display text-2xl font-semibold mb-1" style={{ color: "#f0f0f5" }}>
                  Upload Resume
                </h2>
                <p className="text-sm mb-6" style={{ color: "#6b6b85" }}>
                  PDF or DOCX — We'll extract, analyze, and embed your resume instantly.
                </p>
                <UploadResume onAnalysisComplete={handleAnalysisComplete} />
              </div>

              {/* Feature pills */}
              <div className="flex flex-wrap gap-3 justify-center mt-6">
                {[
                  { icon: "🧠", text: "AI Score" },
                  { icon: "🔍", text: "ATS Optimization" },
                  { icon: "💬", text: "RAG Chatbot" },
                  { icon: "⚡", text: "Instant Analysis" },
                ].map((f, i) => (
                  <div
                    key={i}
                    className="flex items-center gap-2 px-4 py-2 rounded-full text-sm"
                    style={{
                      background: "rgba(255,255,255,0.03)",
                      border: "1px solid rgba(255,255,255,0.07)",
                      color: "#6b6b85",
                    }}
                  >
                    <span>{f.icon}</span> {f.text}
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === "analysis" && resumeData && (
            <div className="max-w-2xl mx-auto">
              <div className="glass-card gradient-border rounded-3xl p-8">
                <div className="flex items-center justify-between mb-6">
                  <div>
                    <h2 className="font-display text-2xl font-semibold" style={{ color: "#f0f0f5" }}>
                      AI Analysis
                    </h2>
                    <p className="text-sm mt-0.5" style={{ color: "#6b6b85" }}>
                      Detailed insights from your resume
                    </p>
                  </div>
                  <button
                    onClick={() => setActiveTab("chat")}
                    className="flex items-center gap-2 text-sm px-4 py-2 rounded-xl transition-all duration-200 hover:scale-105"
                    style={{
                      background: "rgba(255,107,53,0.10)",
                      border: "1px solid rgba(255,107,53,0.25)",
                      color: "#ff6b35",
                    }}
                  >
                    Chat with AI →
                  </button>
                </div>
                <ResumeScore analysis={resumeData.analysis} />
              </div>
            </div>
          )}

          {activeTab === "chat" && resumeData && (
            <div className="max-w-3xl mx-auto">
              <div className="glass-card gradient-border rounded-3xl overflow-hidden">
                <Chatbot resumeId={resumeData.resumeId} />
              </div>
              {/* Score chip while in chat */}
              {resumeData.analysis?.score && (
                <div className="flex justify-center mt-4">
                  <div
                    className="flex items-center gap-2 px-4 py-2 rounded-full text-sm"
                    style={{
                      background: "rgba(255,255,255,0.03)",
                      border: "1px solid rgba(255,255,255,0.07)",
                    }}
                  >
                    <span style={{ color: "#6b6b85" }}>Resume Score:</span>
                    <span
                      className="font-bold font-mono"
                      style={{ color: "#ff6b35" }}
                    >
                      {resumeData.analysis.score}/100
                    </span>
                    <button
                      onClick={() => setActiveTab("analysis")}
                      className="text-xs underline underline-offset-2 ml-1"
                      style={{ color: "#6b6b85" }}
                    >
                      View full report
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <footer className="text-center mt-14 text-xs" style={{ color: "#3d3d55" }}>
          ResumeAI · Powered by Spring Boot + OpenAI + RAG · {new Date().getFullYear()}
        </footer>
      </div>
    </div>
  );
};

export default Dashboard;