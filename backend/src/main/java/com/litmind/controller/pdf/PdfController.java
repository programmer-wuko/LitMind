package com.litmind.controller.pdf;

import com.litmind.common.response.ApiResponse;
import com.litmind.common.util.SecurityUtil;
import com.litmind.model.entity.PdfAnalysis;
import com.litmind.service.pdf.PdfAnalysisService;
import com.litmind.service.recommend.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pdf")
@RequiredArgsConstructor
@Slf4j
public class PdfController {

    private final PdfAnalysisService pdfAnalysisService;
    private final RecommendationService recommendationService;
    private final SecurityUtil securityUtil;

    @GetMapping("/{fileId}/analysis")
    public ApiResponse<PdfAnalysis> getAnalysis(
            @PathVariable Long fileId,
            Authentication authentication) {
        // 记录用户查看PDF的行为
        if (authentication != null) {
            Long userId = securityUtil.getUserId(authentication);
            if (userId != null) {
                try {
                    recommendationService.recordUserBehavior(userId, fileId, "VIEW", 
                        "{\"action\":\"view_pdf_analysis\",\"fileId\":" + fileId + "}");
                } catch (Exception e) {
                    log.warn("记录用户行为失败: {}", e.getMessage());
                }
            }
        }
        
        PdfAnalysis analysis = pdfAnalysisService.getAnalysis(fileId);
        if (analysis == null) {
            return ApiResponse.error(404, "分析记录不存在");
        }
        return ApiResponse.success(analysis);
    }

    @PostMapping("/{fileId}/analyze")
    public ApiResponse<PdfAnalysis> analyzePdf(
            @PathVariable Long fileId,
            Authentication authentication) {
        try {
            PdfAnalysis analysis = pdfAnalysisService.analyzePdf(fileId);
            
            // 记录用户分析PDF的行为
            if (authentication != null) {
                Long userId = securityUtil.getUserId(authentication);
                if (userId != null) {
                    try {
                        recommendationService.recordUserBehavior(userId, fileId, "ANALYZE", 
                            "{\"action\":\"analyze_pdf\",\"fileId\":" + fileId + "}");
                        
                        // 分析完成后，异步生成推荐（如果用户有足够的行为数据）
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000); // 等待2秒，确保行为记录已保存
                                recommendationService.generateRecommendations(userId);
                                log.info("为用户 {} 自动生成推荐", userId);
                            } catch (Exception e) {
                                log.warn("自动生成推荐失败: {}", e.getMessage());
                            }
                        }).start();
                    } catch (Exception e) {
                        log.warn("记录用户行为失败: {}", e.getMessage());
                    }
                }
            }
            
            return ApiResponse.success("分析完成", analysis);
        } catch (Exception e) {
            // 记录详细错误信息
            log.error("PDF分析请求失败: fileId={}, error={}", fileId, e.getMessage(), e);
            throw e; // 让GlobalExceptionHandler处理
        }
    }

    @PutMapping("/{fileId}/analysis")
    public ApiResponse<PdfAnalysis> updateAnalysis(
            @PathVariable Long fileId,
            @RequestBody Map<String, String> request) {
        PdfAnalysisService.PdfAnalysisUpdateRequest updateRequest = 
                new PdfAnalysisService.PdfAnalysisUpdateRequest();
        updateRequest.setResearchBackground(request.get("researchBackground"));
        updateRequest.setCoreContent(request.get("coreContent"));
        updateRequest.setExperimentResults(request.get("experimentResults"));
        updateRequest.setAdditionalInfo(request.get("additionalInfo"));

        PdfAnalysis analysis = pdfAnalysisService.updateAnalysis(fileId, updateRequest);
        return ApiResponse.success("分析更新成功", analysis);
    }
}

