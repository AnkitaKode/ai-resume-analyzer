package com.resumeai.ai_resume_analyzer.repository;

import com.resumeai.ai_resume_analyzer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByGuestSessionToken(String guestSessionToken);

    boolean existsByEmailIgnoreCase(String email);
}
