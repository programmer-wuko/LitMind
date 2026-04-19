package com.litmind.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.litmind.service.ai.AiService;
import com.litmind.service.pdf.PdfAnalysisService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final ChatLanguageModel chatLanguageModel;
    private final ObjectMapper objectMapper;

    @Override
    public String getModelName() {
        if (chatLanguageModel.getClass().getSimpleName().equals("DisabledChatLanguageModel")) {
            return "mock-model";
        }
        return chatLanguageModel.getClass().getSimpleName();
    }

    @Override
    public PdfAnalysisService.PdfAnalysisResult analyzePdf(String pdfText) {
        String prompt = buildAnalysisPrompt(pdfText);
        String response = callLLM(prompt);
        return parseAnalysisResponse(response);
    }

    @Override
    public String answerQuestion(String pdfText, String question) {
        String prompt = buildQAPrompt(pdfText, question);
        return callLLM(prompt);
    }

    private String buildAnalysisPrompt(String pdfText) {
        String truncatedText = pdfText.length() > 10000
                ? pdfText.substring(0, 10000) + "..."
                : pdfText;

        return "请分析以下科研论文内容，并按照以下格式输出JSON结果：\n\n" +
                "论文内容：\n" + truncatedText + "\n\n" +
                "请提供以下分析（每个部分200-500字）：\n" +
                "1. 研究背景：阐述该论文所处领域的研究现状、核心问题及研究动机\n" +
                "2. 核心内容：概括论文提出的方法、模型、算法或关键技术\n" +
                "3. 实验结果分析：总结实验设计、关键数据、性能指标及主要结论\n" +
                "4. 其他补充：包括创新点、局限性、潜在应用场景及未来研究方向\n\n" +
                "请以JSON格式返回，格式如下：\n" +
                "{\n" +
                "  \"researchBackground\": \"研究背景内容\",\n" +
                "  \"coreContent\": \"核心内容\",\n" +
                "  \"experimentResults\": \"实验结果分析\",\n" +
                "  \"additionalInfo\": \"其他补充\"\n" +
                "}";
    }

    private String buildQAPrompt(String pdfText, String question) {
        String truncatedText = pdfText.length() > 8000
                ? pdfText.substring(0, 8000) + "..."
                : pdfText;

        return "基于以下论文内容回答用户问题。如果论文中没有相关信息，请说明。\n\n" +
                "论文内容：\n" + truncatedText + "\n\n" +
                "用户问题：" + question + "\n\n" +
                "请提供准确、简洁的回答：";
    }

    private String callLLM(String prompt) {
        try {
            log.debug("调用LangChain4j AI服务 - prompt长度: {}", prompt.length());
            String response = chatLanguageModel.chat(prompt);
            log.debug("AI响应长度: {}", response != null ? response.length() : 0);
            return response;
        } catch (Exception e) {
            log.error("调用LangChain4j LLM失败: {}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("API key")) {
                throw new RuntimeException("AI API Key无效或已过期，请检查配置。错误详情: " + e.getMessage());
            } else if (e.getMessage() != null && (e.getMessage().contains("quota") || e.getMessage().contains("余额"))) {
                throw new RuntimeException("AI服务账户余额不足，请充值后重试。错误详情: " + e.getMessage());
            } else if (e.getMessage() != null && e.getMessage().contains("rate limit")) {
                throw new RuntimeException("AI服务请求频率过高，请稍后重试。错误详情: " + e.getMessage());
            }
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }

    private PdfAnalysisService.PdfAnalysisResult parseAnalysisResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            PdfAnalysisService.PdfAnalysisResult result = new PdfAnalysisService.PdfAnalysisResult();
            result.setResearchBackground(jsonNode.get("researchBackground").asText());
            result.setCoreContent(jsonNode.get("coreContent").asText());
            result.setExperimentResults(jsonNode.get("experimentResults").asText());
            result.setAdditionalInfo(jsonNode.get("additionalInfo").asText());
            return result;
        } catch (Exception e) {
            log.warn("解析AI响应失败，使用原始响应: {}", e.getMessage());
            PdfAnalysisService.PdfAnalysisResult result = new PdfAnalysisService.PdfAnalysisResult();
            result.setResearchBackground(response);
            result.setCoreContent(response);
            result.setExperimentResults(response);
            result.setAdditionalInfo(response);
            return result;
        }
    }
}
