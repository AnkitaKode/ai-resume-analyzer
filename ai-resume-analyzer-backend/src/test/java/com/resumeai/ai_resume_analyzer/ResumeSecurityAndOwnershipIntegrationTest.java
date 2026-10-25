package com.resumeai.ai_resume_analyzer;

import com.resumeai.ai_resume_analyzer.dto.ResumeAnalysisResponse;
import com.resumeai.ai_resume_analyzer.model.Resume;
import com.resumeai.ai_resume_analyzer.model.User;
import com.resumeai.ai_resume_analyzer.repository.ResumeRepository;
import com.resumeai.ai_resume_analyzer.repository.UserRepository;
import com.resumeai.ai_resume_analyzer.security.UserPrincipal;
import com.resumeai.ai_resume_analyzer.service.AnalysisService;
import com.resumeai.ai_resume_analyzer.service.EmbeddingService;
import com.resumeai.ai_resume_analyzer.service.ResumeService;
import com.resumeai.ai_resume_analyzer.util.DocxExtractor;
import com.resumeai.ai_resume_analyzer.util.PdfExtractor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ResumeSecurityAndOwnershipIntegrationTest {

    private final Map<String, User> userDb = new HashMap<>();
    private final Map<String, Resume> resumeDb = new HashMap<>();

    private UserRepository userRepository;
    private ResumeRepository resumeRepository;
    private ResumeService resumeService;

    @BeforeEach
    void setUp() {
        userDb.clear();
        resumeDb.clear();

        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("findById".equals(name)) {
                        return Optional.ofNullable(userDb.get(args[0]));
                    } else if ("save".equals(name)) {
                        User u = (User) args[0];
                        if (u.getId() == null) u.setId(UUID.randomUUID().toString());
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
                    if ("save".equals(name)) {
                        Resume r = (Resume) args[0];
                        if (r.getId() == null) r.setId(UUID.randomUUID().toString());
                        resumeDb.put(r.getId(), r);
                        return r;
                    } else if ("findById".equals(name)) {
                        return Optional.ofNullable(resumeDb.get(args[0]));
                    } else if ("countByUserId".equals(name)) {
                        String uid = (String) args[0];
                        return resumeDb.values().stream()
                                .filter(r -> r.getUser() != null && uid.equals(r.getUser().getId()))
                                .count();
                    }
                    return null;
                }
        );

        PdfExtractor pdfExtractor = new PdfExtractor() {
            @Override
            public String extract(byte[] pdfBytes) {
                return "Mock extracted text from PDF resume";
            }
        };
        DocxExtractor docxExtractor = new DocxExtractor() {
            @Override
            public String extract(byte[] docxBytes) {
                return "Mock extracted text from DOCX resume";
            }
        };

        EmbeddingService embeddingService = new EmbeddingService(null, null, null, null) {
            @Override
            public void embedAndStore(Resume resume) {
                // No-op for test
            }

            @Override
            public List<String> retrieveRelevantChunks(String resumeId, String userId, String query, int topK) {
                return List.of("Chunk belonging to user resume");
            }
        };

        AnalysisService analysisService = new AnalysisService(null, null) {
            @Override
            public ResumeAnalysisResponse analyze(String resumeId, String resumeText) {
                return ResumeAnalysisResponse.builder()
                        .resumeId(resumeId)
                        .score(85)
                        .summary("Solid profile")
                        .build();
            }
        };

        resumeService = new ResumeService(
                resumeRepository,
                userRepository,
                pdfExtractor,
                docxExtractor,
                embeddingService,
                analysisService
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
    void testUpload_Unauthenticated_ThrowsAccessDenied() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes());

        assertThrows(AccessDeniedException.class, () -> resumeService.upload(file));
    }

    @Test
    void testUpload_AuthenticatedUser_SetsOwnership() {
        User user = User.builder().id("user-1").email("user1@example.com").isGuest(false).build();
        userDb.put("user-1", user);
        authenticateUser(user);

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "dummy content".getBytes());

        var response = resumeService.upload(file);

        assertNotNull(response);
        assertNotNull(response.getId());
        Resume saved = resumeDb.get(response.getId());
        assertNotNull(saved);
        assertEquals(user, saved.getUser());
    }

    @Test
    void testUpload_GuestLimitEnforced() {
        User guest = User.builder().id("guest-1").isGuest(true).build();
        userDb.put("guest-1", guest);
        authenticateUser(guest);

        // Pre-fill 2 resumes (guest limit is 2)
        Resume r1 = Resume.builder().id("r1").user(guest).build();
        Resume r2 = Resume.builder().id("r2").user(guest).build();
        resumeDb.put("r1", r1);
        resumeDb.put("r2", r2);

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "dummy content".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> resumeService.upload(file));

        assertTrue(ex.getMessage().contains("Guest upload limit reached"));
    }

    @Test
    void testGetById_OwnerAccessGranted() {
        User user1 = User.builder().id("user-1").build();
        userDb.put("user-1", user1);
        authenticateUser(user1);

        Resume resume = Resume.builder()
                .id("res-1")
                .fileName("test.pdf")
                .rawText("content")
                .user(user1)
                .build();
        resumeDb.put("res-1", resume);

        Resume result = resumeService.getById("res-1");
        assertNotNull(result);
        assertEquals("res-1", result.getId());
    }

    @Test
    void testGetById_DifferentUserAccessDenied() {
        User user1 = User.builder().id("user-1").build();
        User user2 = User.builder().id("user-2").build();
        userDb.put("user-1", user1);
        userDb.put("user-2", user2);
        authenticateUser(user2); // Authenticated as User 2

        Resume resume = Resume.builder()
                .id("res-1")
                .fileName("test.pdf")
                .rawText("content")
                .user(user1) // Owned by User 1
                .build();
        resumeDb.put("res-1", resume);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> resumeService.getById("res-1"));

        assertTrue(ex.getMessage().contains("permission to access"));
    }

    @Test
    void testAnalyze_DifferentUserAccessDenied() {
        User user1 = User.builder().id("user-1").build();
        User user2 = User.builder().id("user-2").build();
        userDb.put("user-1", user1);
        userDb.put("user-2", user2);
        authenticateUser(user2);

        Resume resume = Resume.builder()
                .id("res-1")
                .user(user1)
                .build();
        resumeDb.put("res-1", resume);

        assertThrows(AccessDeniedException.class, () -> resumeService.analyze("res-1"));
    }
}
