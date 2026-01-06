package com.litmind.repository;

import com.litmind.model.entity.AiQaRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiQaRecordRepository extends JpaRepository<AiQaRecord, Long> {
    List<AiQaRecord> findByFileIdAndUserIdOrderByCreatedAtDesc(Long fileId, Long userId);
}

