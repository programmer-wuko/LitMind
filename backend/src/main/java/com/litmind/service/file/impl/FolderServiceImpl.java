package com.litmind.service.file.impl;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.entity.Folder;
import com.litmind.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.litmind.service.file.FolderService;

@Service
@RequiredArgsConstructor
@Slf4j
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;

    @Override
    public List<Folder> getUserFolders(Long userId, Long parentId) {
        if (parentId != null) {
            return folderRepository.findByUserIdAndParentId(userId, parentId);
        } else {
            return folderRepository.findByUserId(userId);
        }
    }

    @Override
    public List<Folder> getPublicFolders(Long userId, Long parentId) {
        if (parentId != null) {
            return folderRepository.findByIsPublicTrueAndParentId(parentId);
        } else {
            return folderRepository.findByIsPublicTrue();
        }
    }

    @Override
    public List<Folder> getMyFolders(Long userId, Long parentId) {
        return getUserFolders(userId, parentId);
    }

    @Override
    @Transactional
    public Folder createFolder(Long userId, Long parentId, String name, Boolean isPublic) {
        if (parentId != null) {
            folderRepository.findById(parentId)
                    .orElseThrow(() -> new BusinessException(404, "父文件夹不存在"));
        }

        boolean exists = folderRepository.existsByUserIdAndParentIdAndName(userId, parentId, name);
        if (exists) {
            throw new BusinessException(400, "同目录下已存在同名文件夹");
        }

        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setName(name);
        folder.setParentId(parentId);
        folder.setIsPublic(isPublic != null ? isPublic : false);

        return folderRepository.save(folder);
    }

    @Override
    @Transactional
    public Folder updateFolder(Long userId, Long folderId, String newName) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件夹不存在"));

        if (newName != null && !newName.trim().isEmpty()) {
            boolean exists = folderRepository.existsByUserIdAndParentIdAndName(userId, folder.getParentId(), newName);
            if (exists) {
                throw new BusinessException(400, "同目录下已存在同名文件夹");
            }
            folder.setName(newName);
        }

        return folderRepository.save(folder);
    }

    @Override
    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件夹不存在"));

        List<Folder> subFolders = folderRepository.findByUserIdAndParentId(userId, folderId);
        if (!subFolders.isEmpty()) {
            throw new BusinessException(400, "文件夹下存在子文件夹，无法删除");
        }

        folderRepository.delete(folder);
    }
}
