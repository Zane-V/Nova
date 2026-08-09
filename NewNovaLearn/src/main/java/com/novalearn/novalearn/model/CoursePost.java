package com.novalearn.novalearn.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a topic/update that a lecturer publishes under a course.
 * The lecturer can post either video content or text content.
 */
@Entity
@Table(name = "course_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String topic;

    /** "VIDEO" or "TEXT" */
    @Column(nullable = false)
    private String contentType;

    /** Text body when contentType = TEXT */
    @Column(columnDefinition = "TEXT")
    private String textContent;

    /** Relative path to the uploaded video when contentType = VIDEO */
    private String videoPath;

    /** Path to the uploaded document file when contentType = TEXT */
    private String documentPath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
