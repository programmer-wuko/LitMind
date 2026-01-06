package com.litmind.controller.ai;

import com.litmind.common.response.ApiResponse;
import com.litmind.common.util.SecurityUtil;
import com.litmind.model.entity.AiQaRecord;
import com.litmind.repository.AiQaRecordRepository;
import com.litmind.service.ai.AiService;
import com.litmind.service.file.FileService;
import com.litmind.service.pdf.PdfAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final FileService fileService;
    private final PdfAnalysisService pdfAnalysisService;
    private final AiQaRecordRepository aiQaRecordRepository;
    private final SecurityUtil securityUtil;

    @PostMapping("/qa")
    public ApiResponse<String> askQuestion(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        Long fileId = Long.valueOf(request.get("fileId"));
        String question = request.get("question");

        // 获取文件
        com.litmind.model.entity.File file = fileService.getFile(userId, fileId);

        // 提取PDF文本（简化处理，实际应该缓存）
        String pdfText;
        try {
            pdfText = pdfAnalysisService.extractTextFromPdf(file.getFilePath());
        } catch (Exception e) {
            return ApiResponse.error(500, "PDF文本提取失败");
        }

        // 调用AI服务
        String answer = aiService.answerQuestion(pdfText, question);

        // 保存问答记录
        AiQaRecord record = new AiQaRecord();
        record.setFileId(fileId);
        record.setUserId(userId);
        record.setQuestion(question);
        record.setAnswer(answer);
        record.setModelUsed(aiService.getModelName());
        aiQaRecordRepository.save(record);

        return ApiResponse.success(answer);
    }

    private Long getUserId(Authentication authentication) {
        Long userId = securityUtil.getUserId(authentication);
        if (userId == null) {
            throw new RuntimeException("无法获取用户ID");
        }
        return userId;
    }
}

