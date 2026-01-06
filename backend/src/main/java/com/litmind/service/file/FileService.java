package com.litmind.service.file;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.entity.File;
import com.litmind.model.entity.User;
import com.litmind.model.entity.Folder;
import com.litmind.repository.FileRepository;
import com.litmind.repository.FolderRepository;
import com.litmind.repository.UserRepository;
import com.litmind.service.file.AttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final Optional<KafkaTemplate<String, String>> kafkaTemplate;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final Optional<com.litmind.service.recommend.RecommendationService> recommendationService;
    private final AttachmentService attachmentService;
    
    @Autowired
    public FileService(
            FileRepository fileRepository,
            FileStorageService fileStorageService,
            Optional<KafkaTemplate<String, String>> kafkaTemplate,
            UserRepository userRepository,
            FolderRepository folderRepository,
            Optional<com.litmind.service.recommend.RecommendationService> recommendationService,
            AttachmentService attachmentService) {
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.recommendationService = recommendationService;
        this.attachmentService = attachmentService;
    }

    public List<File> getUserFiles(Long userId, Long folderId) {
        if (folderId == null) {
            return fileRepository.findByUserIdAndFolderId(userId, null);
        }
        return fileRepository.findByUserIdAndFolderId(userId, folderId);
    }

    /**
     * 获取公共文件（文献管理 - 仅同部门可见）
     */
    public List<File> getPublicFiles(Long userId, Long folderId) {
        // 获取用户的部门ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        Long departmentId = user.getDepartmentId();
        
        if (departmentId == null) {
            // 如果用户没有部门，返回空列表
            return java.util.Collections.emptyList();
        }
        
        if (folderId == null) {
            return fileRepository.findByIsPublicTrueAndDepartmentId(departmentId);
        }
        return fileRepository.findByIsPublicTrueAndDepartmentIdAndFolderId(departmentId, folderId);
    }

    /**
     * 获取用户的文件（我的文献 - 包括公共和私有）
     */
    public List<File> getMyFiles(Long userId, Long folderId) {
        if (folderId == null) {
            return fileRepository.findByUserId(userId);
        }
        return fileRepository.findByUserIdAndFolderId(userId, folderId);
    }

    @Transactional
    public File uploadFile(Long userId, Long folderId, MultipartFile multipartFile, Boolean isPublic) {
        try {
            // 生成唯一文件名
            String originalFilename = multipartFile.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String objectName = userId + "/" + UUID.randomUUID() + fileExtension;

            // 上传到MinIO
            String filePath = fileStorageService.uploadFile(multipartFile, objectName);

            // 获取用户的部门ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(404, "用户不存在"));
            
            // 保存文件记录
            File file = new File();
            file.setUserId(userId);
            file.setFolderId(folderId);
            file.setName(originalFilename != null ? originalFilename : "未命名文件");
            file.setOriginalName(originalFilename != null ? originalFilename : "未命名文件");
            file.setFilePath(filePath);
            file.setFileSize(multipartFile.getSize());
            file.setFileType(multipartFile.getContentType());
            file.setMimeType(multipartFile.getContentType());
            file.setUploadStatus("COMPLETED");
            // 设置文件可见性：true=文献管理（同部门可见），false=我的文献（仅个人可见）
            file.setIsPublic(isPublic != null ? isPublic : false);
            // 设置文件的部门ID
            file.setDepartmentId(user.getDepartmentId());

            file = fileRepository.save(file);
            
            // 自动将文件添加为附件
            attachmentService.addFileAttachment(file);
            
            final Long fileId = file.getId(); // 提取为final变量供lambda使用

            // 记录用户上传文件的行为
            recommendationService.ifPresent(service -> {
                try {
                    service.recordUserBehavior(userId, fileId, "UPLOAD", 
                        "{\"action\":\"upload_file\",\"fileName\":\"" + originalFilename + "\",\"fileType\":\"" + 
                        multipartFile.getContentType() + "\"}");
                } catch (Exception e) {
                    log.warn("记录用户上传行为失败: {}", e.getMessage());
                }
            });

            // 如果是PDF文件，发送到Kafka进行异步分析（如果Kafka可用）
            if ("application/pdf".equals(multipartFile.getContentType())) {
                kafkaTemplate.ifPresent(template -> {
                    try {
                        template.send("pdf-analysis", String.valueOf(fileId));
                        log.info("PDF文件已发送到分析队列: fileId={}", fileId);
                    } catch (Exception e) {
                        log.warn("Kafka不可用，跳过异步分析: {}", e.getMessage());
                    }
                });
            }
            
            return file;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }

    public File getFile(Long userId, Long fileId) {
        return fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));
    }

    public InputStream downloadFile(String filePath) {
        return fileStorageService.downloadFile(filePath);
    }

    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        File file = fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        // 删除MinIO中的文件
        try {
            fileStorageService.deleteFile(file.getFilePath());
        } catch (Exception e) {
            log.error("删除MinIO文件失败: {}", e.getMessage(), e);
        }

        // 删除数据库记录
        fileRepository.delete(file);
    }

    @Transactional
    public File updateFile(Long userId, Long fileId, String newName, Long newFolderId) {
        File file = fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        // 如果移动文件到新文件夹，验证目标文件夹是否存在且属于同一部门
        if (newFolderId != null && !newFolderId.equals(file.getFolderId())) {
            // 获取用户信息以获取部门ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(404, "用户不存在"));
            
            // 验证目标文件夹是否存在
            Folder targetFolder = folderRepository.findById(newFolderId)
                    .orElseThrow(() -> new BusinessException(404, "目标文件夹不存在"));
            
            // 验证目标文件夹是否属于同一部门
            if (file.getDepartmentId() != null && !file.getDepartmentId().equals(targetFolder.getDepartmentId())) {
                throw new BusinessException(400, "不能将文件移动到不同部门的文件夹");
            }
            
            // 验证目标文件夹是否属于当前用户（对于私有文件夹）或同部门（对于公共文件夹）
            if (targetFolder.getIsPublic() != null && !targetFolder.getIsPublic()) {
                // 私有文件夹必须属于当前用户
                if (!targetFolder.getUserId().equals(userId)) {
                    throw new BusinessException(403, "无权访问该文件夹");
                }
            } else {
                // 公共文件夹必须属于同一部门
                if (user.getDepartmentId() != null && targetFolder.getDepartmentId() != null 
                        && !user.getDepartmentId().equals(targetFolder.getDepartmentId())) {
                    throw new BusinessException(400, "不能将文件移动到不同部门的文件夹");
                }
            }
            
            file.setFolderId(newFolderId);
        } else if (newFolderId == null && file.getFolderId() != null) {
            // 移动到根目录
            file.setFolderId(null);
        }

        if (newName != null && !newName.isEmpty()) {
            file.setName(newName);
        }

        return fileRepository.save(file);
    }
}

