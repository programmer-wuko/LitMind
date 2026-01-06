package com.litmind.repository;

import com.litmind.model.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByUserIdAndFolderId(Long userId, Long folderId);
    List<File> findByUserId(Long userId);
    Optional<File> findByIdAndUserId(Long id, Long userId);
    List<File> findByUserIdAndFileType(Long userId, String fileType);
    
    // 查询公共文件（文献管理）
    List<File> findByIsPublicTrueAndFolderId(Long folderId);
    List<File> findByIsPublicTrue();
    
    // 查询用户的文件（我的文献：包括公共和私有）
    List<File> findByUserIdAndFolderIdAndIsPublic(Long userId, Long folderId, Boolean isPublic);
    
    // 按部门查询公共文件（文献管理）
    List<File> findByIsPublicTrueAndDepartmentId(Long departmentId);
    List<File> findByIsPublicTrueAndDepartmentIdAndFolderId(Long departmentId, Long folderId);
}

