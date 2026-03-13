package com.resumeai.ai_resume_analyzer.controller;

import com.resumeai.ai_resume_analyzer.dto.ChatRequest;
import com.resumeai.ai_resume_analyzer.dto.ChatResponse;
import com.resumeai.ai_resume_analyzer.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * POST /api/chat/message
     * Accepts a user question + conversation history, runs RAG, returns AI reply.
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }
}