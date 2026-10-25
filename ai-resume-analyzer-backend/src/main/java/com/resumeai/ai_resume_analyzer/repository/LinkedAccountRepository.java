package com.resumeai.ai_resume_analyzer.repository;

import com.resumeai.ai_resume_analyzer.model.AuthProvider;
import com.resumeai.ai_resume_analyzer.model.LinkedAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinkedAccountRepository extends JpaRepository<LinkedAccount, String> {

    Optional<LinkedAccount> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    Optional<LinkedAccount> findByUserIdAndProvider(String userId, AuthProvider provider);

    List<LinkedAccount> findByUserId(String userId);
}
