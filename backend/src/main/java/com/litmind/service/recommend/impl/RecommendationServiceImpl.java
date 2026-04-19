package com.litmind.service.recommend.impl;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.entity.File;
import com.litmind.model.entity.Recommendation;
import com.litmind.model.entity.UserBehavior;
import com.litmind.repository.FileRepository;
import com.litmind.repository.RecommendationRepository;
import com.litmind.repository.UserBehaviorRepository;
import com.litmind.service.file.FileStorageService;
import com.litmind.service.recommend.ExternalPaperService;
import com.litmind.service.recommend.RecommendationService;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final FileRepository fileRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserBehaviorRepository userBehaviorRepository;
    private final FileStorageService fileStorageService;
    private final Optional<ExternalPaperService> externalPaperService;

    @Value("${ai.api-key:}")
    private String openaiApiKey;

    @Override
    public List<Recommendation> getUserRecommendations(Long userId) {
        List<File> userFiles = fileRepository.findByUserId(userId);
        return userFiles.stream()
                .map(file -> recommendationRepository.findByRecommendedFileId(file.getId()))
                .flatMap(List::stream)
                .toList();
    }

    @Override
    @Transactional
    public void generateRecommendations(Long userId) {
        List<File> userFiles = fileRepository.findByUserId(userId);

        for (File file : userFiles) {
            try {
                if (file.getFileType() != null && file.getFileType().startsWith("application/pdf")) {
                    String fileContent = readFileContent(file);

                    if (openaiApiKey != null && !openaiApiKey.isEmpty()) {
                        generateAiRecommendations(file, fileContent);
                    }

                    if (externalPaperService.isPresent()) {
                        externalPaperService.get().generateExternalRecommendations(file, fileContent);
                    }
                }
            } catch (Exception e) {
                log.error("为文件生成推荐失败: fileId={}, error={}", file.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void recordUserBehavior(Long userId, Long fileId, String behaviorType, String behaviorData) {
        fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setFileId(fileId);
        behavior.setBehaviorType(behaviorType);
        behavior.setBehaviorData(behaviorData);

        userBehaviorRepository.save(behavior);
    }

    @Override
    @Transactional
    public void updateRecommendationFeedback(Long userId, Long recommendationId, String feedback) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new BusinessException(404, "推荐不存在"));

        fileRepository.findByIdAndUserId(recommendation.getRecommendedFileId(), userId)
                .orElseThrow(() -> new BusinessException(403, "无权访问此推荐"));

        recommendation.setFeedback(feedback);
        recommendationRepository.save(recommendation);
    }

    private String readFileContent(File file) {
        try (InputStream inputStream = fileStorageService.downloadFile(file.getFilePath());
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            log.info("PDF文本提取完成，长度: {}", content.length());
            return content.toString();
        } catch (Exception e) {
            log.error("读取文件内容失败: {}", e.getMessage(), e);
            return "";
        }
    }

    private void generateAiRecommendations(File file, String content) {
        try {
            OpenAiChatModel model = OpenAiChatModel.builder()
                    .apiKey(openaiApiKey)
                    .modelName("gpt-4o-mini")
                    .build();

            PaperRecommender recommender = AiServices.create(PaperRecommender.class, model);
            String recommendations = recommender.recommendPapers(content);
            log.info("AI推荐结果: {}", recommendations);

            saveAiRecommendations(file, recommendations);
        } catch (Exception e) {
            log.error("AI推荐失败: {}", e.getMessage(), e);
        }
    }

    private void saveAiRecommendations(File file, String recommendations) {
        String[] lines = recommendations.split("\\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                Recommendation recommendation = new Recommendation();
                recommendation.setUserId(file.getUserId());
                recommendation.setRecommendedFileId(file.getId());
                recommendation.setPaperTitle(line.trim());
                recommendation.setPaperSource("OpenAI");
                recommendationRepository.save(recommendation);
            }
        }
    }

    interface PaperRecommender {
        @dev.langchain4j.service.SystemMessage("你是一个专业的学术论文推荐助手，根据用户提供的论文内容，推荐5篇最相关的论文。")
        String recommendPapers(String content);
    }
}
