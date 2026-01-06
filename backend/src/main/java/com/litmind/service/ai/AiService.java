package com.litmind.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.litmind.service.pdf.PdfAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    @Value("${ai.provider}")
    private String provider;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.max-tokens:4000}")
    private int maxTokens;

    @Value("${ai.temperature:0.7}")
    private double temperature;

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public AiService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @javax.annotation.PostConstruct
    public void init() {
        // 打印配置信息（隐藏敏感信息）
        String maskedApiKey = apiKey != null && apiKey.length() > 10 
            ? apiKey.substring(0, 10) + "..." 
            : (apiKey != null ? apiKey : "null");
        log.info("AI服务初始化 - provider: {}, apiKey: {}, baseUrl: {}, model: {}", 
            provider, maskedApiKey, baseUrl, model);
        
        // 检查API Key是否有效
        if (apiKey == null || apiKey.isEmpty() || "your-api-key-here".equals(apiKey)) {
            log.warn("⚠️ AI API Key未配置或使用默认值，将返回模拟分析结果");
            log.warn("⚠️ 请在IDEA运行配置中设置环境变量: AI_API_KEY=your-actual-api-key");
        } else {
            log.info("✅ AI API Key已配置，将使用真实的AI服务");
        }
    }

    public String getModelName() {
        return model;
    }

    public PdfAnalysisService.PdfAnalysisResult analyzePdf(String pdfText) {
        String prompt = buildAnalysisPrompt(pdfText);
        String response = callLLM(prompt);
        return parseAnalysisResponse(response);
    }

    public String answerQuestion(String pdfText, String question) {
        String prompt = buildQAPrompt(pdfText, question);
        return callLLM(prompt);
    }

    private String buildAnalysisPrompt(String pdfText) {
        // 限制PDF文本长度，避免超出Token限制
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
            if ("openai".equals(provider)) {
                return callOpenAI(prompt);
            } else if ("anthropic".equals(provider)) {
                return callAnthropic(prompt);
            } else {
                throw new RuntimeException("不支持的AI提供商: " + provider);
            }
        } catch (Exception e) {
            log.error("调用LLM失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }

    private String callOpenAI(String prompt) throws IOException {
        // 验证API Key
        if (apiKey == null || apiKey.isEmpty() || "your-api-key-here".equals(apiKey)) {
            log.warn("AI API Key未配置，返回模拟分析结果");
            log.warn("当前apiKey值: {}", apiKey != null ? (apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : apiKey) : "null");
            return getMockAnalysisResponse();
        }
        
        log.debug("调用AI服务 - baseUrl: {}, model: {}, prompt长度: {}", baseUrl, model, prompt.length());

        String url = baseUrl + "/chat/completions";

        String requestBody = objectMapper.writeValueAsString(new Object() {
            public final String model = AiService.this.model;
            public final Object[] messages = new Object[]{
                    new Object() {
                        public final String role = "user";
                        public final String content = prompt;
                    }
            };
            public final int max_tokens = maxTokens;
            public final double temperature = AiService.this.temperature;
        });

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无错误详情";
                log.error("OpenAI API调用失败: status={}, body={}", response.code(), errorBody);
                
                // 处理特定错误码
                if (response.code() == 402) {
                    throw new IOException("AI服务账户余额不足，请充值后重试。错误详情: " + errorBody);
                } else if (response.code() == 401) {
                    throw new IOException("AI API Key无效或已过期，请检查配置。错误详情: " + errorBody);
                } else if (response.code() == 429) {
                    throw new IOException("AI服务请求频率过高，请稍后重试。错误详情: " + errorBody);
                }
                
                throw new IOException("OpenAI API调用失败: HTTP " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            if (!jsonNode.has("choices") || jsonNode.get("choices").size() == 0) {
                throw new IOException("OpenAI API响应格式错误: " + responseBody);
            }
            
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        }
    }

    private String callAnthropic(String prompt) throws IOException {
        // Anthropic API调用实现（类似OpenAI）
        // 这里简化处理，实际需要根据Anthropic API文档实现
        throw new UnsupportedOperationException("Anthropic API暂未实现");
    }

    /**
     * 返回模拟的分析结果（用于开发测试，当API Key未配置时）
     */
    private String getMockAnalysisResponse() {
        return "{\n" +
                "  \"researchBackground\": \"这是模拟的研究背景内容。在实际使用中，需要配置有效的AI API Key才能获得真实的AI分析结果。研究背景部分通常包括该领域的研究现状、核心问题及研究动机。\",\n" +
                "  \"coreContent\": \"这是模拟的核心内容。核心内容部分概括了论文提出的方法、模型、算法或关键技术。请配置AI_API_KEY环境变量以获取真实的AI分析。\",\n" +
                "  \"experimentResults\": \"这是模拟的实验结果分析。实验结果部分总结了实验设计、关键数据、性能指标及主要结论。配置AI服务后，将获得基于实际PDF内容的分析结果。\",\n" +
                "  \"additionalInfo\": \"这是模拟的其他补充信息。其他补充包括创新点、局限性、潜在应用场景及未来研究方向。要获取真实分析，请在IDEA运行配置中设置AI_API_KEY环境变量。\"\n" +
                "}";
    }

    private PdfAnalysisService.PdfAnalysisResult parseAnalysisResponse(String response) {
        try {
            // 尝试解析JSON响应
            JsonNode jsonNode = objectMapper.readTree(response);
            PdfAnalysisService.PdfAnalysisResult result = new PdfAnalysisService.PdfAnalysisResult();
            result.setResearchBackground(jsonNode.get("researchBackground").asText());
            result.setCoreContent(jsonNode.get("coreContent").asText());
            result.setExperimentResults(jsonNode.get("experimentResults").asText());
            result.setAdditionalInfo(jsonNode.get("additionalInfo").asText());
            return result;
        } catch (Exception e) {
            log.warn("解析AI响应失败，使用原始响应: {}", e.getMessage());
            // 如果解析失败，返回默认结构
            PdfAnalysisService.PdfAnalysisResult result = new PdfAnalysisService.PdfAnalysisResult();
            result.setResearchBackground(response);
            result.setCoreContent(response);
            result.setExperimentResults(response);
            result.setAdditionalInfo(response);
            return result;
        }
    }
}

