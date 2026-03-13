import axios from "axios";
import { API_BASE_URL } from "../utils/constants";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// Request interceptor
api.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
);

// Response interceptor
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message || error.message || "Something went wrong";
    return Promise.reject(new Error(message));
  }
);

/**
 * Upload resume PDF/DOCX
 * @param {File} file
 * @param {Function} onProgress
 * @returns {Promise<UploadResponse>}
 */
export const uploadResume = (file, onProgress) => {
  const formData = new FormData();
  formData.append("file", file);

  return api.post("/resume/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
    onUploadProgress: (e) => {
      if (onProgress && e.total) {
        onProgress(Math.round((e.loaded * 100) / e.total));
      }
    },
  });
};

/**
 * Analyze an uploaded resume
 * @param {string} resumeId
 * @returns {Promise<ResumeAnalysisResponse>}
 */
export const analyzeResume = (resumeId) => {
  return api.post(`/resume/${resumeId}/analyze`);
};

/**
 * Send a chat message about the resume
 * @param {string} resumeId
 * @param {string} message
 * @param {Array} history - [{role, content}]
 * @returns {Promise<ChatResponse>}
 */
export const sendChatMessage = (resumeId, message, history = []) => {
  return api.post("/chat/message", { resumeId, message, history });
};

/**
 * Get resume metadata by ID
 * @param {string} resumeId
 */
export const getResume = (resumeId) => {
  return api.get(`/resume/${resumeId}`);
};

export default api;