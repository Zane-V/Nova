package com.novalearn.novalearn.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "reset_token")
    private String resetCode;

    @Column(name = "reset_code_expiry")
    private LocalDateTime resetCodeExpiry;

    @Builder.Default
    @Column(name = "password_reset_attempts")
    private int passwordResetAttempts = 0;

    @Column(name = "password_reset_locked_until")
    private LocalDateTime passwordResetLockedUntil;

    @Builder.Default
    @Column(name = "login_attempts")
    private int loginAttempts = 0;

    @Builder.Default
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil = null;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
