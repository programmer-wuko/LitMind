package com.litmind.service.file.impl;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.entity.File;
import com.litmind.repository.FileRepository;
import com.litmind.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import com.litmind.service.file.FileService;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;

    @Override
    public List<File> getUserFiles(Long userId, Long folderId) {
        if (folderId != null) {
            return fileRepository.findByUserIdAndFolderId(userId, folderId);
        } else {
            return fileRepository.findByUserId(userId);
        }
    }

    @Override
    public List<File> getPublicFiles(Long userId, Long folderId) {
        if (folderId != null) {
            return fileRepository.findByIsPublicTrueAndFolderId(folderId);
        } else {
            return fileRepository.findByIsPublicTrue();
        }
    }

    @Override
    public List<File> getMyFiles(Long userId, Long folderId) {
        if (folderId != null) {
            return fileRepository.findByUserIdAndFolderId(userId, folderId);
        } else {
            return fileRepository.findByUserId(userId);
        }
    }

    @Override
    @Transactional
    public File uploadFile(Long userId, Long folderId, MultipartFile multipartFile, Boolean isPublic) {
        try {
            // 生成唯一文件名
            String originalFilename = multipartFile.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String objectName = "files/" + userId + "/" + UUID.randomUUID() + fileExtension;

            // 上传文件
            String filePath = fileStorageService.uploadFile(multipartFile, objectName);

            // 保存文件信息
            File file = new File();
            file.setUserId(userId);
            file.setName(originalFilename != null ? originalFilename : "未命名文件");
            file.setFolderId(folderId);
            file.setFilePath(filePath);
            file.setFileSize(multipartFile.getSize());
            file.setFileType(multipartFile.getContentType() != null ? multipartFile.getContentType() : "application/octet-stream");
            file.setIsPublic(isPublic != null ? isPublic : false);

            return fileRepository.save(file);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public File getFile(Long userId, Long fileId) {
        return fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));
    }

    @Override
    public InputStream downloadFile(String filePath) {
        return fileStorageService.downloadFile(filePath);
    }

    @Override
    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        File file = fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        try {
            // 删除存储中的文件
            fileStorageService.deleteFile(file.getFilePath());
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
        }

        fileRepository.delete(file);
    }

    @Override
    @Transactional
    public File updateFile(Long userId, Long fileId, String newName, Long newFolderId) {
        File file = fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        if (newName != null && !newName.trim().isEmpty()) {
            file.setName(newName);
        }
        if (newFolderId != null) {
            file.setFolderId(newFolderId);
        }

        return fileRepository.save(file);
    }
}
