package com.litmind.config;

import com.litmind.service.pdf.PdfAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaConsumerConfig {

    private final PdfAnalysisService pdfAnalysisService;

    public KafkaConsumerConfig(PdfAnalysisService pdfAnalysisService) {
        this.pdfAnalysisService = pdfAnalysisService;
    }

    @org.springframework.kafka.annotation.KafkaListener(topics = "pdf-analysis", groupId = "litmind-group")
    public void consumePdfAnalysis(String fileId) {
        try {
            log.info("收到PDF分析任务: fileId={}", fileId);
            pdfAnalysisService.analyzePdf(Long.parseLong(fileId));
            log.info("PDF分析完成: fileId={}", fileId);
        } catch (Exception e) {
            log.error("PDF分析失败: fileId={}, error={}", fileId, e.getMessage(), e);
        }
    }
}

