package com.novalearn.novalearn.service;

import com.novalearn.novalearn.model.Session;
import com.novalearn.novalearn.model.Session.SessionStatus;
import com.novalearn.novalearn.model.User;
import com.novalearn.novalearn.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Creates a new live session hosted by the given user.
     */
    public Session createSession(String title, String subject, LocalDateTime scheduledAt, User host) {
        String roomCode = generateRoomCode();

        Session session = Session.builder()
                .title(title)
                .subject(subject)
                .roomCode(roomCode)
                .host(host)
                .status(SessionStatus.ACTIVE)
                .scheduledAt(scheduledAt)
                .build();

        return sessionRepository.save(session);
    }

    /**
     * Returns all sessions that are SCHEDULED or ACTIVE, newest first.
     */
    public List<Session> getActiveSessions() {
        return sessionRepository.findByStatusInOrderByScheduledAtDesc(
                List.of(SessionStatus.ACTIVE, SessionStatus.SCHEDULED));
    }

    /**
     * Returns all sessions created by a specific lecturer (by nickname).
     */
    public List<Session> getSessionsByHost(String nickname) {
        return sessionRepository.findByHostNicknameOrderByCreatedAtDesc(nickname);
    }

    /**
     * Finds a session by its room code.
     */
    public Optional<Session> findByRoomCode(String roomCode) {
        return sessionRepository.findByRoomCode(roomCode);
    }

    /**
     * Finds a session by its ID.
     */
    public Optional<Session> findById(Long id) {
        return sessionRepository.findById(id);
    }

    /**
     * Marks a session as ENDED.
     */
    @Transactional
    public boolean endSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .map(session -> {
                    session.setStatus(SessionStatus.ENDED);
                    sessionRepository.saveAndFlush(session);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Generates a unique 8-character alphanumeric room code.
     */
    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
            code = sb.toString();
        } while (sessionRepository.findByRoomCode(code).isPresent());
        return code;
    }
}
