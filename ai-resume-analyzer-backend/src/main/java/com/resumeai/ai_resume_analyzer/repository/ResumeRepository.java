package com.resumeai.ai_resume_analyzer.repository;



import com.resumeai.ai_resume_analyzer.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, String> {
    List<Resume> findByStatusOrderByCreatedAtDesc(String status);
}
