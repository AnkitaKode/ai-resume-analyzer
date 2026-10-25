package com.resumeai.ai_resume_analyzer;

import com.resumeai.ai_resume_analyzer.config.TestAiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestAiConfig.class)
class AiResumeAnalyzerApplicationTests {

    @Test
    void contextLoads() {
    }
}
