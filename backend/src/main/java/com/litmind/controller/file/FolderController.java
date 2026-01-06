package com.litmind.controller.file;

import com.litmind.common.response.ApiResponse;
import com.litmind.common.util.SecurityUtil;
import com.litmind.model.entity.Folder;
import com.litmind.service.file.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ApiResponse<List<Folder>> getFolders(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false, defaultValue = "false") Boolean isPublic,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        List<Folder> folders;
        if (Boolean.TRUE.equals(isPublic)) {
            // 文献管理：获取公共文件夹（仅同部门可见）
            folders = folderService.getPublicFolders(userId, parentId);
        } else {
            // 我的文献：获取用户的文件夹（包括公共和私有）
            folders = folderService.getMyFolders(userId, parentId);
        }
        return ApiResponse.success(folders);
    }

    @PostMapping
    public ApiResponse<Folder> createFolder(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        Long parentId = request.get("parentId") != null
                ? Long.valueOf(request.get("parentId").toString())
                : null;
        String name = request.get("name").toString();
        Boolean isPublic = request.get("isPublic") != null
                ? Boolean.valueOf(request.get("isPublic").toString())
                : false;

        Folder folder = folderService.createFolder(userId, parentId, name, isPublic);
        return ApiResponse.success("文件夹创建成功", folder);
    }

    @PutMapping("/{id}")
    public ApiResponse<Folder> updateFolder(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        String newName = request.get("name");

        Folder folder = folderService.updateFolder(userId, id, newName);
        return ApiResponse.success("文件夹更新成功", folder);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFolder(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        folderService.deleteFolder(userId, id);
        return ApiResponse.success("文件夹删除成功", null);
    }

    private Long getUserId(Authentication authentication) {
        Long userId = securityUtil.getUserId(authentication);
        if (userId == null) {
            throw new RuntimeException("无法获取用户ID");
        }
        return userId;
    }
}

