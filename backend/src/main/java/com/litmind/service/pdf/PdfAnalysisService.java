package com.litmind.service.pdf;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfAnalysisService {

    private final PdfAnalysisRepository pdfAnalysisRepository;
    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final AiService aiService;

    public PdfAnalysis getAnalysis(Long fileId) {
        return pdfAnalysisRepository.findByFileId(fileId)
                .orElse(null);
    }

    @Transactional
    public PdfAnalysis analyzePdf(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        // 检查是否已有分析结果
        PdfAnalysis existingAnalysis = pdfAnalysisRepository.findByFileId(fileId).orElse(null);
        if (existingAnalysis != null && "COMPLETED".equals(existingAnalysis.getAnalysisStatus())) {
            return existingAnalysis;
        }

        // 创建或更新分析记录
        PdfAnalysis analysis = existingAnalysis != null ? existingAnalysis : new PdfAnalysis();
        analysis.setFileId(fileId);
        analysis.setAnalysisStatus("PROCESSING");

        if (existingAnalysis == null) {
            analysis = pdfAnalysisRepository.save(analysis);
        } else {
            analysis = pdfAnalysisRepository.save(analysis);
        }

        try {
            // 提取PDF文本
            String pdfText = extractTextFromPdf(file.getFilePath());

            // 调用AI服务生成分析
            PdfAnalysisResult result = aiService.analyzePdf(pdfText);

            // 保存分析结果
            analysis.setResearchBackground(result.getResearchBackground());
            analysis.setCoreContent(result.getCoreContent());
            analysis.setExperimentResults(result.getExperimentResults());
            analysis.setAdditionalInfo(result.getAdditionalInfo());
            analysis.setAnalysisStatus("COMPLETED");
            analysis.setAnalysisModel(aiService.getModelName());

            return pdfAnalysisRepository.save(analysis);
        } catch (BusinessException e) {
            // 业务异常直接抛出
            log.error("PDF分析失败: fileId={}, error={}", fileId, e.getMessage(), e);
            analysis.setAnalysisStatus("FAILED");
            pdfAnalysisRepository.save(analysis);
            throw e;
        } catch (Exception e) {
            log.error("PDF分析失败: fileId={}, error={}", fileId, e.getMessage(), e);
            analysis.setAnalysisStatus("FAILED");
            pdfAnalysisRepository.save(analysis);
            // 根据异常类型返回更友好的错误信息
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                if (errorMsg.contains("API Key") || errorMsg.contains("未配置")) {
                    throw new BusinessException(400, "AI服务未配置，请设置AI_API_KEY环境变量");
                } else if (errorMsg.contains("余额不足") || errorMsg.contains("Insufficient Balance")) {
                    throw new BusinessException(402, "AI服务账户余额不足，请充值后重试。如需免费使用，可配置Ollama本地部署。");
                } else if (errorMsg.contains("无效") || errorMsg.contains("已过期") || errorMsg.contains("401")) {
                    throw new BusinessException(401, "AI API Key无效或已过期，请检查配置");
                } else if (errorMsg.contains("频率过高") || errorMsg.contains("429")) {
                    throw new BusinessException(429, "AI服务请求频率过高，请稍后重试");
                }
            }
            throw new BusinessException(500, "PDF分析失败: " + errorMsg);
        }
    }

    @Transactional
    public PdfAnalysis updateAnalysis(Long fileId, PdfAnalysisUpdateRequest request) {
        PdfAnalysis analysis = pdfAnalysisRepository.findByFileId(fileId)
                .orElseThrow(() -> new BusinessException(404, "分析记录不存在"));

        if (request.getResearchBackground() != null) {
            analysis.setResearchBackground(request.getResearchBackground());
        }
        if (request.getCoreContent() != null) {
            analysis.setCoreContent(request.getCoreContent());
        }
        if (request.getExperimentResults() != null) {
            analysis.setExperimentResults(request.getExperimentResults());
        }
        if (request.getAdditionalInfo() != null) {
            analysis.setAdditionalInfo(request.getAdditionalInfo());
        }

        return pdfAnalysisRepository.save(analysis);
    }

    public String extractTextFromPdf(String filePath) {
        try (InputStream inputStream = fileStorageService.downloadFile(filePath);
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            log.error("PDF文本提取失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "PDF文本提取失败: " + e.getMessage());
        }
    }

    public static class PdfAnalysisUpdateRequest {
        private String researchBackground;
        private String coreContent;
        private String experimentResults;
        private String additionalInfo;

        // Getters and Setters
        public String getResearchBackground() { return researchBackground; }
        public void setResearchBackground(String researchBackground) { this.researchBackground = researchBackground; }
        public String getCoreContent() { return coreContent; }
        public void setCoreContent(String coreContent) { this.coreContent = coreContent; }
        public String getExperimentResults() { return experimentResults; }
        public void setExperimentResults(String experimentResults) { this.experimentResults = experimentResults; }
        public String getAdditionalInfo() { return additionalInfo; }
        public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    }

    public static class PdfAnalysisResult {
        private String researchBackground;
        private String coreContent;
        private String experimentResults;
        private String additionalInfo;

        // Getters and Setters
        public String getResearchBackground() { return researchBackground; }
        public void setResearchBackground(String researchBackground) { this.researchBackground = researchBackground; }
        public String getCoreContent() { return coreContent; }
        public void setCoreContent(String coreContent) { this.coreContent = coreContent; }
        public String getExperimentResults() { return experimentResults; }
        public void setExperimentResults(String experimentResults) { this.experimentResults = experimentResults; }
        public String getAdditionalInfo() { return additionalInfo; }
        public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    }
}

