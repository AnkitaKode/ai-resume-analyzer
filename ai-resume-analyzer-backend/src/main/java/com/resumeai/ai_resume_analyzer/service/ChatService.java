package com.resumeai.ai_resume_analyzer.service;

import com.resumeai.ai_resume_analyzer.dto.ChatRequest;
import com.resumeai.ai_resume_analyzer.dto.ChatResponse;
import com.resumeai.ai_resume_analyzer.model.Resume;
import com.resumeai.ai_resume_analyzer.model.User;
import com.resumeai.ai_resume_analyzer.repository.ResumeRepository;
import com.resumeai.ai_resume_analyzer.repository.UserRepository;
import com.resumeai.ai_resume_analyzer.security.SecurityUtils;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatModel chatModel;
    private final EmbeddingService embeddingService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    @Value("classpath:prompts/rag_prompt.txt")
    private Resource ragPromptResource;

    private static final int TOP_K = 4;
    public static final int GUEST_MAX_CHATS = 5;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        log.info("Chat request for resume: {}", request.getResumeId());

        String currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Authentication required to chat with AI."));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AccessDeniedException("User account not found."));

        // Enforce guest limit
        if (Boolean.TRUE.equals(user.getIsGuest())) {
            int chatCount = user.getChatMessageCount() != null ? user.getChatMessageCount() : 0;
            if (chatCount >= GUEST_MAX_CHATS) {
                throw new IllegalArgumentException("Guest chat limit reached (max " + GUEST_MAX_CHATS + " questions). Please create an account or sign in to continue chatting.");
            }
        }

        // Verify resume ownership
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + request.getResumeId()));

        if (resume.getUser() != null && !resume.getUser().getId().equals(user.getId())) {
            log.warn("Access denied: User {} attempted to chat about resume {} owned by {}",
                    user.getId(), resume.getId(), resume.getUser().getId());
            throw new AccessDeniedException("You do not have permission to access this resume.");
        }

        try {
            // Strictly retrieve chunks scoped to this resume and this user
            List<String> relevantChunks = embeddingService.retrieveRelevantChunks(
                    request.getResumeId(), user.getId(), request.getMessage(), TOP_K);

            String context = relevantChunks.isEmpty()
                    ? "No resume context found."
                    : String.join("\n\n---\n\n", relevantChunks);

            String systemPrompt = buildSystemPrompt(context);
            List<Message> messages = buildMessageHistory(systemPrompt, request);

            var response = chatModel.call(new Prompt(messages));

            String reply = response.getResult().getOutput().getText();

            // Increment chat message count for the user
            user.setChatMessageCount((user.getChatMessageCount() != null ? user.getChatMessageCount() : 0) + 1);
            userRepository.save(user);

            return ChatResponse.builder()
                    .message(reply)
                    .resumeId(request.getResumeId())
                    .build();

        } catch (AccessDeniedException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Chat failed for resume {}: {}", request.getResumeId(), e.getMessage(), e);
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
