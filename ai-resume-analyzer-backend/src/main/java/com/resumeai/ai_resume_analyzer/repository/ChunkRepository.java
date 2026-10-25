package com.resumeai.ai_resume_analyzer.repository;

import com.resumeai.ai_resume_analyzer.model.ResumeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChunkRepository extends JpaRepository<ResumeChunk, String> {

    List<ResumeChunk> findByResumeIdOrderByChunkIndex(String resumeId);

    @Query("SELECT c FROM ResumeChunk c WHERE c.resume.id = :resumeId")
    List<ResumeChunk> findAllByResumeId(@Param("resumeId") String resumeId);

    @Query("SELECT c FROM ResumeChunk c WHERE c.resume.id = :resumeId AND c.resume.user.id = :userId")
    List<ResumeChunk> findAllByResumeIdAndUserId(@Param("resumeId") String resumeId, @Param("userId") String userId);

    @Transactional
    void deleteByResumeId(String resumeId);
}
