import axios from "axios";
import { API_BASE_URL } from "../utils/constants";

// Root backend URL (without /api) for OAuth authorization endpoints
export const BACKEND_ROOT_URL = API_BASE_URL.endsWith("/api")
  ? API_BASE_URL.slice(0, -4)
  : API_BASE_URL.replace(/\/api\/?$/, "");

export const GOOGLE_AUTH_URL = `https://ai-resume-analyzer-jrl3.onrender.com/oauth2/authorization/google`;

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
  withCredentials: true, // Send HTTP-only cookies on all requests
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
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      "Something went wrong";
    const status = error.response?.status;
    const customError = new Error(message);
    customError.status = status;
    customError.response = error.response;
    return Promise.reject(customError);
  }
);

// ── Auth API ───────────────────────────────────────────────────────────────

export const signInApi = (data) => api.post("/auth/signin", data);

export const signUpApi = (data) => api.post("/auth/signup", data);

export const continueAsGuestApi = () => api.post("/auth/guest");

export const getCurrentUserApi = () => api.get("/auth/me");

export const logoutApi = () => api.post("/auth/logout");

export const forgotPasswordApi = (email) =>
  api.post("/auth/forgot-password", { email });

export const resetPasswordApi = (token, newPassword) =>
  api.post("/auth/reset-password", { token, newPassword });

export const transferGuestDataApi = (guestSessionToken) =>
  api.post("/auth/transfer-guest-data", { guestSessionToken });

// ── Resume API ─────────────────────────────────────────────────────────────

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
