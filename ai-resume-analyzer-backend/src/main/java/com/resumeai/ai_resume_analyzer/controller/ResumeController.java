package com.resumeai.ai_resume_analyzer.controller;

import com.resumeai.ai_resume_analyzer.dto.ResumeAnalysisResponse;
import com.resumeai.ai_resume_analyzer.dto.UploadResponse;
import com.resumeai.ai_resume_analyzer.model.Resume;
import com.resumeai.ai_resume_analyzer.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * POST /api/resume/upload
     * Accepts PDF or DOCX, extracts text, stores, and indexes embeddings.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        UploadResponse response = resumeService.upload(file);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/resume/{id}/analyze
     * Runs AI analysis on an already-uploaded resume.
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyze(@PathVariable String id) {
        ResumeAnalysisResponse response = resumeService.analyze(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/resume/{id}
     * Returns resume metadata.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resume> getById(@PathVariable String id) {
        Resume resume = resumeService.getById(id);
        // Avoid leaking raw text for large resumes in list views
        resume.setRawText(resume.getRawText().substring(0, Math.min(200, resume.getRawText().length())) + "...");
        return ResponseEntity.ok(resume);
    }
}