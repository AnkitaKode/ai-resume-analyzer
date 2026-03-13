import React, { useState, useRef, useEffect } from "react";
import ChatMessage from "./ChatMessage";
import { TypingIndicator } from "./Loader";
import { sendChatMessage } from "../services/api";
import { SAMPLE_QUESTIONS } from "../utils/constants";

const SendIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="22" y1="2" x2="11" y2="13"/>
    <polygon points="22 2 15 22 11 13 2 9 22 2"/>
  </svg>
);

const Chatbot = ({ resumeId }) => {
  const [messages, setMessages] = useState([
    {
      role: "assistant",
      content: "👋 Hi! I've analyzed your resume. Ask me anything about it — your skills, experience, how to improve it, or ATS optimization tips.",
      timestamp: new Date().toISOString(),
    },
  ]);
  const [input, setInput] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const [errorDetail, setErrorDetail] = useState("");
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isTyping]);

  const sendMessage = async (text) => {
    const content = text || input.trim();
    if (!content || isTyping) return;

    // Only send role+content in history, not timestamps
    const history = messages.map(({ role, content }) => ({ role, content }));
    const userMsg = { role: "user", content, timestamp: new Date().toISOString() };

    setMessages((prev) => [...prev, userMsg]);
    setInput("");
    setIsTyping(true);
    setErrorDetail("");

    try {
      const res = await sendChatMessage(resumeId, content, history);

      // Handle both response shapes
      const reply = res?.message || res?.response || res;

      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: typeof reply === "string" ? reply : JSON.stringify(reply),
          timestamp: new Date().toISOString(),
        },
      ]);
    } catch (err) {
      // Show the real error message so debugging is easier
      const errMsg = err?.message || "Unknown error";
      setErrorDetail(errMsg);
      console.error("Chat error:", errMsg);

      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: `⚠️ Failed to get response. Error: ${errMsg}`,
          timestamp: new Date().toISOString(),
        },
      ]);
    } finally {
      setIsTyping(false);
      inputRef.current?.focus();
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <div className="flex flex-col h-full" style={{ minHeight: "500px", maxHeight: "680px" }}>
      {/* Header */}
      <div
        className="px-5 py-4 flex items-center gap-3 flex-shrink-0"
        style={{ borderBottom: "1px solid rgba(255,255,255,0.06)" }}
      >
        <div
          className="w-8 h-8 rounded-full flex items-center justify-center"
          style={{ background: "rgba(255,107,53,0.12)", border: "1px solid rgba(255,107,53,0.25)" }}
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ff6b35" strokeWidth="2">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <div>
          <p className="text-sm font-semibold" style={{ color: "#f0f0f5" }}>Resume AI</p>
          <div className="flex items-center gap-1.5">
            <div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse" />
            <p className="text-xs" style={{ color: "#6b6b85" }}>Ready to help</p>
          </div>
        </div>

        {/* Show resumeId for debugging — remove in production */}
        <div className="ml-auto">
          <p className="text-xs font-mono" style={{ color: "#3d3d55" }}>
            ID: {resumeId?.slice(0, 8)}...
          </p>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4">
        {messages.map((msg, i) => (
          <ChatMessage key={i} message={msg} />
        ))}
        {isTyping && (
          <div className="flex gap-3">
            <div
              className="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0"
              style={{ background: "rgba(255,107,53,0.12)", border: "1px solid rgba(255,107,53,0.25)" }}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ff6b35" strokeWidth="2">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
              </svg>
            </div>
            <div
              className="rounded-2xl rounded-tl-sm"
              style={{ background: "rgba(255,255,255,0.03)", border: "1px solid rgba(255,255,255,0.07)" }}
            >
              <TypingIndicator />
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Sample questions — show only at start */}
      {messages.length <= 1 && (
        <div className="px-4 pb-3 flex gap-2 flex-wrap">
          {SAMPLE_QUESTIONS.slice(0, 3).map((q, i) => (
            <button
              key={i}
              onClick={() => sendMessage(q)}
              className="text-xs px-3 py-1.5 rounded-full transition-all duration-200 hover:scale-105 active:scale-95"
              style={{
                background: "rgba(255,107,53,0.08)",
                border: "1px solid rgba(255,107,53,0.2)",
                color: "#ff6b35",
              }}
            >
              {q}
            </button>
          ))}
        </div>
      )}

      {/* Input */}
      <div
        className="px-4 py-3 flex-shrink-0"
        style={{ borderTop: "1px solid rgba(255,255,255,0.06)" }}
      >
        <div
          className="flex items-end gap-3 rounded-xl px-4 py-3"
          style={{ background: "rgba(255,255,255,0.04)", border: "1px solid rgba(255,255,255,0.08)" }}
        >
          <textarea
            ref={inputRef}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask about your resume..."
            rows={1}
            className="flex-1 bg-transparent resize-none outline-none text-sm leading-relaxed"
            style={{ color: "#f0f0f5", maxHeight: "120px", overflowY: "auto" }}
            onInput={(e) => {
              e.target.style.height = "auto";
              e.target.style.height = Math.min(e.target.scrollHeight, 120) + "px";
            }}
          />
          <button
            onClick={() => sendMessage()}
            disabled={!input.trim() || isTyping}
            className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 transition-all duration-200 active:scale-90 disabled:opacity-30"
            style={{
              background: input.trim() && !isTyping
                ? "linear-gradient(135deg, #ff6b35, #ffbe0b)"
                : "rgba(255,255,255,0.06)",
              color: input.trim() && !isTyping ? "#050508" : "#6b6b85",
            }}
          >
            <SendIcon />
          </button>
        </div>
        <p className="text-xs text-center mt-2" style={{ color: "#3d3d55" }}>
          Powered by RAG · Enter to send
        </p>
      </div>
    </div>
  );
};

export default Chatbot;