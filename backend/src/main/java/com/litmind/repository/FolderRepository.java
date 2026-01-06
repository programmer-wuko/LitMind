package com.litmind.repository;

import com.litmind.model.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByUserIdAndParentId(Long userId, Long parentId);
    List<Folder> findByUserId(Long userId);
    Optional<Folder> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndParentIdAndName(Long userId, Long parentId, String name);
    
    // 查询公共文件夹（文献管理）
    List<Folder> findByIsPublicTrueAndParentId(Long parentId);
    List<Folder> findByIsPublicTrue();
    
    // 查询用户的文件夹（我的文献：包括公共和私有）
    List<Folder> findByUserIdAndParentIdAndIsPublic(Long userId, Long parentId, Boolean isPublic);
    
    // 按部门查询公共文件夹（文献管理）
    List<Folder> findByIsPublicTrueAndDepartmentId(Long departmentId);
    List<Folder> findByIsPublicTrueAndDepartmentIdAndParentId(Long departmentId, Long parentId);
}

