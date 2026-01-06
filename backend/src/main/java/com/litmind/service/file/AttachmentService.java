package com.litmind.service.file;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.entity.Attachment;
import com.litmind.model.entity.File;
import com.litmind.repository.AttachmentRepository;
import com.litmind.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;

    public List<Attachment> getAttachmentsByFileId(Long fileId) {
        // 验证文件是否存在
        fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));
        return attachmentRepository.findByFileId(fileId);
    }

    public Attachment getAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(404, "附件不存在"));
    }

    @Transactional
    public Attachment uploadAttachment(Long fileId, MultipartFile multipartFile) {
        // 验证文件是否存在
        fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        try {
            // 生成唯一文件名
            String originalFilename = multipartFile.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String objectName = "attachments/" + fileId + "/" + UUID.randomUUID() + fileExtension;

            // 上传到存储服务
            String filePath = fileStorageService.uploadFile(multipartFile, objectName);

            // 保存附件记录
            Attachment attachment = new Attachment();
            attachment.setFileId(fileId);
            attachment.setName(originalFilename != null ? originalFilename : "未命名附件");
            attachment.setFilePath(filePath);
            attachment.setFileSize(multipartFile.getSize());
            attachment.setFileType(multipartFile.getContentType() != null 
                    ? multipartFile.getContentType() 
                    : "application/octet-stream");

            return attachmentRepository.save(attachment);
        } catch (Exception e) {
            log.error("附件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "附件上传失败: " + e.getMessage());
        }
    }

    public InputStream downloadAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(404, "附件不存在"));
        
        return fileStorageService.downloadFile(attachment.getFilePath());
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException(404, "附件不存在"));

        // 删除存储中的文件
        try {
            fileStorageService.deleteFile(attachment.getFilePath());
        } catch (Exception e) {
            log.error("删除附件文件失败: {}", e.getMessage(), e);
        }

        // 删除数据库记录
        attachmentRepository.delete(attachment);
    }

    /**
     * 将上传的文件自动添加为附件
     * @param file 上传的文件对象
     */
    @Transactional
    public void addFileAttachment(File file) {
        try {
            // 检查文件是否存在
            if (file == null || file.getId() == null) {
                throw new BusinessException(400, "文件信息不完整");
            }

            // 保存附件记录
            Attachment attachment = new Attachment();
            attachment.setFileId(file.getId());
            attachment.setName(file.getName());
            attachment.setFilePath(file.getFilePath());
            attachment.setFileSize(file.getFileSize());
            attachment.setFileType(file.getFileType() != null ? file.getFileType() : "application/octet-stream");

            attachmentRepository.save(attachment);
            log.info("文件已自动添加为附件: fileId={}, attachmentName={}", file.getId(), file.getName());
        } catch (Exception e) {
            log.error("自动添加附件失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "自动添加附件失败: " + e.getMessage());
        }
    }
}

