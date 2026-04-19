package com.litmind.service.file;

import com.litmind.model.entity.Attachment;
import com.litmind.model.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface AttachmentService {

    List<Attachment> getAttachmentsByFileId(Long fileId);

    Attachment getAttachmentById(Long attachmentId);

    Attachment uploadAttachment(Long fileId, MultipartFile multipartFile);

    InputStream downloadAttachment(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    void addFileAttachment(File file);
}
