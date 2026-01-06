package com.litmind.controller.file;

import com.litmind.common.response.ApiResponse;
import com.litmind.common.util.SecurityUtil;
import com.litmind.model.entity.Attachment;
import com.litmind.service.file.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ApiResponse<List<Attachment>> getAttachments(
            @RequestParam Long fileId,
            Authentication authentication) {
        // 验证用户权限（通过fileId验证文件所有权）
        getUserId(authentication);
        List<Attachment> attachments = attachmentService.getAttachmentsByFileId(fileId);
        return ApiResponse.success(attachments);
    }

    @PostMapping("/upload")
    public ApiResponse<Attachment> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileId") Long fileId,
            Authentication authentication) {
        getUserId(authentication);
        Attachment attachment = attachmentService.uploadAttachment(fileId, file);
        return ApiResponse.success("附件上传成功", attachment);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(
            @PathVariable Long id,
            Authentication authentication) {
        getUserId(authentication);
        
        try {
            // 获取附件信息
            Attachment attachment = attachmentService.getAttachmentById(id);
            InputStream inputStream = attachmentService.downloadAttachment(id);
            InputStreamResource resource = new InputStreamResource(inputStream);
            
            // 根据文件类型设置Content-Type
            String contentType = attachment.getFileType() != null 
                    ? attachment.getFileType() 
                    : "application/octet-stream";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + attachment.getName() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("附件下载失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAttachment(
            @PathVariable Long id,
            Authentication authentication) {
        getUserId(authentication);
        attachmentService.deleteAttachment(id);
        return ApiResponse.success("附件删除成功", null);
    }

    private Long getUserId(Authentication authentication) {
        Long userId = securityUtil.getUserId(authentication);
        if (userId == null) {
            throw new RuntimeException("无法获取用户ID");
        }
        return userId;
    }
}

