package com.resumeai.ai_resume_analyzer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "linked_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_provider_subject", columnNames = {"provider", "providerSubject"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkedAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthProvider provider;

    @Column(nullable = false)
    private String providerSubject;

    @Column
    private String providerEmail;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
