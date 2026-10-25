package com.resumeai.ai_resume_analyzer.service;

import com.resumeai.ai_resume_analyzer.dto.ResumeAnalysisResponse;
import com.resumeai.ai_resume_analyzer.dto.UploadResponse;
import com.resumeai.ai_resume_analyzer.model.Resume;
import com.resumeai.ai_resume_analyzer.model.User;
import com.resumeai.ai_resume_analyzer.repository.ResumeRepository;
import com.resumeai.ai_resume_analyzer.repository.UserRepository;
import com.resumeai.ai_resume_analyzer.security.SecurityUtils;
import com.resumeai.ai_resume_analyzer.util.DocxExtractor;
import com.resumeai.ai_resume_analyzer.util.PdfExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final PdfExtractor pdfExtractor;
    private final DocxExtractor docxExtractor;
    private final EmbeddingService embeddingService;
    private final AnalysisService analysisService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final int GUEST_MAX_UPLOADS = 2;

    /**
     * Handles upload: extract text → persist → embed chunks.
     * Enforces user ownership and guest upload limits.
     */
    @Transactional
    public UploadResponse upload(MultipartFile file) {
        validateFile(file);

        String currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Authentication required to upload a resume."));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AccessDeniedException("User account not found."));

        if (Boolean.TRUE.equals(user.getIsGuest())) {
            long existingCount = resumeRepository.countByUserId(user.getId());
            if (existingCount >= GUEST_MAX_UPLOADS) {
                throw new IllegalArgumentException("Guest upload limit reached (max " + GUEST_MAX_UPLOADS + " resumes). Please create an account or sign in to upload more.");
            }
        }

        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        byte[] bytes;

        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Could not read uploaded file", e);
        }

        String rawText = extractText(bytes, ext);
        if (rawText.isBlank()) {
            throw new IllegalArgumentException("No text could be extracted from the file.");
        }

        Resume resume = Resume.builder()
                .fileName(originalName)
                .fileType(ext)
                .rawText(rawText)
                .fileSize(file.getSize())
                .status("UPLOADED")
                .user(user)
                .build();

        resume = resumeRepository.save(resume);
        user.setUploadCount((user.getUploadCount() != null ? user.getUploadCount() : 0) + 1);
        userRepository.save(user);

        log.info("Saved resume {} ({}) for user {}", resume.getId(), originalName, user.getId());

        // Async-safe: embed in same thread
        embeddingService.embedAndStore(resume);

        return UploadResponse.builder()
                .id(resume.getId())
                .fileName(resume.getFileName())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .status(resume.getStatus())
                .message("Resume uploaded and indexed successfully.")
                .build();
    }

    /**
     * Runs AI analysis on an already-uploaded resume.
     * Verifies resume ownership.
     */
    @Transactional
    public ResumeAnalysisResponse analyze(String resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + resumeId));

        verifyResumeOwnership(resume);

        ResumeAnalysisResponse result = analysisService.analyze(resumeId, resume.getRawText());

        resume.setStatus("ANALYZED");
        resumeRepository.save(resume);

        return result;
    }

    @Transactional(readOnly = true)
    public Resume getById(String id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + id));

        verifyResumeOwnership(resume);
        return resume;
    }

    public void verifyResumeOwnership(Resume resume) {
        if (resume.getUser() == null) {
            return; // Legacy resume without user
        }
        String currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Authentication required to access this resume."));

        if (!resume.getUser().getId().equals(currentUserId)) {
            log.warn("Access denied: User {} attempted to access resume {} owned by {}",
                    currentUserId, resume.getId(), resume.getUser().getId());
            throw new AccessDeniedException("You do not have permission to access this resume.");
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is empty.");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("File exceeds 10MB limit.");

        String ext = getExtension(file.getOriginalFilename());
        if (!ext.equals("pdf") && !ext.equals("docx")) {
            throw new IllegalArgumentException("Only PDF and DOCX files are accepted.");
        }
    }

    private String extractText(byte[] bytes, String ext) {
        return switch (ext) {
            case "pdf"  -> pdfExtractor.extract(bytes);
            case "docx" -> docxExtractor.extract(bytes);
            default     -> throw new IllegalArgumentException("Unsupported file type: " + ext);
        };
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
