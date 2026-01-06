package com.litmind.repository;

import com.litmind.model.entity.PdfAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PdfAnalysisRepository extends JpaRepository<PdfAnalysis, Long> {
    Optional<PdfAnalysis> findByFileId(Long fileId);
}

