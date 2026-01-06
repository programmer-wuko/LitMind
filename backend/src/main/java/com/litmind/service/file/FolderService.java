package com.litmind.service.file;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.entity.Folder;
import com.litmind.model.entity.User;
import com.litmind.repository.FolderRepository;
import com.litmind.repository.FileRepository;
import com.litmind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public List<Folder> getUserFolders(Long userId, Long parentId) {
        if (parentId == null) {
            // 当parentId为null时，返回该用户的所有文件夹（用于构建完整的树形结构）
            return folderRepository.findByUserId(userId);
        }
        return folderRepository.findByUserIdAndParentId(userId, parentId);
    }

    /**
     * 获取公共文件夹（文献管理 - 仅同部门可见）
     */
    public List<Folder> getPublicFolders(Long userId, Long parentId) {
        // 获取用户的部门ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        Long departmentId = user.getDepartmentId();
        
        if (departmentId == null) {
            // 如果用户没有部门，返回空列表
            return java.util.Collections.emptyList();
        }
        
        if (parentId == null) {
            return folderRepository.findByIsPublicTrueAndDepartmentId(departmentId);
        }
        return folderRepository.findByIsPublicTrueAndDepartmentIdAndParentId(departmentId, parentId);
    }

    /**
     * 获取用户的文件夹（我的文献 - 包括公共和私有）
     */
    public List<Folder> getMyFolders(Long userId, Long parentId) {
        if (parentId == null) {
            return folderRepository.findByUserId(userId);
        }
        return folderRepository.findByUserIdAndParentId(userId, parentId);
    }

    public Folder createFolder(Long userId, Long parentId, String name, Boolean isPublic) {
        // 如果指定了父文件夹，验证父文件夹是否存在且属于当前用户
        if (parentId != null) {
            folderRepository.findByIdAndUserId(parentId, userId)
                    .orElseThrow(() -> new BusinessException(404, "父文件夹不存在"));
        }

        // 检查同名文件夹是否存在
        if (folderRepository.existsByUserIdAndParentIdAndName(userId, parentId, name)) {
            throw new BusinessException(400, "该文件夹已存在");
        }

        // 获取用户的部门ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setParentId(parentId);
        folder.setName(name);
        folder.setPath(generatePath(userId, parentId, name));
        // 设置文件夹可见性：true=文献管理（同部门可见），false=我的文献（仅个人可见）
        folder.setIsPublic(isPublic != null ? isPublic : false);
        // 设置文件夹的部门ID
        folder.setDepartmentId(user.getDepartmentId());

        return folderRepository.save(folder);
    }

    public Folder updateFolder(Long userId, Long folderId, String newName) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件夹不存在"));

        // 检查新名称是否已存在
        if (folderRepository.existsByUserIdAndParentIdAndName(userId, folder.getParentId(), newName)) {
            throw new BusinessException(400, "该文件夹名称已存在");
        }

        folder.setName(newName);
        folder.setPath(generatePath(userId, folder.getParentId(), newName));

        return folderRepository.save(folder);
    }

    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件夹不存在"));

        // 检查是否有子文件夹
        List<Folder> children = folderRepository.findByUserIdAndParentId(userId, folderId);
        if (!children.isEmpty()) {
            throw new BusinessException(400, "文件夹不为空，无法删除。请先删除子文件夹");
        }

        // 检查是否有文件
        List<com.litmind.model.entity.File> files = fileRepository.findByUserIdAndFolderId(userId, folderId);
        if (!files.isEmpty()) {
            throw new BusinessException(400, "文件夹不为空，无法删除。请先删除文件夹中的文件");
        }

        folderRepository.delete(folder);
    }

    private String generatePath(Long userId, Long parentId, String name) {
        if (parentId == null) {
            return "/" + userId + "/" + name;
        }
        Folder parent = folderRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(404, "父文件夹不存在"));
        return parent.getPath() + "/" + name;
    }
}

