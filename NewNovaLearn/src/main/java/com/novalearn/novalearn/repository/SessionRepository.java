package com.novalearn.novalearn.repository;

import com.novalearn.novalearn.model.Session;
import com.novalearn.novalearn.model.Session.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    @EntityGraph(attributePaths = {"host"})
    List<Session> findByStatusInOrderByScheduledAtDesc(List<SessionStatus> statuses);
    
    @EntityGraph(attributePaths = {"host"})
    List<Session> findByHostNicknameOrderByCreatedAtDesc(String nickname);
    
    @EntityGraph(attributePaths = {"host"})
    Optional<Session> findByRoomCode(String roomCode);
}
