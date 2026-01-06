package com.litmind.model.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_behaviors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBehavior {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "behavior_type", nullable = false, length = 50)
    private String behaviorType;

    @Column(name = "behavior_data", columnDefinition = "JSON")
    private String behaviorData;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

