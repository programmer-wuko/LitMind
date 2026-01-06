package com.litmind.controller.file;

import com.litmind.common.response.ApiResponse;
import com.litmind.common.util.SecurityUtil;
import com.litmind.model.entity.File;
import com.litmind.service.file.FileService;
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
import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ApiResponse<List<File>> getFiles(
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false, defaultValue = "false") Boolean isPublic,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        List<File> files;
        if (Boolean.TRUE.equals(isPublic)) {
            // 文献管理：获取公共文件（仅同部门可见）
            files = fileService.getPublicFiles(userId, folderId);
        } else {
            // 我的文献：获取用户的文件（包括公共和私有）
            files = fileService.getMyFiles(userId, folderId);
        }
        return ApiResponse.success(files);
    }

    @PostMapping("/upload")
    public ApiResponse<File> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false, defaultValue = "false") Boolean isPublic,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        File uploadedFile = fileService.uploadFile(userId, folderId, file, isPublic);
        return ApiResponse.success("文件上传成功", uploadedFile);
    }

    @GetMapping("/{id}")
    public ApiResponse<File> getFile(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        File file = fileService.getFile(userId, id);
        return ApiResponse.success(file);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        File file = fileService.getFile(userId, id);
        
        try {
            InputStream inputStream = fileService.downloadFile(file.getFilePath());
            InputStreamResource resource = new InputStreamResource(inputStream);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.parseMediaType(file.getMimeType() != null ? file.getMimeType() : "application/pdf"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<File> updateFile(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        String newName = request.get("name") != null ? request.get("name").toString() : null;
        Long newFolderId = request.get("folderId") != null
                ? Long.valueOf(request.get("folderId").toString())
                : null;

        File file = fileService.updateFile(userId, id, newName, newFolderId);
        return ApiResponse.success("文件更新成功", file);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFile(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        fileService.deleteFile(userId, id);
        return ApiResponse.success("文件删除成功", null);
    }

    private Long getUserId(Authentication authentication) {
        Long userId = securityUtil.getUserId(authentication);
        if (userId == null) {
            throw new RuntimeException("无法获取用户ID");
        }
        return userId;
    }
}

