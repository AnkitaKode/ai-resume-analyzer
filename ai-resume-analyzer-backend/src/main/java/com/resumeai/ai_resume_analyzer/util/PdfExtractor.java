package com.resumeai.ai_resume_analyzer.util;

import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

@Component
public class PdfExtractor {

    /**
     * Extracts plain text from PDF bytes using Apache Tika.
     */
    public String extract(byte[] pdfBytes) {
        try {
            var resource = new ByteArrayResource(pdfBytes);
            var reader = new TikaDocumentReader(resource);
            var docs = reader.get();
            if (docs == null || docs.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            for (var doc : docs) {
                sb.append(doc.getText()).append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }
}