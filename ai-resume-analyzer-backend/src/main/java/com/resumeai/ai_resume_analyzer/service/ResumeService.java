package com.resumeai.ai_resume_analyzer.service;

import com.resumeai.ai_resume_analyzer.dto.ResumeAnalysisResponse;
import com.resumeai.ai_resume_analyzer.dto.UploadResponse;
import com.resumeai.ai_resume_analyzer.model.Resume;
import com.resumeai.ai_resume_analyzer.repository.ResumeRepository;
import com.resumeai.ai_resume_analyzer.util.DocxExtractor;
import com.resumeai.ai_resume_analyzer.util.PdfExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final PdfExtractor pdfExtractor;
    private final DocxExtractor docxExtractor;
    private final EmbeddingService embeddingService;
    private final AnalysisService analysisService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Handles upload: extract text → persist → embed chunks.
     */
    @Transactional
    public UploadResponse upload(MultipartFile file) {
        validateFile(file);

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
                .build();

        resume = resumeRepository.save(resume);
        log.info("Saved resume {} ({})", resume.getId(), originalName);

        // Async-safe: embed in same thread (can move to @Async later)
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
     */
    @Transactional
    public ResumeAnalysisResponse analyze(String resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        ResumeAnalysisResponse result = analysisService.analyze(resumeId, resume.getRawText());

        resume.setStatus("ANALYZED");
        resumeRepository.save(resume);

        return result;
    }

    public Resume getById(String id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + id));
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