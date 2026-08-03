package com.novalearn.novalearn.repository;

import com.novalearn.novalearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByFullName(String fullName);
    boolean existsByNickname(String nickname);
    Optional<User> findByResetCode(String resetCode);
    Optional<User> findByNickname(String nickname);
}
