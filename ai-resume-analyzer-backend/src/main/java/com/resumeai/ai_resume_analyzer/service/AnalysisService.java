package com.resumeai.ai_resume_analyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.ai_resume_analyzer.dto.ResumeAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Value("classpath:prompts/resume_analysis_prompt.txt")
    private Resource analysisPromptResource;

    /**
     * Sends resume text to GPT-4o and parses structured JSON response.
     */
    public ResumeAnalysisResponse analyze(String resumeId, String resumeText) {
        log.info("Analyzing resume: {}", resumeId);

        try {
            String systemPrompt = analysisPromptResource.getContentAsString(StandardCharsets.UTF_8);
            String userContent = "Resume Text:\n\n" + truncate(resumeText, 6000);

            var response = chatModel.call(
                    new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userContent)))
            );

            String rawJson = response.getResult().getOutput().getText();
            rawJson = rawJson.replaceAll("```json", "").replaceAll("```", "").trim();

            var parsed = objectMapper.readValue(rawJson, ResumeAnalysisResponse.class);
            parsed.setResumeId(resumeId);
            return parsed;

        } catch (Exception e) {
            log.error("Analysis failed for resume {}", resumeId, e);
            e.printStackTrace();
            return ResumeAnalysisResponse.builder()
                    .resumeId(resumeId)
                    .score(50)
                    .strengths(List.of("Could not parse strengths"))
                    .weaknesses(List.of("Analysis encountered an error"))
                    .suggestions(List.of("Please try again"))
                    .atsKeywords(List.of())
                    .summary("Analysis failed: " + e.getMessage())
                    .build();
        }
    }

    private String truncate(String text, int maxChars) {
        return text.length() > maxChars ? text.substring(0, maxChars) + "..." : text;
    }
}