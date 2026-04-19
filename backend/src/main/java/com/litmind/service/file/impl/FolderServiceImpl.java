package com.litmind.service.file.impl;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.dto.FolderDetailDTO;
import com.litmind.model.dto.FolderListDTO;
import com.litmind.model.entity.Folder;
import com.litmind.model.request.FolderCreateRequest;
import com.litmind.model.request.FolderUpdateRequest;
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
    public FolderDetailDTO getFolderById(Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BusinessException(404, "文件夹不存在"));
        return convertToDetailDTO(folder);
    }

    @Override
    public List<FolderListDTO> getFoldersByParentId(Long parentId) {
        List<Folder> folders = folderRepository.findByParentId(parentId);
        return folders.stream().map(this::convertToListDTO).toList();
    }

    @Override
    @Transactional
    public FolderDetailDTO createFolder(FolderCreateRequest request) {
        // 验证父文件夹是否存在
        if (request.getParentId() != null) {
            folderRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException(404, "父文件夹不存在"));
        }

        // 检查同名文件夹
        List<Folder> existingFolders = folderRepository.findByNameAndParentId(request.getName(), request.getParentId());
        if (!existingFolders.isEmpty()) {
            throw new BusinessException(400, "同目录下已存在同名文件夹");
        }

        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setParentId(request.getParentId());

        Folder savedFolder = folderRepository.save(folder);
        return convertToDetailDTO(savedFolder);
    }

    @Override
    @Transactional
    public FolderDetailDTO updateFolder(Long folderId, FolderUpdateRequest request) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BusinessException(404, "文件夹不存在"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            // 检查同名文件夹
            List<Folder> existingFolders = folderRepository.findByNameAndParentId(request.getName(), folder.getParentId());
            for (Folder existingFolder : existingFolders) {
                if (!existingFolder.getId().equals(folderId)) {
                    throw new BusinessException(400, "同目录下已存在同名文件夹");
                }
            }
            folder.setName(request.getName());
        }

        Folder updatedFolder = folderRepository.save(folder);
        return convertToDetailDTO(updatedFolder);
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BusinessException(404, "文件夹不存在"));

        // 检查是否有子文件夹
        List<Folder> subFolders = folderRepository.findByParentId(folderId);
        if (!subFolders.isEmpty()) {
            throw new BusinessException(400, "文件夹下存在子文件夹，无法删除");
        }

        folderRepository.delete(folder);
    }

    private FolderDetailDTO convertToDetailDTO(Folder folder) {
        FolderDetailDTO dto = new FolderDetailDTO();
        dto.setId(folder.getId());
        dto.setName(folder.getName());
        dto.setParentId(folder.getParentId());
        dto.setCreatedAt(folder.getCreatedAt());
        dto.setUpdatedAt(folder.getUpdatedAt());
        return dto;
    }

    private FolderListDTO convertToListDTO(Folder folder) {
        FolderListDTO dto = new FolderListDTO();
        dto.setId(folder.getId());
        dto.setName(folder.getName());
        dto.setParentId(folder.getParentId());
        dto.setCreatedAt(folder.getCreatedAt());
        return dto;
    }
}
