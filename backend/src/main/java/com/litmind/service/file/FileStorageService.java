package com.litmind.service.file;

import com.litmind.config.MinIOConfig;
import com.litmind.common.exception.BusinessException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@Slf4j
public class FileStorageService {

    private final Optional<MinioClient> minioClient;
    private final Optional<MinIOConfig> minioConfig;
    
    @Value("${file.storage.type:minio}")
    private String storageType;
    
    @Value("${file.storage.local.path:./uploads}")
    private String localStoragePath;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public FileStorageService(Optional<MinioClient> minioClient, Optional<MinIOConfig> minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    public String uploadFile(MultipartFile file, String objectName) {
        try {
            if ("local".equals(storageType)) {
                return uploadToLocal(file, objectName);
            } else {
                return uploadToMinIO(file, objectName);
            }
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }
    
    private String uploadToLocal(MultipartFile file, String objectName) throws IOException {
        Path uploadPath = Paths.get(localStoragePath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(objectName);
        // 确保父目录存在（例如：uploads/1/）
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        Files.copy(file.getInputStream(), filePath);
        return objectName;
    }
    
    private String uploadToMinIO(MultipartFile file, String objectName) throws Exception {
        if (!minioClient.isPresent() || !minioConfig.isPresent()) {
            throw new BusinessException(500, "MinIO客户端未配置");
        }
        // 确保存储桶存在
        ensureBucketExists();

        // 上传文件
        minioClient.get().putObject(
                PutObjectArgs.builder()
                        .bucket(minioConfig.get().getBucket())
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        return objectName;
    }

    public InputStream downloadFile(String objectName) {
        try {
            if ("local".equals(storageType)) {
                Path filePath = Paths.get(localStoragePath, objectName);
                return new FileInputStream(filePath.toFile());
            } else {
                if (!minioClient.isPresent() || !minioConfig.isPresent()) {
                    throw new BusinessException(500, "MinIO客户端未配置");
                }
                return minioClient.get().getObject(
                        GetObjectArgs.builder()
                                .bucket(minioConfig.get().getBucket())
                                .object(objectName)
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "文件下载失败: " + e.getMessage());
        }
    }

    public void deleteFile(String objectName) {
        try {
            if ("local".equals(storageType)) {
                Path filePath = Paths.get(localStoragePath, objectName);
                Files.deleteIfExists(filePath);
            } else {
                if (!minioClient.isPresent() || !minioConfig.isPresent()) {
                    throw new BusinessException(500, "MinIO客户端未配置");
                }
                minioClient.get().removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(minioConfig.get().getBucket())
                                .object(objectName)
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "文件删除失败: " + e.getMessage());
        }
    }

    private void ensureBucketExists() {
        if ("local".equals(storageType) || !minioClient.isPresent() || !minioConfig.isPresent()) {
            return; // 本地存储不需要bucket
        }
        try {
            boolean found = minioClient.get().bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(minioConfig.get().getBucket())
                            .build()
            );
            if (!found) {
                minioClient.get().makeBucket(
                        io.minio.MakeBucketArgs.builder()
                                .bucket(minioConfig.get().getBucket())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("检查存储桶失败: {}", e.getMessage(), e);
        }
    }
}

