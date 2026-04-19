package com.litmind.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

    String uploadFile(MultipartFile file, String objectName);

    InputStream downloadFile(String objectName);

    void deleteFile(String objectName);
}
