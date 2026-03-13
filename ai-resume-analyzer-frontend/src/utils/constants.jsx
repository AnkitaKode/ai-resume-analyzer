export const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080/api";

export const ACCEPTED_FILE_TYPES = {
  "application/pdf": [".pdf"],
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document": [".docx"],
};

export const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

export const SCORE_LABELS = [
  { min: 90, label: "Excellent", color: "#22c55e" },
  { min: 75, label: "Good", color: "#84cc16" },
  { min: 60, label: "Average", color: "#eab308" },
  { min: 40, label: "Below Average", color: "#f97316" },
  { min: 0,  label: "Needs Major Work", color: "#ef4444" },
];

export const getScoreLabel = (score) => {
  return SCORE_LABELS.find((s) => score >= s.min) || SCORE_LABELS[SCORE_LABELS.length - 1];
};

export const SAMPLE_QUESTIONS = [
  "What are my strongest technical skills?",
  "How can I improve my resume for a senior role?",
  "What ATS keywords am I missing?",
  "Summarize my work experience.",
  "What roles am I best suited for?",
];