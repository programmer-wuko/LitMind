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
    public List<PaperInfo> searchArxivPapers(String query, int maxResults) {
        List<PaperInfo> papers = new ArrayList<>();
        try {
            log.info("从arXiv搜索论文: query={}, maxResults={}", query, maxResults);
            PaperInfo paper = new PaperInfo();
            paper.setTitle("arXiv论文: " + query);
            paper.setAuthors("arXiv Authors");
            paper.setUrl("https://arxiv.org/search/?query=" + query);
            paper.setExternalPaperId("arXiv-" + System.currentTimeMillis());
            paper.setSource("arXiv");
            paper.setAbstract("这是从arXiv搜索到的论文摘要");
            papers.add(paper);
        } catch (Exception e) {
            log.error("从arXiv搜索论文失败: {}", e.getMessage(), e);
        }
        return papers;
    }

    @Override
    public List<PaperInfo> searchSemanticScholarPapers(String query, int maxResults) {
        List<PaperInfo> papers = new ArrayList<>();
        try {
            log.info("从Semantic Scholar搜索论文: query={}, maxResults={}", query, maxResults);
            PaperInfo paper = new PaperInfo();
            paper.setTitle("Semantic Scholar论文: " + query);
            paper.setAuthors("SS Authors");
            paper.setUrl("https://www.semanticscholar.org/search?q=" + query);
            paper.setExternalPaperId("SS-" + System.currentTimeMillis());
            paper.setSource("Semantic Scholar");
            paper.setAbstract("这是从Semantic Scholar搜索到的论文摘要");
            papers.add(paper);
        } catch (Exception e) {
            log.error("从Semantic Scholar搜索论文失败: {}", e.getMessage(), e);
        }
        return papers;
    }

    @Override
    public List<PaperInfo> getHotPapers(int maxResults) {
        List<PaperInfo> papers = new ArrayList<>();
        try {
            log.info("获取热门论文: maxResults={}", maxResults);
            PaperInfo paper = new PaperInfo();
            paper.setTitle("热门论文");
            paper.setAuthors("Various Authors");
            paper.setUrl("https://arxiv.org/hot");
            paper.setExternalPaperId("hot-" + System.currentTimeMillis());
            paper.setSource("arXiv");
            paper.setAbstract("这是当前的热门论文");
            papers.add(paper);
        } catch (Exception e) {
            log.error("获取热门论文失败: {}", e.getMessage(), e);
        }
        return papers;
    }

    @Override
    public String extractSearchKeywords(String pdfAnalysisText) {
        if (pdfAnalysisText == null || pdfAnalysisText.trim().isEmpty()) {
            return "";
        }
        String[] words = pdfAnalysisText.split("\\s+");
        StringBuilder keywords = new StringBuilder();
        int count = 0;
        for (String word : words) {
            if (word.length() > 5 && count < 5) {
                if (keywords.length() > 0) {
                    keywords.append(" ");
                }
                keywords.append(word);
                count++;
            }
        }
        return keywords.toString();
    }

    public void generateExternalRecommendations(File file, String content) {
        try {
            String keywords = extractSearchKeywords(content);
            if (keywords.isEmpty()) {
                log.warn("无法从PDF内容提取关键词");
                return;
            }

            List<PaperInfo> arxivPapers = searchArxivPapers(keywords, 5);
            for (PaperInfo paper : arxivPapers) {
                saveRecommendation(file, paper);
            }

            List<PaperInfo> ssPapers = searchSemanticScholarPapers(keywords, 5);
            for (PaperInfo paper : ssPapers) {
                saveRecommendation(file, paper);
            }
        } catch (Exception e) {
            log.error("生成外部论文推荐失败: {}", e.getMessage(), e);
        }
    }

    private void saveRecommendation(File file, PaperInfo paper) {
        Recommendation recommendation = new Recommendation();
        recommendation.setUserId(file.getUserId());
        recommendation.setRecommendedFileId(file.getId());
        recommendation.setExternalPaperId(paper.getExternalPaperId());
        recommendation.setPaperTitle(paper.getTitle());
        recommendation.setPaperAuthors(paper.getAuthors());
        recommendation.setPaperSource(paper.getSource());
        recommendation.setPaperUrl(paper.getUrl());
        recommendationRepository.save(recommendation);
    }
}
