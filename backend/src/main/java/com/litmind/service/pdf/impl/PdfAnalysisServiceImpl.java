package com.litmind.service.pdf.impl;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.entity.File;
import com.litmind.model.entity.PdfAnalysis;
import com.litmind.repository.FileRepository;
import com.litmind.repository.PdfAnalysisRepository;
import com.litmind.service.ai.AiService;
import com.litmind.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

import com.litmind.service.pdf.PdfAnalysisService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfAnalysisServiceImpl implements PdfAnalysisService {

    private final FileStorageService fileStorageService;
    private final FileRepository fileRepository;
    private final PdfAnalysisRepository pdfAnalysisRepository;
    private final AiService aiService;

    @Override
    public PdfAnalysis getAnalysis(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));
        return pdfAnalysisRepository.findByFileId(fileId)
                .orElseThrow(() -> new BusinessException(404, "PDF分析记录不存在"));
    }

    @Override
    @Transactional
    public PdfAnalysis analyzePdf(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        if (file.getFileType() == null || !file.getFileType().startsWith("application/pdf")) {
            throw new BusinessException(400, "该文件不是PDF文件");
        }

        try {
            String pdfText = extractTextFromPdf(file.getFilePath());
            PdfAnalysisService.PdfAnalysisResult result = aiService.analyzePdf(pdfText);

            PdfAnalysis pdfAnalysis = pdfAnalysisRepository.findByFileId(fileId)
                    .orElse(new PdfAnalysis());
            pdfAnalysis.setFileId(fileId);
            pdfAnalysis.setResearchBackground(result.getResearchBackground());
            pdfAnalysis.setCoreContent(result.getCoreContent());
            pdfAnalysis.setExperimentResults(result.getExperimentResults());
            pdfAnalysis.setAdditionalInfo(result.getAdditionalInfo());

            PdfAnalysis savedAnalysis = pdfAnalysisRepository.save(pdfAnalysis);

            file.setUploadStatus("COMPLETED");
            fileRepository.save(file);

            return savedAnalysis;
        } catch (Exception e) {
            log.error("PDF分析失败: {}", e.getMessage(), e);
            file.setUploadStatus("FAILED");
            fileRepository.save(file);
            throw new BusinessException(500, "PDF分析失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PdfAnalysis updateAnalysis(Long fileId, PdfAnalysisUpdateRequest request) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        PdfAnalysis pdfAnalysis = pdfAnalysisRepository.findByFileId(fileId)
                .orElseThrow(() -> new BusinessException(404, "PDF分析记录不存在"));

        if (request.getResearchBackground() != null) {
            pdfAnalysis.setResearchBackground(request.getResearchBackground());
        }
        if (request.getCoreContent() != null) {
            pdfAnalysis.setCoreContent(request.getCoreContent());
        }
        if (request.getExperimentResults() != null) {
            pdfAnalysis.setExperimentResults(request.getExperimentResults());
        }
        if (request.getAdditionalInfo() != null) {
            pdfAnalysis.setAdditionalInfo(request.getAdditionalInfo());
        }

        return pdfAnalysisRepository.save(pdfAnalysis);
    }

    @Override
    public String extractTextFromPdf(String filePath) {
        try (InputStream inputStream = fileStorageService.downloadFile(filePath);
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("PDF文本提取完成，长度: {}", text.length());
            return text;
        } catch (Exception e) {
            log.error("PDF文本提取失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "PDF文本提取失败: " + e.getMessage());
        }
    }
}
