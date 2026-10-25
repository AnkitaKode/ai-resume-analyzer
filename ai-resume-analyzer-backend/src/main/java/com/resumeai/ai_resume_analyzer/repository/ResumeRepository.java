package com.resumeai.ai_resume_analyzer.repository;

import com.resumeai.ai_resume_analyzer.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, String> {
    List<Resume> findByStatusOrderByCreatedAtDesc(String status);
    List<Resume> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Resume> findByIdAndUserId(String id, String userId);
    long countByUserId(String userId);
}
