package com.resumeai.ai_resume_analyzer.service;

import com.resumeai.ai_resume_analyzer.dto.ChatRequest;
import com.resumeai.ai_resume_analyzer.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatModel chatModel;
    private final EmbeddingService embeddingService;

    @Value("classpath:prompts/rag_prompt.txt")
    private Resource ragPromptResource;

    private static final int TOP_K = 4;

    public ChatResponse chat(ChatRequest request) {
        log.info("Chat request for resume: {}", request.getResumeId());

        try {
            List<String> relevantChunks = embeddingService.retrieveRelevantChunks(
                    request.getResumeId(), request.getMessage(), TOP_K);

            String context = relevantChunks.isEmpty()
                    ? "No resume context found."
                    : String.join("\n\n---\n\n", relevantChunks);

            String systemPrompt = buildSystemPrompt(context);
            List<Message> messages = buildMessageHistory(systemPrompt, request);

            var response = chatModel.call(new Prompt(messages));

            // FIX: getText() not getContent()
            String reply = response.getResult().getOutput().getText();

            return ChatResponse.builder()
                    .message(reply)
                    .resumeId(request.getResumeId())
                    .build();

        } catch (Exception e) {
            log.error("Chat failed for resume {}: {}", request.getResumeId(), e.getMessage(), e);
            // Return error as message so frontend shows real reason
            return ChatResponse.builder()
                    .message("Error: " + e.getMessage())
                    .resumeId(request.getResumeId())
                    .build();
        }
    }

    private String buildSystemPrompt(String context) {
        try {
            String template = ragPromptResource.getContentAsString(StandardCharsets.UTF_8);
            return template.replace("{{context}}", context);
        } catch (Exception e) {
            log.warn("Could not load rag_prompt.txt, using default prompt");
            return """
                You are a professional resume advisor AI. Use the following resume excerpts to answer questions:
                
                %s
                
                Always be specific, actionable, and reference the resume content directly.
                """.formatted(context);
        }
    }

    private List<Message> buildMessageHistory(String systemPrompt, ChatRequest request) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));

        if (request.getHistory() != null) {
            int start = Math.max(0, request.getHistory().size() - 10);
            for (var item : request.getHistory().subList(start, request.getHistory().size())) {
                if ("user".equals(item.getRole())) {
                    messages.add(new UserMessage(item.getContent()));
                } else if ("assistant".equals(item.getRole())) {
                    messages.add(new AssistantMessage(item.getContent()));
                }
            }
        }

        messages.add(new UserMessage(request.getMessage()));
        return messages;
    }
}