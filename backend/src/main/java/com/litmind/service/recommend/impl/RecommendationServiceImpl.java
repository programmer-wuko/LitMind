package com.litmind.service.recommend.impl;

import com.litmind.common.exception.BusinessException;
import com.litmind.model.dto.RecommendationDTO;
import com.litmind.model.entity.File;
import com.litmind.model.entity.Recommendation;
import com.litmind.repository.FileRepository;
import com.litmind.repository.RecommendationRepository;
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

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final FileRepository fileRepository;
    private final RecommendationRepository recommendationRepository;
    private final FileStorageService fileStorageService;
    private final Optional<ExternalPaperService> externalPaperService;
    
    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Override
    public List<RecommendationDTO> getRecommendationsByFileId(Long fileId) {
        // 验证文件是否存在
        fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));
        
        List<Recommendation> recommendations = recommendationRepository.findByFileId(fileId);
        return recommendations.stream().map(this::convertToDTO).toList();
    }

    @Override
    @Transactional
    public void generateRecommendations(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(404, "文件不存在"));

        try {
            // 1. 读取文件内容
            String fileContent = readFileContent(file);
            
            // 2. 使用LangChain4j生成推荐
            if (openaiApiKey != null && !openaiApiKey.isEmpty()) {
                generateAiRecommendations(file, fileContent);
            }
            
            // 3. 生成外部论文推荐
            if (externalPaperService.isPresent()) {
                externalPaperService.get().generateExternalRecommendations(file, fileContent);
            }
            
        } catch (Exception e) {
            log.error("生成推荐失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    private String readFileContent(File file) {
        try (InputStream inputStream = fileStorageService.downloadFile(file.getFilePath())) {
            // 读取文件内容（这里简化处理，实际应根据文件类型进行适当处理）
            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取文件内容失败: {}", e.getMessage(), e);
            return "";
        }
    }

    private void generateAiRecommendations(File file, String content) {
        try {
            // 初始化OpenAI模型
            OpenAiChatModel model = OpenAiChatModel.builder()
                    .apiKey(openaiApiKey)
                    .modelName("gpt-4o-mini")
                    .build();

            // 创建AI服务
            PaperRecommender recommender = AiServices.create(PaperRecommender.class, model);

            // 生成推荐
            String recommendations = recommender.recommendPapers(content);
            log.info("AI推荐结果: {}", recommendations);

            // 解析并保存推荐
            saveAiRecommendations(file, recommendations);
        } catch (Exception e) {
            log.error("AI推荐失败: {}", e.getMessage(), e);
        }
    }

    private void saveAiRecommendations(File file, String recommendations) {
        // 简单解析推荐结果，实际应根据返回格式进行更复杂的解析
        String[] lines = recommendations.split("\\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                Recommendation recommendation = new Recommendation();
                recommendation.setFileId(file.getId());
                recommendation.setTitle(line.trim());
                recommendation.setType("AI推荐");
                recommendation.setSource("OpenAI");
                recommendationRepository.save(recommendation);
            }
        }
    }

    private RecommendationDTO convertToDTO(Recommendation recommendation) {
        RecommendationDTO dto = new RecommendationDTO();
        dto.setId(recommendation.getId());
        dto.setFileId(recommendation.getFileId());
        dto.setTitle(recommendation.getTitle());
        dto.setAuthors(recommendation.getAuthors());
        dto.setYear(recommendation.getYear());
        dto.setJournal(recommendation.getJournal());
        dto.setAbstract(recommendation.getAbstract());
        dto.setUrl(recommendation.getUrl());
        dto.setType(recommendation.getType());
        dto.setSource(recommendation.getSource());
        dto.setScore(recommendation.getScore());
        dto.setCreatedAt(recommendation.getCreatedAt());
        return dto;
    }

    // LangChain4j服务接口
    interface PaperRecommender {
        @dev.langchain4j.service.SystemMessage("你是一个专业的学术论文推荐助手，根据用户提供的论文内容，推荐5篇最相关的论文。")
        String recommendPapers(String content);
    }
}
