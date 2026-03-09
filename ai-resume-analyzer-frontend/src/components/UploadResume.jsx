import React, { useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";
import { ACCEPTED_FILE_TYPES, MAX_FILE_SIZE } from "../utils/constants";
import { uploadResume, analyzeResume } from "../services/api";
import { Loader } from "./Loader";

const UploadIcon = () => (
  <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="16 16 12 12 8 16" />
    <line x1="12" y1="12" x2="12" y2="21" />
    <path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3" />
  </svg>
);

const FileIcon = ({ type }) => (
  <div
    className="w-12 h-12 rounded-xl flex items-center justify-center text-xs font-bold font-mono"
    style={{
      background: type === "pdf" ? "rgba(239,68,68,0.12)" : "rgba(59,130,246,0.12)",
      border: `1px solid ${type === "pdf" ? "rgba(239,68,68,0.3)" : "rgba(59,130,246,0.3)"}`,
      color: type === "pdf" ? "#ef4444" : "#3b82f6",
    }}
  >
    {type.toUpperCase()}
  </div>
);

const ProgressBar = ({ progress }) => (
  <div className="w-full">
    <div className="flex justify-between text-xs mb-1" style={{ color: "#6b6b85" }}>
      <span>Uploading...</span>
      <span>{progress}%</span>
    </div>
    <div className="h-1 rounded-full" style={{ background: "rgba(255,255,255,0.06)" }}>
      <div
        className="h-full rounded-full"
        style={{
          width: `${progress}%`,
          background: "linear-gradient(90deg, #ff6b35, #ffbe0b)",
          transition: "width 0.3s ease",
        }}
      />
    </div>
  </div>
);

const UploadResume = ({ onAnalysisComplete }) => {
  const [file, setFile] = useState(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [status, setStatus] = useState("idle"); // idle | uploading | analyzing | done | error
  const [error, setError] = useState("");

  const onDrop = useCallback((accepted, rejected) => {
    if (rejected?.length > 0) {
      setError(rejected[0].errors[0]?.message || "Invalid file.");
      return;
    }
    if (accepted?.length > 0) {
      setFile(accepted[0]);
      setError("");
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: ACCEPTED_FILE_TYPES,
    maxSize: MAX_FILE_SIZE,
    maxFiles: 1,
  });

  const fileExt = file?.name?.split(".").pop()?.toLowerCase();

  const handleUploadAndAnalyze = async () => {
    if (!file) return;
    setError("");

    try {
      setStatus("uploading");
      setUploadProgress(0);
      const uploadRes = await uploadResume(file, setUploadProgress);
      const resumeId = uploadRes.id || uploadRes.resumeId;

      setStatus("analyzing");
      const analysis = await analyzeResume(resumeId);
      onAnalysisComplete({ resumeId, analysis });
      setStatus("done");
    } catch (err) {
      setError(err.message || "Upload failed. Please try again.");
      setStatus("error");
    }
  };

  const handleReset = () => {
    setFile(null);
    setUploadProgress(0);
    setStatus("idle");
    setError("");
  };

  return (
    <div className="space-y-4">
      {/* Dropzone */}
      {!file ? (
        <div
          {...getRootProps()}
          className={`relative rounded-2xl p-10 text-center cursor-pointer transition-all duration-300 ${
            isDragActive ? "drag-active" : ""
          }`}
          style={{
            background: isDragActive ? "rgba(255,107,53,0.05)" : "rgba(255,255,255,0.02)",
            border: `2px dashed ${isDragActive ? "#ff6b35" : "rgba(255,255,255,0.1)"}`,
          }}
        >
          <input {...getInputProps()} />
          <div className="flex flex-col items-center gap-4">
            <div
              className="w-16 h-16 rounded-2xl flex items-center justify-center transition-all duration-300"
              style={{
                background: isDragActive ? "rgba(255,107,53,0.12)" : "rgba(255,255,255,0.04)",
                color: isDragActive ? "#ff6b35" : "#6b6b85",
              }}
            >
              <UploadIcon />
            </div>
            <div>
              <p className="font-semibold text-base" style={{ color: isDragActive ? "#ff6b35" : "#f0f0f5" }}>
                {isDragActive ? "Drop it here" : "Drag & drop your resume"}
              </p>
              <p className="text-sm mt-1" style={{ color: "#6b6b85" }}>
                or <span style={{ color: "#ff6b35" }} className="underline underline-offset-2">browse files</span>
              </p>
              <p className="text-xs mt-2" style={{ color: "#3d3d55" }}>
                PDF or DOCX — max 10MB
              </p>
            </div>
          </div>
        </div>
      ) : (
        /* File selected */
        <div className="glass-card gradient-border rounded-2xl p-5 space-y-4">
          <div className="flex items-center gap-4">
            <FileIcon type={fileExt} />
            <div className="flex-1 min-w-0">
              <p className="font-medium truncate" style={{ color: "#f0f0f5" }}>{file.name}</p>
              <p className="text-xs mt-0.5" style={{ color: "#6b6b85" }}>
                {(file.size / 1024).toFixed(1)} KB
              </p>
            </div>
            {status === "idle" || status === "error" ? (
              <button
                onClick={handleReset}
                className="p-2 rounded-lg transition-colors"
                style={{ color: "#6b6b85" }}
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            ) : null}
          </div>

          {(status === "uploading") && <ProgressBar progress={uploadProgress} />}

          {(status === "analyzing") && (
            <div className="flex items-center gap-3">
              <Loader size="sm" />
              <p className="text-sm" style={{ color: "#6b6b85" }}>Analyzing with AI...</p>
            </div>
          )}

          {status === "done" && (
            <div className="flex items-center gap-2 text-sm" style={{ color: "#22c55e" }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="20 6 9 17 4 12"/></svg>
              Analysis complete!
            </div>
          )}

          {(status === "idle" || status === "error") && (
            <button
              onClick={handleUploadAndAnalyze}
              className="w-full py-3 rounded-xl font-semibold text-sm transition-all duration-200 active:scale-95"
              style={{
                background: "linear-gradient(135deg, #ff6b35, #ffbe0b)",
                color: "#050508",
                boxShadow: "0 4px 20px rgba(255,107,53,0.25)",
              }}
            >
              Upload & Analyze Resume
            </button>
          )}
        </div>
      )}

      {error && (
        <div
          className="rounded-xl px-4 py-3 text-sm"
          style={{ background: "rgba(239,68,68,0.08)", border: "1px solid rgba(239,68,68,0.2)", color: "#ef4444" }}
        >
          {error}
        </div>
      )}
    </div>
  );
};

export default UploadResume;