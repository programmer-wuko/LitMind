package com.litmind.model.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pdf_analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false, unique = true)
    private Long fileId;

    @Column(name = "research_background", columnDefinition = "TEXT")
    private String researchBackground;

    @Column(name = "core_content", columnDefinition = "TEXT")
    private String coreContent;

    @Column(name = "experiment_results", columnDefinition = "TEXT")
    private String experimentResults;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(name = "analysis_status", length = 20)
    private String analysisStatus = "PENDING";

    @Column(name = "analysis_model", length = 50)
    private String analysisModel;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

