package com.litmind.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderTreeDto {
    private Long id;
    private String name;
    private Long parentId;
    private String path;
    private List<FolderTreeDto> children;
    private List<FileItemDto> files;
}

