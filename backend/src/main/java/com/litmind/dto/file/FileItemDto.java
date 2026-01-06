package com.litmind.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileItemDto {
    private Long id;
    private String name;
    private String originalName;
    private Long fileSize;
    private String fileType;
    private Long folderId;
    private LocalDateTime createdAt;
    private String uploadStatus;
}

