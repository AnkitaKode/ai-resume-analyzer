import React from "react";
import Markdown  from "react-markdown";

const UserIcon = () => (
  <div
    className="w-8 h-8 rounded-full flex items-center justify-center text-xs font-semibold flex-shrink-0"
    style={{ background: "linear-gradient(135deg, #ff6b35, #ffbe0b)", color: "#050508" }}
  >
    U
  </div>
);

const AIIcon = () => (
  <div
    className="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0"
    style={{ background: "rgba(255,107,53,0.12)", border: "1px solid rgba(255,107,53,0.25)" }}
  >
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
      <path
        d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"
        stroke="#ff6b35"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  </div>
);

const ChatMessage = ({ message }) => {
  const isUser = message.role === "user";

  return (
    <div className={`message-enter flex gap-3 ${isUser ? "flex-row-reverse" : "flex-row"}`}>
      {isUser ? <UserIcon /> : <AIIcon />}
      <div
        className={`max-w-[78%] rounded-2xl px-4 py-3 ${
          isUser
            ? "rounded-tr-sm"
            : "rounded-tl-sm"
        }`}
        style={
          isUser
            ? {
                background: "linear-gradient(135deg, rgba(255,107,53,0.18), rgba(255,190,11,0.10))",
                border: "1px solid rgba(255,107,53,0.2)",
              }
            : {
                background: "rgba(255,255,255,0.03)",
                border: "1px solid rgba(255,255,255,0.07)",
              }
        }
      >
        {isUser ? (
          <p className="text-sm leading-relaxed" style={{ color: "#f0f0f5" }}>
            {message.content}
          </p>
        ) : (
          <div className="prose-dark text-sm leading-relaxed">
            <Markdown >{message.content}</Markdown>
          </div>
        )}
        <p className="text-xs mt-2" style={{ color: "#3d3d55" }}>
          {message.timestamp
            ? new Date(message.timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
            : ""}
        </p>
      </div>
    </div>
  );
};

export default ChatMessage;