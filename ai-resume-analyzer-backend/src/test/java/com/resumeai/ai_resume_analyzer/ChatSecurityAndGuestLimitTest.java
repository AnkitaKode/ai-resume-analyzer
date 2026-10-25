package com.resumeai.ai_resume_analyzer;

import com.resumeai.ai_resume_analyzer.dto.ChatRequest;
import com.resumeai.ai_resume_analyzer.dto.ChatResponse;
import com.resumeai.ai_resume_analyzer.model.Resume;
import com.resumeai.ai_resume_analyzer.model.User;
import com.resumeai.ai_resume_analyzer.repository.ResumeRepository;
import com.resumeai.ai_resume_analyzer.repository.UserRepository;
import com.resumeai.ai_resume_analyzer.security.UserPrincipal;
import com.resumeai.ai_resume_analyzer.service.ChatService;
import com.resumeai.ai_resume_analyzer.service.EmbeddingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ChatSecurityAndGuestLimitTest {

    private final Map<String, User> userDb = new HashMap<>();
    private final Map<String, Resume> resumeDb = new HashMap<>();
    private final List<String> passedUserIds = new ArrayList<>();

    private UserRepository userRepository;
    private ResumeRepository resumeRepository;
    private EmbeddingService embeddingService;
    private ChatModel chatModel;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        userDb.clear();
        resumeDb.clear();
        passedUserIds.clear();

        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name)) {
                        return Optional.ofNullable(userDb.get(args[0]));
                    } else if ("save".equals(name)) {
                        User u = (User) args[0];
                        userDb.put(u.getId(), u);
                        return u;
                    }
                    return null;
                }
        );

        resumeRepository = (ResumeRepository) Proxy.newProxyInstance(
                ResumeRepository.class.getClassLoader(),
                new Class[]{ResumeRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name)) {
                        return Optional.ofNullable(resumeDb.get(args[0]));
                    }
                    return null;
                }
        );

        embeddingService = new EmbeddingService(null, null, null, null) {
            @Override
            public List<String> retrieveRelevantChunks(String resumeId, String userId, String query, int topK) {
                passedUserIds.add(userId);
                return List.of("Scoped resume excerpt for user " + userId);
            }
        };

        chatModel = new ChatModel() {
            @Override
            public org.springframework.ai.chat.model.ChatResponse call(Prompt prompt) {
                Generation gen = new Generation(new org.springframework.ai.chat.messages.AssistantMessage("Advice based on resume context."));
                return new org.springframework.ai.chat.model.ChatResponse(List.of(gen));
            }
        };

        chatService = new ChatService(
                chatModel,
                embeddingService,
                resumeRepository,
                userRepository
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testChat_GuestLimitEnforced() {
        User guest = User.builder()
                .id("guest-1")
                .isGuest(true)
                .chatMessageCount(5) // Limit is 5
                .build();
        userDb.put("guest-1", guest);
        authenticateUser(guest);

        Resume resume = Resume.builder().id("res-1").user(guest).build();
        resumeDb.put("res-1", resume);

        ChatRequest request = ChatRequest.builder()
                .resumeId("res-1")
                .message("How can I improve?")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> chatService.chat(request));

        assertTrue(ex.getMessage().contains("Guest chat limit reached"));
    }

    @Test
    void testChat_DifferentUserAccessDenied() {
        User user1 = User.builder().id("user-1").isGuest(false).chatMessageCount(0).build();
        User user2 = User.builder().id("user-2").isGuest(false).chatMessageCount(0).build();
        userDb.put("user-1", user1);
        userDb.put("user-2", user2);
        authenticateUser(user2);

        Resume resume1 = Resume.builder().id("res-1").user(user1).build();
        resumeDb.put("res-1", resume1);

        ChatRequest request = ChatRequest.builder()
                .resumeId("res-1")
                .message("Tell me about this resume")
                .build();

        assertThrows(AccessDeniedException.class, () -> chatService.chat(request));
    }

    @Test
    void testChat_ScopesChunkRetrievalToCurrentUser() {
        User user1 = User.builder().id("user-1").isGuest(false).chatMessageCount(0).build();
        userDb.put("user-1", user1);
        authenticateUser(user1);

        Resume resume1 = Resume.builder().id("res-1").user(user1).build();
        resumeDb.put("res-1", resume1);

        ChatRequest request = ChatRequest.builder()
                .resumeId("res-1")
                .message("What are my key skills?")
                .build();

        ChatResponse response = chatService.chat(request);

        assertNotNull(response);
        assertEquals("Advice based on resume context.", response.getMessage());
        assertEquals(1, passedUserIds.size());
        assertEquals("user-1", passedUserIds.get(0));
        assertEquals(1, user1.getChatMessageCount());
    }
}
