package com.litmind.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.DisabledChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class LangChain4jConfig {

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${ai.model:gpt-4-turbo-preview}")
    private String model;

    @Value("${ai.max-tokens:4000}")
    private int maxTokens;

    @Value("${ai.temperature:0.7}")
    private double temperature;

    @Value("${ai.enabled:true}")
    private boolean aiEnabled;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if (!aiEnabled || apiKey == null || apiKey.isEmpty() || "your-api-key-here".equals(apiKey)) {
            log.warn("AI服务未启用或API Key无效，将返回模拟结果");
            log.warn("请配置有效的AI_API_KEY环境变量");
            return new DisabledChatLanguageModel();
        }

        log.info("初始化LangChain4j AI服务 - baseUrl: {}, model: {}", baseUrl, model);

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}
