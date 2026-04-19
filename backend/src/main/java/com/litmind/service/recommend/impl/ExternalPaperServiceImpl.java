package com.litmind.service.recommend.impl;

import com.litmind.model.entity.File;
import com.litmind.model.entity.Recommendation;
import com.litmind.repository.RecommendationRepository;
import com.litmind.service.recommend.ExternalPaperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalPaperServiceImpl implements ExternalPaperService {

    private final RecommendationRepository recommendationRepository;
    private final RestTemplate restTemplate;

    @Override
    public void generateExternalRecommendations(File file, String content) {
        try {
            // 从arXiv获取推荐
            List<Recommendation> arxivRecommendations = fetchFromArXiv(content);
            saveRecommendations(file, arxivRecommendations);

            // 从Semantic Scholar获取推荐
            List<Recommendation> semanticScholarRecommendations = fetchFromSemanticScholar(content);
            saveRecommendations(file, semanticScholarRecommendations);

        } catch (Exception e) {
            log.error("获取外部论文推荐失败: {}", e.getMessage(), e);
        }
    }

    private List<Recommendation> fetchFromArXiv(String content) {
        try {
            // 这里简化处理，实际应调用arXiv API
            // 示例：https://arxiv.org/search/?query=content
            log.info("从arXiv获取推荐");
            // 模拟返回结果
            List<Recommendation> recommendations = new ArrayList<>();
            Recommendation rec1 = new Recommendation();
            rec1.setTitle("Deep Learning for PDF Analysis");
            rec1.setAuthors("John Doe, Jane Smith");
            rec1.setYear(2024);
            rec1.setJournal("arXiv");
            rec1.setAbstract("This paper discusses deep learning techniques for PDF document analysis.");
            rec1.setUrl("https://arxiv.org/abs/2401.00001");
            rec1.setType("外部论文");
            rec1.setSource("arXiv");
            rec1.setScore(0.95);
            recommendations.add(rec1);
            return recommendations;
        } catch (Exception e) {
            log.error("从arXiv获取推荐失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<Recommendation> fetchFromSemanticScholar(String content) {
        try {
            // 这里简化处理，实际应调用Semantic Scholar API
            // 示例：https://api.semanticscholar.org/graph/v1/paper/search
            log.info("从Semantic Scholar获取推荐");
            // 模拟返回结果
            List<Recommendation> recommendations = new ArrayList<>();
            Recommendation rec1 = new Recommendation();
            rec1.setTitle("Advanced PDF Processing Techniques");
            rec1.setAuthors("Alice Johnson, Bob Brown");
            rec1.setYear(2023);
            rec1.setJournal("Semantic Scholar");
            rec1.setAbstract("This paper presents advanced techniques for processing PDF documents.");
            rec1.setUrl("https://www.semanticscholar.org/paper/advanced-pdf-processing-techniques");
            rec1.setType("外部论文");
            rec1.setSource("Semantic Scholar");
            rec1.setScore(0.92);
            recommendations.add(rec1);
            return recommendations;
        } catch (Exception e) {
            log.error("从Semantic Scholar获取推荐失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private void saveRecommendations(File file, List<Recommendation> recommendations) {
        for (Recommendation recommendation : recommendations) {
            recommendation.setFileId(file.getId());
            recommendationRepository.save(recommendation);
        }
    }
}
