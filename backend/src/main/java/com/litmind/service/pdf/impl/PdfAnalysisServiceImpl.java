package com.litmind.service.pdf.impl;

import com.litmind.model.entity.File;
import com.litmind.model.enums.FileStatus;
import com.litmind.repository.FileRepository;
import com.litmind.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.InputStream;

import com.litmind.service.pdf.PdfAnalysisService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfAnalysisServiceImpl implements PdfAnalysisService {

    private final FileStorageService fileStorageService;
    private final FileRepository fileRepository;

    @Override
    public void analyzePdf(File file) {
        log.info("开始分析PDF文件: {}", file.getName());
        try {
            // 读取PDF文件
            try (InputStream inputStream = fileStorageService.downloadFile(file.getFilePath());
                 PDDocument document = PDDocument.load(inputStream)) {

                // 提取文本
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                log.info("PDF文件分析完成，提取文本长度: {}", text.length());

                // 可以在这里添加更多分析逻辑，如：
                // 1. 提取元数据
                // 2. 提取图片
                // 3. 分析文本内容
                // 4. 生成摘要

            } catch (Exception e) {
                log.error("PDF分析失败: {}", e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            log.error("PDF分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("PDF分析失败", e);
        }
    }
}
