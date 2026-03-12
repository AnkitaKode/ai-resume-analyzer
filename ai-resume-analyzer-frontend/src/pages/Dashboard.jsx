import React, { useState } from "react";
import UploadResume from "../components/UploadResume";
import ResumeScore from "../components/ResumeScore";
import Chatbot from "../components/Chatbot";

const Logo = () => (
  <div className="flex items-center gap-2.5">
    <div className="w-8 h-8 rounded-lg flex items-center justify-center"
      style={{ background: "linear-gradient(135deg, #ff6b35, #ffbe0b)" }}>
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

// ── Stat Cards Row ─────────────────────────────────────────────────────────────
const StatsRow = () => (
  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10">
    {[
      { value: "98%",    label: "Accuracy Rate",      icon: "🎯", color: "#22c55e" },
      { value: "3s",     label: "Avg Analysis Time",  icon: "⚡", color: "#ffbe0b" },
      { value: "50+",    label: "ATS Keywords Found",  icon: "🔍", color: "#a78bfa" },
      { value: "RAG",    label: "AI Search Engine",    icon: "🧠", color: "#ff6b35" },
    ].map((s, i) => (
      <div key={i} className="glass-card rounded-2xl p-4 flex items-center gap-3"
        style={{ border: "1px solid rgba(255,255,255,0.07)" }}>
        <div className="text-2xl">{s.icon}</div>
        <div>
          <p className="text-xl font-bold font-display" style={{ color: s.color }}>{s.value}</p>
          <p className="text-xs" style={{ color: "#6b6b85" }}>{s.label}</p>
        </div>
      </div>
    ))}
  </div>
);

// ── How It Works ───────────────────────────────────────────────────────────────
const HowItWorks = () => (
  <div className="mb-10">
    <p className="text-xs uppercase tracking-widest mb-4 text-center" style={{ color: "#6b6b85" }}>
      How It Works
    </p>
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {[
        {
          step: "01",
          title: "Upload Resume",
          desc: "Drag and drop your PDF or DOCX resume. Our parser extracts every section instantly using Apache Tika.",
          icon: "📄",
          color: "#ff6b35",
        },
        {
          step: "02",
          title: "AI Analysis",
          desc: "GPT-4 scores your resume out of 100, identifies strengths, weaknesses, and missing ATS keywords.",
          icon: "🧠",
          color: "#ffbe0b",
        },
        {
          step: "03",
          title: "Chat with AI",
          desc: "Ask anything about your resume. Our RAG pipeline retrieves relevant context and answers precisely.",
          icon: "💬",
          color: "#a78bfa",
        },
      ].map((item, i) => (
        <div key={i} className="glass-card rounded-2xl p-5 relative overflow-hidden"
          style={{ border: "1px solid rgba(255,255,255,0.07)" }}>
          <div className="absolute top-4 right-4 font-display font-bold text-4xl"
            style={{ color: "rgba(255,255,255,0.03)" }}>
            {item.step}
          </div>
          <div className="w-10 h-10 rounded-xl flex items-center justify-center text-xl mb-3"
            style={{ background: `${item.color}15`, border: `1px solid ${item.color}25` }}>
            {item.icon}
          </div>
          <p className="text-xs font-semibold uppercase tracking-widest mb-1"
            style={{ color: item.color }}>{item.step}</p>
          <h3 className="font-semibold mb-2" style={{ color: "#f0f0f5" }}>{item.title}</h3>
          <p className="text-sm leading-relaxed" style={{ color: "#6b6b85" }}>{item.desc}</p>
        </div>
      ))}
    </div>
  </div>
);

// ── Feature Cards ──────────────────────────────────────────────────────────────
const FeatureCards = () => (
  <div className="mb-10">
    <p className="text-xs uppercase tracking-widest mb-4 text-center" style={{ color: "#6b6b85" }}>
      Features
    </p>
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {[
        {
          title: "Resume Score & Grading",
          desc: "Get an instant 0–100 score based on formatting, keyword density, action verbs, quantified achievements, and ATS compatibility.",
          icon: "📊",
          color: "#22c55e",
          tags: ["Scoring", "ATS", "Formatting"],
        },
        {
          title: "Strengths & Weaknesses",
          desc: "AI pinpoints exactly what's working in your resume and what's holding you back with specific, actionable feedback per section.",
          icon: "⚖️",
          color: "#ffbe0b",
          tags: ["Analysis", "Feedback", "Sections"],
        },
        {
          title: "ATS Keyword Optimization",
          desc: "Discover missing keywords recruiters and applicant tracking systems are scanning for — tailored to your industry and role.",
          icon: "🔍",
          color: "#a78bfa",
          tags: ["Keywords", "Recruiters", "Optimization"],
        },
        {
          title: "RAG-Powered Chatbot",
          desc: "Chat directly with your resume using Retrieval-Augmented Generation. Ask about your experience, skills, or how to improve specific sections.",
          icon: "🤖",
          color: "#ff6b35",
          tags: ["RAG", "Vector Search", "GPT-4"],
        },
      ].map((f, i) => (
        <div key={i} className="glass-card rounded-2xl p-5"
          style={{ border: "1px solid rgba(255,255,255,0.07)" }}>
          <div className="flex items-start gap-4">
            <div className="w-10 h-10 rounded-xl flex items-center justify-center text-xl flex-shrink-0"
              style={{ background: `${f.color}15`, border: `1px solid ${f.color}25` }}>
              {f.icon}
            </div>
            <div className="flex-1">
              <h3 className="font-semibold mb-1" style={{ color: "#f0f0f5" }}>{f.title}</h3>
              <p className="text-sm leading-relaxed mb-3" style={{ color: "#6b6b85" }}>{f.desc}</p>
              <div className="flex flex-wrap gap-2">
                {f.tags.map((tag, j) => (
                  <span key={j} className="text-xs px-2 py-0.5 rounded-full"
                    style={{ background: `${f.color}10`, border: `1px solid ${f.color}20`, color: f.color }}>
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  </div>
);

// ── Tech Stack ─────────────────────────────────────────────────────────────────
const TechStack = () => (
  <div className="mb-10">
    <p className="text-xs uppercase tracking-widest mb-4 text-center" style={{ color: "#6b6b85" }}>
      Tech Stack
    </p>
    <div className="glass-card rounded-2xl p-5" style={{ border: "1px solid rgba(255,255,255,0.07)" }}>
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { layer: "Frontend",  techs: ["React 18", "TailwindCSS", "Axios"],              color: "#38bdf8" },
          { layer: "Backend",   techs: ["Spring Boot 3", "Spring AI", "REST API"],        color: "#86efac" },
          { layer: "AI / ML",   techs: ["GPT-4 / Llama3", "RAG Pipeline", "Embeddings"], color: "#ff6b35" },
          { layer: "Database",  techs: ["PostgreSQL", "JPA / Hibernate", "Vector Store"], color: "#a78bfa" },
        ].map((t, i) => (
          <div key={i}>
            <p className="text-xs font-semibold uppercase tracking-widest mb-2"
              style={{ color: t.color }}>{t.layer}</p>
            <div className="space-y-1">
              {t.techs.map((tech, j) => (
                <div key={j} className="flex items-center gap-2">
                  <div className="w-1 h-1 rounded-full" style={{ background: t.color }} />
                  <p className="text-xs" style={{ color: "#8a8aa5" }}>{tech}</p>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  </div>
);

// ── Tips Card ──────────────────────────────────────────────────────────────────
const TipsCard = () => (
  <div className="mb-10">
    <p className="text-xs uppercase tracking-widest mb-4 text-center" style={{ color: "#6b6b85" }}>
      Pro Tips
    </p>
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {[
        { tip: "Use quantified achievements like 'Increased sales by 40%' to boost your score significantly.", icon: "📈" },
        { tip: "Include role-specific keywords from job descriptions — ATS systems scan for exact matches.", icon: "🎯" },
        { tip: "Ask the chatbot 'What roles am I best suited for?' to discover new career opportunities.", icon: "💡" },
      ].map((t, i) => (
        <div key={i} className="glass-card rounded-2xl p-4 flex gap-3"
          style={{ border: "1px solid rgba(255,190,11,0.1)" }}>
          <span className="text-xl flex-shrink-0">{t.tip.startsWith("Use") ? "📈" : t.tip.startsWith("Include") ? "🎯" : "💡"}</span>
          <p className="text-sm leading-relaxed" style={{ color: "#8a8aa5" }}>{t.tip}</p>
        </div>
      ))}
    </div>
  </div>
);

// ── Main Dashboard ─────────────────────────────────────────────────────────────
const Dashboard = () => {
  const [activeTab, setActiveTab] = useState("upload");
  const [resumeData, setResumeData] = useState(null);

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
      {/* Ambient blobs */}
      <div className="fixed top-0 left-1/4 w-96 h-96 rounded-full pointer-events-none"
        style={{ background: "radial-gradient(circle, rgba(255,107,53,0.06) 0%, transparent 70%)", filter: "blur(40px)", transform: "translate(-50%, -30%)" }} />
      <div className="fixed bottom-0 right-1/4 w-80 h-80 rounded-full pointer-events-none"
        style={{ background: "radial-gradient(circle, rgba(255,190,11,0.04) 0%, transparent 70%)", filter: "blur(40px)" }} />
      <div className="fixed top-1/2 right-0 w-64 h-64 rounded-full pointer-events-none"
        style={{ background: "radial-gradient(circle, rgba(167,139,250,0.04) 0%, transparent 70%)", filter: "blur(40px)" }} />

      <div className="relative z-10 max-w-5xl mx-auto px-4 py-8">

        {/* Header */}
        <header className="flex items-center justify-between mb-10 animate-fade-up">
          <Logo />
          <div className="flex items-center gap-4">
            <StatBadge value="Llama3" label="Model" color="#22c55e" />
            <StatBadge value="RAG" label="Search" color="#a78bfa" />
            <StatBadge value="v1.0" label="Version" color="#ffbe0b" />
            {resumeData && (
              <button onClick={handleNewResume}
                className="text-xs px-4 py-2 rounded-xl transition-all duration-200 hover:scale-105 active:scale-95"
                style={{ background: "rgba(255,255,255,0.04)", border: "1px solid rgba(255,255,255,0.08)", color: "#6b6b85" }}>
                ↑ New Resume
              </button>
            )}
          </div>
        </header>

        {/* Hero */}
        <div className="text-center mb-10 animate-fade-up stagger-1">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full mb-4 text-xs"
            style={{ background: "rgba(255,107,53,0.08)", border: "1px solid rgba(255,107,53,0.2)", color: "#ff6b35" }}>
            <div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse" />
            AI-Powered Resume Intelligence
          </div>
          <h1 className="font-display text-5xl font-bold mb-3 leading-tight">
            <span style={{ color: "#f0f0f5" }}>Analyze Your Resume</span>
            <br />
            <span style={{ background: "linear-gradient(90deg, #ff6b35, #ffbe0b)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent" }}>
              With AI Precision
            </span>
          </h1>
          <p className="text-base max-w-xl mx-auto mb-6" style={{ color: "#6b6b85" }}>
            Upload your resume, get an instant AI-powered score, identify ATS gaps, and chat with your resume using advanced Retrieval-Augmented Generation.
          </p>
          {/* CTA hint */}
          {!resumeData && (
            <div className="flex items-center justify-center gap-2 text-sm" style={{ color: "#6b6b85" }}>
              <span>↓</span>
              <span>Upload your resume to get started — takes under 30 seconds</span>
            </div>
          )}
        </div>

        {/* Stats Row — only on upload tab */}
        {activeTab === "upload" && <StatsRow />}

        {/* Tab Navigation */}
        <div className="flex justify-center gap-2 mb-8 animate-fade-up stagger-2">
          <div className="inline-flex gap-1 p-1 rounded-2xl"
            style={{ background: "rgba(255,255,255,0.03)", border: "1px solid rgba(255,255,255,0.07)" }}>
            <TabButton active={activeTab === "upload"} onClick={() => setActiveTab("upload")}>
              📄 Upload
            </TabButton>
            <TabButton active={activeTab === "analysis"} onClick={() => setActiveTab("analysis")} disabled={!resumeData}>
              📊 Analysis
            </TabButton>
            <TabButton active={activeTab === "chat"} onClick={() => setActiveTab("chat")} disabled={!resumeData}>
              💬 Chat
            </TabButton>
          </div>
        </div>

        {/* Main Content */}
        <div className="animate-fade-up stagger-3">

          {/* ── UPLOAD TAB ── */}
          {activeTab === "upload" && (
            <div className="space-y-8">
              {/* Upload Card */}
              <div className="max-w-2xl mx-auto">
                <div className="glass-card gradient-border rounded-3xl p-8">
                  <div className="flex items-center gap-3 mb-1">
                    <div className="w-8 h-8 rounded-lg flex items-center justify-center text-sm"
                      style={{ background: "rgba(255,107,53,0.12)", border: "1px solid rgba(255,107,53,0.2)" }}>
                      📄
                    </div>
                    <h2 className="font-display text-2xl font-semibold" style={{ color: "#f0f0f5" }}>
                      Upload Resume
                    </h2>
                  </div>
                  <p className="text-sm mb-6 ml-11" style={{ color: "#6b6b85" }}>
                    PDF or DOCX · Max 10MB · Analyzed in seconds
                  </p>
                  <UploadResume onAnalysisComplete={handleAnalysisComplete} />
                </div>
              </div>

              {/* How It Works */}
              <HowItWorks />

              {/* Feature Cards */}
              <FeatureCards />

              {/* Tech Stack */}
              <TechStack />

              {/* Tips */}
              <TipsCard />
            </div>
          )}

          {/* ── ANALYSIS TAB ── */}
          {activeTab === "analysis" && resumeData && (
            <div className="max-w-2xl mx-auto space-y-4">
              {/* Analysis header card */}
              <div className="glass-card gradient-border rounded-3xl p-8">
                <div className="flex items-center justify-between mb-6">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
                      <p className="text-xs uppercase tracking-widest" style={{ color: "#6b6b85" }}>
                        Analysis Complete
                      </p>
                    </div>
                    <h2 className="font-display text-2xl font-semibold" style={{ color: "#f0f0f5" }}>
                      AI Analysis Report
                    </h2>
                    <p className="text-sm mt-0.5" style={{ color: "#6b6b85" }}>
                      Detailed insights powered by LLM + RAG
                    </p>
                  </div>
                  <button onClick={() => setActiveTab("chat")}
                    className="flex items-center gap-2 text-sm px-4 py-2 rounded-xl transition-all duration-200 hover:scale-105"
                    style={{ background: "rgba(255,107,53,0.10)", border: "1px solid rgba(255,107,53,0.25)", color: "#ff6b35" }}>
                    Chat with AI →
                  </button>
                </div>
                <ResumeScore analysis={resumeData.analysis} />
              </div>

              {/* What to do next card */}
              <div className="glass-card rounded-2xl p-5" style={{ border: "1px solid rgba(255,255,255,0.07)" }}>
                <h3 className="font-semibold mb-3" style={{ color: "#f0f0f5" }}>🚀 Next Steps</h3>
                <div className="space-y-2">
                  {[
                    "Review the weaknesses above and update your resume accordingly",
                    "Add the missing ATS keywords naturally into your experience section",
                    "Use the Chat tab to ask specific questions about improving your resume",
                    "Re-upload your updated resume to track your score improvement",
                  ].map((step, i) => (
                    <div key={i} className="flex gap-3 text-sm" style={{ color: "#8a8aa5" }}>
                      <span style={{ color: "#ff6b35", flexShrink: 0 }}>{i + 1}.</span>
                      {step}
                    </div>
                  ))}
                </div>
              </div>

              {/* Quick stats from analysis */}
              {resumeData.analysis && (
                <div className="grid grid-cols-3 gap-3">
                  {[
                    { label: "Strengths Found",   value: resumeData.analysis.strengths?.length || 0,   color: "#22c55e" },
                    { label: "Areas to Improve",  value: resumeData.analysis.weaknesses?.length || 0,  color: "#f97316" },
                    { label: "ATS Keywords",       value: resumeData.analysis.atsKeywords?.length || 0, color: "#a78bfa" },
                  ].map((s, i) => (
                    <div key={i} className="glass-card rounded-2xl p-4 text-center"
                      style={{ border: "1px solid rgba(255,255,255,0.07)" }}>
                      <p className="text-2xl font-bold font-display" style={{ color: s.color }}>{s.value}</p>
                      <p className="text-xs mt-1" style={{ color: "#6b6b85" }}>{s.label}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* ── CHAT TAB ── */}
          {activeTab === "chat" && resumeData && (
            <div className="max-w-3xl mx-auto space-y-4">
              {/* Score chip */}
              {resumeData.analysis?.score && (
                <div className="flex items-center justify-between px-4 py-2 rounded-2xl"
                  style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.07)" }}>
                  <div className="flex items-center gap-3">
                    <span className="text-xs" style={{ color: "#6b6b85" }}>Resume Score:</span>
                    <span className="font-bold font-mono" style={{ color: "#ff6b35" }}>
                      {resumeData.analysis.score}/100
                    </span>
                    <button onClick={() => setActiveTab("analysis")}
                      className="text-xs underline underline-offset-2"
                      style={{ color: "#6b6b85" }}>
                      View full report
                    </button>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse" />
                    <span className="text-xs" style={{ color: "#6b6b85" }}>RAG Active</span>
                  </div>
                </div>
              )}

              {/* Chat window */}
              <div className="glass-card gradient-border rounded-3xl overflow-hidden">
                <Chatbot resumeId={resumeData.resumeId} />
              </div>

              {/* Chat tips */}
              <div className="glass-card rounded-2xl p-4" style={{ border: "1px solid rgba(255,255,255,0.07)" }}>
                <p className="text-xs font-semibold mb-2 uppercase tracking-widest" style={{ color: "#6b6b85" }}>
                  Suggested Questions
                </p>
                <div className="flex flex-wrap gap-2">
                  {[
                    "What are my top 3 skills?",
                    "Which companies should I target?",
                    "How do I improve my summary?",
                    "Am I ready for a senior role?",
                    "What's missing from my resume?",
                  ].map((q, i) => (
                    <span key={i} className="text-xs px-3 py-1.5 rounded-full cursor-default"
                      style={{ background: "rgba(167,139,250,0.08)", border: "1px solid rgba(167,139,250,0.2)", color: "#a78bfa" }}>
                      {q}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <footer className="text-center mt-14 pb-4" style={{ borderTop: "1px solid rgba(255,255,255,0.04)", paddingTop: "2rem" }}>
          <div className="flex items-center justify-center gap-2 mb-2">
            <Logo />
          </div>
          <p className="text-xs" style={{ color: "#3d3d55" }}>
            Built with Spring Boot · Spring AI · PostgreSQL · React · TailwindCSS
          </p>
          <p className="text-xs mt-1" style={{ color: "#3d3d55" }}>
            © {new Date().getFullYear()} ResumeAI · All rights reserved
          </p>
        </footer>
      </div>
    </div>
  );
};

export default Dashboard;