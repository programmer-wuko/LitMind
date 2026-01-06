package com.litmind.model.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recommended_file_id")
    private Long recommendedFileId;

    @Column(name = "external_paper_id", length = 100)
    private String externalPaperId;

    @Column(name = "paper_title", length = 500)
    private String paperTitle;

    @Column(name = "paper_authors", columnDefinition = "TEXT")
    private String paperAuthors;

    @Column(name = "paper_source", length = 100)
    private String paperSource;

    @Column(name = "paper_url", length = 500)
    private String paperUrl;

    @Column(name = "recommendation_reason", columnDefinition = "TEXT")
    private String recommendationReason;

    @Column(name = "recommendation_score", precision = 5, scale = 2)
    private BigDecimal recommendationScore;

    @Column(length = 20)
    private String feedback;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

