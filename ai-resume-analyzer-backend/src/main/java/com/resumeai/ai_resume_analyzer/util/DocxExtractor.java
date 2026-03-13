package com.resumeai.ai_resume_analyzer.util;


import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

@Component
public class DocxExtractor {

    /**
     * Extracts plain text from DOCX bytes using Apache Tika.
     * Tika handles DOCX natively — same approach as PDF.
     */
    public String extract(byte[] docxBytes) {
        try {
            var resource = new ByteArrayResource(docxBytes);
            var reader = new TikaDocumentReader(resource);
            var docs = reader.get();
            if (docs == null || docs.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            for (var doc : docs) {
                sb.append(doc.getText()).append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from DOCX: " + e.getMessage(), e);
        }
    }
}