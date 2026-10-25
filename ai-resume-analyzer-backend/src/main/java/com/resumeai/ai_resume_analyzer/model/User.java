package com.resumeai.ai_resume_analyzer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    @Column
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Builder.Default
    @Column(nullable = false)
    private String role = "ROLE_USER";

    @Builder.Default
    @Column(nullable = false)
    private Boolean isGuest = false;

    @Column(unique = true)
    private String guestSessionToken;

    @Builder.Default
    @Column(nullable = false)
    private Integer uploadCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer chatMessageCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LinkedAccount> linkedAccounts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Resume> resumes = new ArrayList<>();
}
