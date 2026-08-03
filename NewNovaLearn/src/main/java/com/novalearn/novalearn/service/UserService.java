package com.novalearn.novalearn.service;

import com.novalearn.novalearn.model.Role;
import com.novalearn.novalearn.model.User;
import com.novalearn.novalearn.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String fullName, String nickname, String email, String password, Role role) {
        if (fullName == null || fullName.trim().isBlank()) {
            throw new IllegalArgumentException("Full Name is required.");
        }
        if (nickname == null || nickname.trim().isBlank()) {
            throw new IllegalArgumentException("Nickname is required.");
        }
        if (email == null || email.trim().isBlank()) {
            throw new IllegalArgumentException("Email Address is required.");
        }

        // 1. Uniqueness Checks
        if (userRepository.existsByEmail(email.trim())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }
        if (userRepository.existsByFullName(fullName.trim())) {
            throw new IllegalArgumentException("An account with this full name already exists.");
        }
        if (userRepository.existsByNickname(nickname.trim())) {
            throw new IllegalArgumentException("This nickname is already taken by another user.");
        }

        // 2. Password Rules
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one letter and one number.");
        }

        User user = User.builder()
                .fullName(fullName.trim())
                .nickname(nickname.trim())
                .email(email.trim())
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void incrementLoginAttempts(User user) {
        user.setLoginAttempts(user.getLoginAttempts() + 1);
        if (user.getLoginAttempts() >= 5) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
        }
        userRepository.save(user);
    }

    public void resetLoginAttempts(User user) {
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    public boolean isAccountLocked(User user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
    }
    
    public Optional<User> findByResetCode(String resetCode) {
        return userRepository.findByResetCode(resetCode);
    }
    
    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }
    
    public void save(User user) {
        userRepository.save(user);
    }
}
