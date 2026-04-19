package com.litmind.service.file;

import com.litmind.model.entity.Folder;

import java.util.List;

public interface FolderService {

    List<Folder> getUserFolders(Long userId, Long parentId);

    List<Folder> getPublicFolders(Long userId, Long parentId);

    List<Folder> getMyFolders(Long userId, Long parentId);

    Folder createFolder(Long userId, Long parentId, String name, Boolean isPublic);

    Folder updateFolder(Long userId, Long folderId, String newName);

    void deleteFolder(Long userId, Long folderId);
}
