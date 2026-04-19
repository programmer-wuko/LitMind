package com.litmind.service.file;

import com.litmind.model.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileService {

    List<File> getUserFiles(Long userId, Long folderId);

    List<File> getPublicFiles(Long userId, Long folderId);

    List<File> getMyFiles(Long userId, Long folderId);

    File uploadFile(Long userId, Long folderId, MultipartFile multipartFile, Boolean isPublic);

    File getFile(Long userId, Long fileId);

    InputStream downloadFile(String filePath);

    void deleteFile(Long userId, Long fileId);

    File updateFile(Long userId, Long fileId, String newName, Long newFolderId);
}
