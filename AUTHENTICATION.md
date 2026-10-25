# 🔐 Authentication & Authorization Architecture & Setup Guide

This document describes the authentication system implemented for **AI Resume Analyzer & RAG Chatbot**.

---

## 🌟 Architecture Overview

```
                                      ┌────────────────────────────────────────────────┐
                                      │              Browser / Client UI               │
                                      │        (React 19 + Tailwind CSS)               │
                                      └───────┬──────────────────────────────▲─────────┘
                                              │                              │
                    1. Credentials / Guest    │                              │  4. HTTP-only Cookie
                       OAuth2 Redirect        │                              │     (AUTH-TOKEN JWT)
                                              ▼                              │
┌────────────────────────────────────────────────────────────────────────────┴─────────────────────────────┐
│                                       Spring Boot 3.4.5 Backend                                          │
│                                                                                                          │
│  ┌────────────────────────┐   ┌─────────────────────────────┐   ┌─────────────────────────────────────┐  │
│  │     AuthController     │   │      JwtAuthenticationFilter│   │  OAuth2AuthenticationSuccessHandler │  │
│  │ /auth/signin           │   │ Intercepts HTTP-only Cookie │   │ Issues HTTP-only JWT on success     │  │
│  │ /auth/signup           │   │ Validates HMAC-SHA256 Token │   │ Redirects to /?oauth_success=true   │  │
│  │ /auth/guest            │   │ Populates SecurityContext   │   └─────────────────────────────────────┘  │
│  │ /auth/me, /auth/logout │   └──────────────┬──────────────┘                                            │
│  └───────────┬────────────┘                  │                                                           │
│              ▼                               ▼                                                           │
│  ┌──────────────────────────────────────────────────────────┐   ┌─────────────────────────────────────┐  │
│  │                  AuthService & SecurityUtils             │   │        Spring Security Filters      │  │
│  │  - BCrypt password hashing                               │   │  - Stateless session                │  │
│  │  - UserPrincipal resolution                              │   │  - CSRF disabled (SameSite cookies) │  │
│  │  - Guest session tracking & data transfer                │   │  - CORS withCredentials: true       │  │
│  │  - Dev-mode password reset flow                          │   │  - 401 & 403 Rest handlers          │  │
│  └───────────────────────────┬──────────────────────────────┘   └─────────────────────────────────────┘  │
│                              │                                                                           │
│  ┌───────────────────────────┴──────────────────────────────┐                                            │
│  │                 Secure Data & RAG Retrieval               │                                            │
│  │  ResumeService: Guest limit (2) & user ownership check   │                                            │
│  │  ChatService: Guest limit (5) & user ownership check     │                                            │
│  │  EmbeddingService: Scoped vector similarity queries:     │                                            │
│  │    `resume_chunks JOIN resumes WHERE r.user_id = :userId`│                                            │
│  └───────────────────────────┬──────────────────────────────┘                                            │
└──────────────────────────────┼───────────────────────────────────────────────────────────────────────────┘
                               ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                    PostgreSQL with pgvector                                              │
│                                                                                                          │
│  ┌──────────────┐         ┌──────────────────┐         ┌────────────────────────┐         ┌───────────┐  │
│  │    users     │1───────*│ linked_accounts  │         │ password_reset_tokens  │         │  resumes  │  │
│  │ id (UUID)    │         │ provider         │         │ token                  │         │ id (UUID) │  │
│  │ email (uniq) │         │ provider_subject │         │ expiry_date            │1───────*│ user_id   │  │
│  │ password_hash│         └──────────────────┘         └────────────────────────┘         └─────┬─────┘  │
│  │ is_guest     │                                                                               │        │
│  └──────┬───────┘                                                                               │1       │
│         │                                                                                       │        │
│         └───────────────────────────────────────────────────────────────────────────────────────┼────────┤
│                                                                                                 ▼*       │
│                                                                                           resume_chunks  │
│                                                                                           (vector embed) │
└──────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Key Features Implemented

1. **Email & Password Authentication**:
   - Secure registration with client-side & server-side validation.
   - BCrypt hashing with salt rounds. Plain-text passwords are never stored or logged.
   - Unique email constraint in PostgreSQL preventing account duplicates.
   - Expiring Password Reset Token flow with local development mode fallback (displays token directly when SMTP is unconfigured).

2. **HTTP-only, Secure Cookie JWT**:
   - Tokens are **never** stored in `localStorage` or `sessionStorage` (preventing XSS credential theft).
   - `SameSite=Lax`, `HttpOnly=true`, and configurable `Secure=true` (for HTTPS in production).
   - Supports 30-day "Remember Me" extended token lifespan.

3. **Google OAuth2 / OpenID Connect**:
   - Standard Spring Security OAuth2 Client integration.
   - Automatically links accounts matching verified email or creates new user accounts.
   - Clean cookie-based redirect with `?oauth_success=true`.

4. **Guest Mode & Seamless Data Transfer**:
   - Visitors can explore without signing up via "Continue as Guest".
   - Backend enforces strict limits: **maximum 2 resume uploads** and **maximum 5 AI questions**.
   - Guest data is completely isolated under a temporary guest account ID.
   - When a guest signs up or signs in, their existing resumes, analyses, and embeddings are securely migrated to their registered account.

6. **Strict Data & RAG Retrieval Isolation**:
   - Resumes and chat interactions are linked to the user (`user_id`).
   - Every file upload, analysis retrieval, and chat question validates ownership at the service layer (`verifyResumeOwnership`).
   - Vector similarity search (`retrieveRelevantChunks`) enforces ownership by joining `resumes` and filtering by `r.user_id = :userId`.

---

## 🛠️ Setup Instructions

### 1. Prerequisites
- **Java**: 21+
- **Node.js**: 18+ (tested with v20 & v24)
- **PostgreSQL**: 15+ with `pgvector` extension enabled
- **Ollama**: (optional, for local LLM) running on `http://localhost:11434` with `llama3.1:8b` and `mxbai-embed-large`

---

### 2. Google OAuth 2.0 Setup (Google Cloud Console)

1. Open [Google Cloud Console](https://console.cloud.google.com/).
2. Select or create a project (e.g., `ai-resume-analyzer`).
3. Navigate to **APIs & Services** > **OAuth consent screen**:
   - Select **External** (or Internal for Google Workspace).
   - Set App name: `ResumeAI`.
   - Add user support email and developer contact email.
   - Add scopes: `.../auth/userinfo.email`, `.../auth/userinfo.profile`, `openid`.
   - Add test users if in "Testing" publishing status.
4. Navigate to **APIs & Services** > **Credentials**:
   - Click **+ CREATE CREDENTIALS** > **OAuth client ID**.
   - Application type: **Web application**.
   - Name: `ResumeAI Web Client`.
   - **Authorized JavaScript origins**:
     - `http://localhost:3000` (Local frontend)
     - `https://your-domain.vercel.app` (Production frontend)
   - **Authorized redirect URIs**:
     - `http://localhost:8080/login/oauth2/code/google` (Local backend)
     - `https://your-backend-domain.com/login/oauth2/code/google` (Production backend)
5. Copy the **Client ID** and **Client Secret** into your `.env` file:
   ```env
   GOOGLE_CLIENT_ID=123456789-abc.apps.googleusercontent.com
   GOOGLE_CLIENT_SECRET=GOCSPX-your-secret-here
   ```

---

### 3. Database Migrations

The project includes Flyway migration scripts located in:
`ai-resume-analyzer-backend/src/main/resources/db/migration/`:

- `V1__init_schema.sql`: Initializes baseline `resumes` and `resume_chunks` tables with pgvector extension.
- `V2__auth_schema.sql`:
  - Creates `users` table (`id`, `full_name`, `email`, `password_hash`, `role`, `is_guest`, `guest_session_token`, `upload_count`, `chat_message_count`).
  - Creates `linked_accounts` table (`user_id`, `provider`, `provider_subject`, `provider_email`).
  - Creates `password_reset_tokens` table (`token`, `user_id`, `expiry_date`).
  - Adds `user_id` foreign key column and indexes to `resumes`.

Migrations execute automatically when starting the backend Spring Boot application. Alternatively, to run manually in PostgreSQL:
```sql
\c resumeai
\i ai-resume-analyzer-backend/src/main/resources/db/migration/V1__init_schema.sql
\i ai-resume-analyzer-backend/src/main/resources/db/migration/V2__auth_schema.sql
```

---

### 5. Running the Application

#### Backend:
1. Copy `.env.example` to `.env`:
   ```bash
   cd ai-resume-analyzer-backend
   cp .env.example .env
   ```
2. Start the backend:
   ```bash
   # Windows PowerShell / CMD
   .\mvnw.cmd spring-boot:run

   # Linux / macOS
   ./mvnw spring-boot:run
   ```
   The backend will be available at `http://localhost:8080`.

#### Frontend:
1. Copy `.env.example` to `.env`:
   ```bash
   cd ai-resume-analyzer-frontend
   cp .env.example .env
   ```
2. Install packages and start:
   ```bash
   npm install
   npm start
   ```
   The frontend will be available at `http://localhost:3000`.

---

## 🧪 Testing Checklist & Verification

### Running Automated Test Suites

- **Backend Tests (22 Unit & Integration Tests)**:
  ```bash
  cd ai-resume-analyzer-backend
  .\mvnw.cmd test
  ```
  Verified tests include:
  - `AuthServiceTest`: Registration, login, guest sessions, guest transfer, password reset dev flow, token verification.
  - `JwtTokenProviderTest`: Generation, claims extraction, expiration, signature validation.
  - `ResumeSecurityAndOwnershipIntegrationTest`: Resume upload isolation, cross-user 403 Forbidden checks, guest upload limits (max 2).
  - `ChatSecurityAndGuestLimitTest`: RAG chat user scoping, guest chat limits (max 5), vector retrieval isolation.

- **Frontend Tests (Jest + React Testing Library)**:
  ```bash
  cd ai-resume-analyzer-frontend
  npm test -- --watchAll=false
  ```
  Verified tests include:
  - `App.test.js`: Unauthenticated redirect to `/signin`, authenticated user dashboard rendering, guest banner & badge rendering.
  - `AuthPages.test.js`: Form validations, password visibility toggles, strength meter, terms acceptance, dev-mode reset link.

---

## 🔒 Security Best Practices Summary

| Requirement | Implementation Detail |
|---|---|
| **No Passwords in Plain Text** | BCrypt with configurable salt rounds (`BCryptPasswordEncoder`). |
| **No Tokens in LocalStorage** | JWT is transmitted and stored solely via `HttpOnly`, `SameSite=Lax` cookie (`AUTH-TOKEN`). |
| **Cross-Site Request Forgery (CSRF)** | Mitigated via SameSite cookie policy and stateless JWT auth filter. |
| **XSS Protection** | Scripts cannot access `HttpOnly` cookies. React safely escapes injected inputs. |
| **Ownership Isolation** | Enforced at Service layer. ID manipulation fails with `AccessDeniedException` (HTTP 403). |
| **Vector DB / RAG Isolation** | Embeddings join `resumes` table filtered by `user_id`. One user cannot search another user's chunks. |
| **Guest Throttling** | Strict backend counters prevent abuse: max 2 uploads, max 5 chat messages. |

---

## ⚡ AI Provider Profiles: Localhost (Ollama) vs Cloud Deployment (Groq)

The application supports dual Spring Boot profiles configured in `application.properties`, `application-dev.properties`, and `application-prod.properties`:

### 1. `dev` Profile (Default - Localhost & 100% Offline)
- **Engine**: Local **Ollama** (`http://localhost:11434`)
- **Chat Model**: `llama3.2:3b` (fast!) or `llama3.1:8b`
- **Embedding Model**: `mxbai-embed-large`
- **Internet Requirement**: **0% (100% Offline)**
- **How to run**:
  ```bash
  .\mvnw.cmd spring-boot:run
  # Or explicitly:
  .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
  ```

### 2. `prod` Profile (Cloud Deployment - Ultra Fast & Free)
- **Engine**: **Groq Cloud API** (OpenAI-compatible) via `https://api.groq.com/openai`
- **Chat Model**: `llama-3.1-8b-instant` (~500+ tokens/sec, instant response time)
- **Embedding Model**:
  - Uses OpenAI `text-embedding-3-small` if `OPENAI_API_KEY` is provided.
  - If no OpenAI key is set, automatically uses the built-in resilient 384-dimensional feature vectorizer (`AiConfig.LocalFeatureEmbeddingModel`) for **zero API costs and zero external quota dependency**.
- **Internet Requirement**: Requires internet connection (standard for deployed cloud websites like Render, Railway, AWS, Docker).
- **How to run on deployment**:
  Set environment variable in your cloud dashboard:
  ```env
  SPRING_PROFILES_ACTIVE=prod
  GROQ_API_KEY=gsk_your_groq_api_key_here
  ```
  Or pass JVM argument:
  ```bash
  java -jar -Dspring.profiles.active=prod target/ai-resume-analyzer-0.0.1-SNAPSHOT.jar
  ```
