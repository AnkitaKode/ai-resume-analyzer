package com.resumeai.ai_resume_analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "resume_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer chunkIndex;

    /**
     * Stores the embedding as a JSON array string.
     * For production, use pgvector extension with proper vector column type.
     */
    @Column(columnDefinition = "TEXT")
    private String embeddingJson;

    @Transient
    private List<Float> embedding;
}