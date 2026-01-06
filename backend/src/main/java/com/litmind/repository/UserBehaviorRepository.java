package com.litmind.repository;

import com.litmind.model.entity.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    List<UserBehavior> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<UserBehavior> findByFileId(Long fileId);
}

