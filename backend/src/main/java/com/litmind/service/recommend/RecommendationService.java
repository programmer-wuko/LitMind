package com.litmind.service.recommend;

import com.litmind.model.entity.File;
import com.litmind.model.entity.PdfAnalysis;
import com.litmind.model.entity.Recommendation;
import com.litmind.model.entity.User;
import com.litmind.model.entity.UserBehavior;
import com.litmind.repository.FileRepository;
import com.litmind.repository.PdfAnalysisRepository;
import com.litmind.repository.RecommendationRepository;
import com.litmind.repository.UserBehaviorRepository;
import com.litmind.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserBehaviorRepository userBehaviorRepository;
    private final Optional<RedisTemplate<String, Object>> redisTemplate;
    private final FileRepository fileRepository;
    private final PdfAnalysisRepository pdfAnalysisRepository;
    private final UserRepository userRepository;
    private final ExternalPaperService externalPaperService;
    
    @Autowired
    public RecommendationService(
            RecommendationRepository recommendationRepository,
            UserBehaviorRepository userBehaviorRepository,
            Optional<RedisTemplate<String, Object>> redisTemplate,
            FileRepository fileRepository,
            PdfAnalysisRepository pdfAnalysisRepository,
            UserRepository userRepository,
            ExternalPaperService externalPaperService) {
        this.recommendationRepository = recommendationRepository;
        this.userBehaviorRepository = userBehaviorRepository;
        this.redisTemplate = redisTemplate;
        this.fileRepository = fileRepository;
        this.pdfAnalysisRepository = pdfAnalysisRepository;
        this.userRepository = userRepository;
        this.externalPaperService = externalPaperService;
    }

    public List<Recommendation> getUserRecommendations(Long userId) {
        String cacheKey = "recommendations:user:" + userId;
        
        // 尝试从Redis获取缓存
        if (redisTemplate.isPresent()) {
            @SuppressWarnings("unchecked")
            List<Recommendation> cached = (List<Recommendation>) redisTemplate.get().opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        // 从数据库获取推荐
        List<Recommendation> recommendations = recommendationRepository.findByUserIdOrderByRecommendationScoreDesc(userId);
        
        // 如果Redis可用，缓存1小时
        redisTemplate.ifPresent(rt -> {
            try {
                rt.opsForValue().set(cacheKey, recommendations, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("Redis缓存失败: {}", e.getMessage());
            }
        });
        
        return recommendations;
    }

    public void generateRecommendations(Long userId) {
        log.info("开始为用户 {} 生成推荐", userId);
        
        // 删除旧的推荐记录
        List<Recommendation> oldRecommendations = recommendationRepository.findByUserIdOrderByRecommendationScoreDesc(userId);
        if (!oldRecommendations.isEmpty()) {
            recommendationRepository.deleteAll(oldRecommendations);
            log.info("删除用户 {} 的旧推荐记录 {} 条", userId, oldRecommendations.size());
        }
        
        // 获取用户行为数据
        List<UserBehavior> behaviors = userBehaviorRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // 基于用户行为生成推荐
        List<Recommendation> recommendations = calculateRecommendations(userId, behaviors);
        
        // 保存推荐结果
        if (!recommendations.isEmpty()) {
            recommendationRepository.saveAll(recommendations);
            log.info("为用户 {} 生成了 {} 条新推荐", userId, recommendations.size());
        } else {
            log.info("为用户 {} 未生成推荐（可能没有足够的候选文件）", userId);
        }
        
        // 清除缓存
        redisTemplate.ifPresent(rt -> {
            try {
                String cacheKey = "recommendations:user:" + userId;
                rt.delete(cacheKey);
            } catch (Exception e) {
                log.warn("清除Redis缓存失败: {}", e.getMessage());
            }
        });
    }

    public void recordUserBehavior(Long userId, Long fileId, String behaviorType, String behaviorData) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setFileId(fileId);
        behavior.setBehaviorType(behaviorType);
        behavior.setBehaviorData(behaviorData);
        userBehaviorRepository.save(behavior);
    }

    public void updateRecommendationFeedback(Long userId, Long recommendationId, String feedback) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("推荐记录不存在"));
        
        if (!recommendation.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该推荐记录");
        }
        
        recommendation.setFeedback(feedback);
        recommendationRepository.save(recommendation);
    }

    private List<Recommendation> calculateRecommendations(Long userId, List<UserBehavior> behaviors) {
        // 策略1：优先推荐与用户上传的PDF相似的外部学术论文
        List<File> userUploadedFiles = fileRepository.findByUserId(userId)
                .stream()
                .filter(f -> isPdfFile(f))
                .collect(Collectors.toList());
        
        if (!userUploadedFiles.isEmpty()) {
            log.info("用户 {} 上传了 {} 个PDF文件，基于上传文件搜索外部学术论文", userId, userUploadedFiles.size());
            List<Recommendation> similarRecommendations = recommendExternalPapersFromUserFiles(userId, userUploadedFiles);
            if (!similarRecommendations.isEmpty()) {
                log.info("基于用户上传文件成功生成 {} 条外部论文推荐", similarRecommendations.size());
                return similarRecommendations;
            }
            log.info("基于用户上传文件未找到外部论文推荐（可能PDF分析未完成或外部API失败），降级到推荐热门论文");
        }
        

        log.info("推荐热门外部学术论文");
        List<Recommendation> hotExternalRecommendations = recommendHotExternalPapers(userId);
        
        // 如果外部论文推荐失败或为空，确保至少推荐系统内的文件
        if (hotExternalRecommendations.isEmpty()) {
            log.info("外部论文推荐为空，降级到推荐系统内文件");
            List<Recommendation> systemRecommendations = recommendHotFiles(userId);
            if (!systemRecommendations.isEmpty()) {
                log.info("成功生成 {} 条系统内文件推荐", systemRecommendations.size());
                return systemRecommendations;
            }
            log.warn("系统内也没有可推荐的文件");
        }
        
        return hotExternalRecommendations;
    }
    
    /**
     * 基于用户上传的文件推荐外部学术论文
     */
    private List<Recommendation> recommendExternalPapersFromUserFiles(Long userId, List<File> userFiles) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // 提取用户上传文件的主题关键词
        Set<Long> userFileIds = userFiles.stream().map(File::getId).collect(Collectors.toSet());
        Map<Long, String> userFileTopics = extractTopicsFromFiles(userFileIds);
        
        if (userFileTopics.isEmpty()) {
            log.warn("无法提取用户上传文件的主题（可能PDF分析未完成或分析失败）");
            return recommendations;
        }
        
        log.info("成功提取了 {} 个用户文件的主题，开始搜索外部学术论文", userFileTopics.size());
        
        // 合并所有文件的主题文本，提取搜索关键词
        String combinedTopics = userFileTopics.values().stream()
                .collect(Collectors.joining(" "));
        String searchKeywords = externalPaperService.extractSearchKeywords(combinedTopics);
        
        if (searchKeywords == null || searchKeywords.trim().isEmpty()) {
            log.warn("无法从PDF分析结果中提取搜索关键词");
            return recommendations;
        }
        
        log.info("提取的搜索关键词: {}", searchKeywords);
        
        try {
            // 从arXiv搜索论文
            List<ExternalPaperService.PaperInfo> arxivPapers = externalPaperService.searchArxivPapers(searchKeywords, 10);
            
            // 从Semantic Scholar搜索论文
            List<ExternalPaperService.PaperInfo> semanticPapers = externalPaperService.searchSemanticScholarPapers(searchKeywords, 10);
            
            // 合并结果，去重（基于标题）
            Set<String> seenTitles = new HashSet<>();
            List<ExternalPaperService.PaperInfo> allPapers = new ArrayList<>();
            
            for (ExternalPaperService.PaperInfo paper : arxivPapers) {
                if (paper.getTitle() != null && !seenTitles.contains(paper.getTitle())) {
                    allPapers.add(paper);
                    seenTitles.add(paper.getTitle());
                }
            }
            
            for (ExternalPaperService.PaperInfo paper : semanticPapers) {
                if (paper.getTitle() != null && !seenTitles.contains(paper.getTitle())) {
                    allPapers.add(paper);
                    seenTitles.add(paper.getTitle());
                }
            }
            
            // 生成推荐记录（最多10个）
            int count = 0;
            for (ExternalPaperService.PaperInfo paper : allPapers) {
                if (count >= 10) {
                    break;
                }
                
                Recommendation rec = new Recommendation();
                rec.setUserId(userId);
                rec.setExternalPaperId(paper.getExternalPaperId());
                rec.setPaperTitle(paper.getTitle());
                rec.setPaperAuthors(paper.getAuthors());
                rec.setPaperSource(paper.getSource());
                rec.setPaperUrl(paper.getUrl());
                rec.setRecommendationReason("基于您上传的PDF文件主题推荐的相关学术论文");
                rec.setRecommendationScore(BigDecimal.valueOf(0.9 - count * 0.05));
                rec.setCreatedAt(LocalDateTime.now());
                recommendations.add(rec);
                count++;
            }
            
            log.info("基于用户上传文件生成了 {} 条外部学术论文推荐", recommendations.size());
        } catch (Exception e) {
            log.error("搜索外部论文失败: {}", e.getMessage(), e);
            // 如果搜索失败，返回空列表，让上层逻辑降级到热门论文推荐
        }
        
        return recommendations;
    }
    
    /**
     * 推荐热门外部学术论文
     */
    private List<Recommendation> recommendHotExternalPapers(Long userId) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        log.info("开始获取热门外部学术论文");
        
        try {
            // 从arXiv获取热门论文
            List<ExternalPaperService.PaperInfo> hotPapers = externalPaperService.getHotPapers(10);
            
            // 过滤掉无效的示例论文（externalPaperId以"example"开头的）
            List<ExternalPaperService.PaperInfo> validPapers = hotPapers.stream()
                    .filter(paper -> paper.getExternalPaperId() != null 
                            && !paper.getExternalPaperId().startsWith("example"))
                    .collect(Collectors.toList());
            
            if (validPapers.isEmpty()) {
                log.info("未获取到有效的外部论文，降级到推荐系统内文件");
                // 降级到推荐系统内的热门文件
                return recommendHotFiles(userId);
            }
            
            // 生成推荐记录
            for (int i = 0; i < validPapers.size(); i++) {
                ExternalPaperService.PaperInfo paper = validPapers.get(i);
                
                Recommendation rec = new Recommendation();
                rec.setUserId(userId);
                rec.setExternalPaperId(paper.getExternalPaperId());
                rec.setPaperTitle(paper.getTitle());
                rec.setPaperAuthors(paper.getAuthors());
                rec.setPaperSource(paper.getSource());
                rec.setPaperUrl(paper.getUrl());
                rec.setRecommendationReason("热门学术论文推荐");
                rec.setRecommendationScore(BigDecimal.valueOf(0.7 - i * 0.05));
                rec.setCreatedAt(LocalDateTime.now());
                recommendations.add(rec);
            }
            
            log.info("生成了 {} 条热门外部学术论文推荐", recommendations.size());
        } catch (Exception e) {
            log.error("获取热门论文失败: {}", e.getMessage(), e);
            // 外部API失败，降级到推荐系统内的热门文件
            log.info("外部API失败，降级到推荐系统内文件");
            return recommendHotFiles(userId);
        }
        
        return recommendations;
    }
    
    /**
     * 推荐同部门其他人的文件
     */
    private List<Recommendation> recommendDepartmentFiles(Long userId, List<File> departmentFiles) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // 统计每个文件的查看次数
        Map<Long, Long> viewCounts = new HashMap<>();
        for (File file : departmentFiles) {
            List<UserBehavior> fileBehaviors = userBehaviorRepository.findByFileId(file.getId());
            long count = fileBehaviors.stream()
                    .filter(b -> "VIEW".equals(b.getBehaviorType()) || "ANALYZE".equals(b.getBehaviorType()))
                    .count();
            viewCounts.put(file.getId(), count);
        }
        
        // 按查看次数排序，如果查看次数相同，按创建时间排序（新的优先）
        departmentFiles.sort((f1, f2) -> {
            Long count1 = viewCounts.getOrDefault(f1.getId(), 0L);
            Long count2 = viewCounts.getOrDefault(f2.getId(), 0L);
            int compare = Long.compare(count2, count1);
            if (compare != 0) {
                return compare;
            }
            // 查看次数相同，按创建时间降序（新的优先）
            return f2.getCreatedAt().compareTo(f1.getCreatedAt());
        });
        
        // 生成推荐（最多10个）
        for (int i = 0; i < Math.min(10, departmentFiles.size()); i++) {
            File file = departmentFiles.get(i);
            Recommendation rec = new Recommendation();
            rec.setUserId(userId);
            rec.setRecommendedFileId(file.getId());
            rec.setPaperTitle(file.getName());
            rec.setPaperSource("系统内文件");
            rec.setPaperUrl("/pdf/" + file.getId());
            rec.setRecommendationReason("同部门其他用户上传的文件");
            rec.setRecommendationScore(BigDecimal.valueOf(0.8 - i * 0.05));
            rec.setCreatedAt(LocalDateTime.now());
            recommendations.add(rec);
        }
        
        log.info("推荐了 {} 个同部门其他人的文件", recommendations.size());
        return recommendations;
    }
    
    /**
     * 推荐用户自己的其他文件
     */
    private List<Recommendation> recommendUserOwnFiles(Long userId, List<File> userFiles) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // 按创建时间排序（新的优先）
        userFiles.sort((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()));
        
        // 生成推荐
        for (int i = 0; i < Math.min(10, userFiles.size()); i++) {
            File file = userFiles.get(i);
            Recommendation rec = new Recommendation();
            rec.setUserId(userId);
            rec.setRecommendedFileId(file.getId());
            rec.setPaperTitle(file.getName());
            rec.setPaperSource("系统内文件");
            rec.setPaperUrl("/pdf/" + file.getId());
            rec.setRecommendationReason("您上传的其他文件");
            rec.setRecommendationScore(BigDecimal.valueOf(0.5 - i * 0.05));
            rec.setCreatedAt(LocalDateTime.now());
            recommendations.add(rec);
        }
        
        log.info("推荐了 {} 个用户自己的其他文件", recommendations.size());
        return recommendations;
    }
    
    /**
     * 推荐系统热门文件（跨部门，基于查看次数）
     */
    private List<Recommendation> recommendHotFiles(Long userId) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // 获取所有公共PDF文件（跨部门，包括用户自己的文件）
        List<File> publicFiles = fileRepository.findByIsPublicTrue()
                .stream()
                .filter(f -> isPdfFile(f))
                .collect(Collectors.toList());
        
        if (publicFiles.isEmpty()) {
            log.info("系统内没有公共PDF文件，尝试推荐用户自己的私有文件");
            // 如果没有公共文件，尝试推荐用户自己的所有文件（包括私有文件）
            List<File> userFiles = fileRepository.findByUserId(userId)
                    .stream()
                    .filter(f -> isPdfFile(f))
                    .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()))
                    .skip(1) // 跳过最新的文件
                    .limit(10)
                    .collect(Collectors.toList());
            
            if (!userFiles.isEmpty()) {
                log.info("推荐用户自己的 {} 个文件", userFiles.size());
                return recommendUserOwnFiles(userId, userFiles);
            }
            
            log.warn("系统内没有任何PDF文件可推荐");
            return recommendations;
        }
        
        // 如果只有用户自己的文件，也推荐（至少让用户能看到自己的文件）
        if (publicFiles.stream().allMatch(f -> f.getUserId().equals(userId))) {
            log.info("系统内只有用户自己的公共文件，推荐用户自己的其他文件");
            // 如果只有一个文件，也推荐它（不跳过）
            if (publicFiles.size() == 1) {
                File file = publicFiles.get(0);
                Recommendation rec = new Recommendation();
                rec.setUserId(userId);
                rec.setRecommendedFileId(file.getId());
                rec.setPaperTitle(file.getName());
                rec.setPaperSource("系统内文件");
                rec.setPaperUrl("/pdf/" + file.getId());
                rec.setRecommendationReason("您上传的文件");
                rec.setRecommendationScore(BigDecimal.valueOf(0.5));
                rec.setCreatedAt(LocalDateTime.now());
                recommendations.add(rec);
                return recommendations;
            }
            
            // 排除第一个文件（可能是刚上传的），推荐其他文件
            List<File> userOwnFiles = publicFiles.stream()
                    .filter(f -> f.getUserId().equals(userId))
                    .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()))
                    .skip(1) // 跳过最新的文件
                    .limit(10)
                    .collect(Collectors.toList());
            
            if (!userOwnFiles.isEmpty()) {
                return recommendUserOwnFiles(userId, userOwnFiles);
            }
        }
        
        // 排除用户自己的文件，推荐其他人的文件
        publicFiles = publicFiles.stream()
                .filter(f -> !f.getUserId().equals(userId))
                .collect(Collectors.toList());
        
        if (publicFiles.isEmpty()) {
            log.info("系统内没有其他用户的文件可推荐，推荐用户自己的文件");
            // 如果没有其他人的文件，推荐用户自己的文件
            List<File> userFiles = fileRepository.findByUserId(userId)
                    .stream()
                    .filter(f -> isPdfFile(f))
                    .sorted((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            if (!userFiles.isEmpty()) {
                return recommendUserOwnFiles(userId, userFiles);
            }
            
            return recommendations;
        }
        
        // 统计每个文件的查看次数（统计所有用户对该文件的查看）
        Map<Long, Long> viewCounts = new HashMap<>();
        for (File file : publicFiles) {
            List<UserBehavior> fileBehaviors = userBehaviorRepository.findByFileId(file.getId());
            long count = fileBehaviors.stream()
                    .filter(b -> "VIEW".equals(b.getBehaviorType()) || "ANALYZE".equals(b.getBehaviorType()))
                    .count();
            viewCounts.put(file.getId(), count);
        }
        
        // 按查看次数排序，如果查看次数相同，按创建时间排序（新的优先）
        publicFiles.sort((f1, f2) -> {
            Long count1 = viewCounts.getOrDefault(f1.getId(), 0L);
            Long count2 = viewCounts.getOrDefault(f2.getId(), 0L);
            int compare = Long.compare(count2, count1);
            if (compare != 0) {
                return compare;
            }
            // 查看次数相同，按创建时间降序（新的优先）
            return f2.getCreatedAt().compareTo(f1.getCreatedAt());
        });
        
        // 生成推荐（最多10个）
        for (int i = 0; i < Math.min(10, publicFiles.size()); i++) {
            File file = publicFiles.get(i);
            Recommendation rec = new Recommendation();
            rec.setUserId(userId);
            rec.setRecommendedFileId(file.getId());
            rec.setPaperTitle(file.getName());
            rec.setPaperSource("系统内文件");
            rec.setPaperUrl("/pdf/" + file.getId());
            rec.setRecommendationReason("系统热门文件推荐");
            rec.setRecommendationScore(BigDecimal.valueOf(0.6 - i * 0.05));
            rec.setCreatedAt(LocalDateTime.now());
            recommendations.add(rec);
        }
        
        log.info("推荐了 {} 个系统热门文件", recommendations.size());
        return recommendations;
    }
    
    /**
     * 从文件列表中提取主题关键词
     */
    private Map<Long, String> extractTopicsFromFiles(Set<Long> fileIds) {
        Map<Long, String> topics = new HashMap<>();
        
        for (Long fileId : fileIds) {
            Optional<PdfAnalysis> analysisOpt = pdfAnalysisRepository.findByFileId(fileId);
            if (analysisOpt.isPresent()) {
                PdfAnalysis analysis = analysisOpt.get();
                if ("COMPLETED".equals(analysis.getAnalysisStatus())) {
                    // 从分析结果中提取关键词（合并研究背景、核心内容等）
                    String topic = buildTopicString(analysis);
                    if (topic != null && !topic.isEmpty()) {
                        topics.put(fileId, topic);
                    }
                }
            }
        }
        
        return topics;
    }
    
    /**
     * 构建主题字符串（用于相似度计算）
     */
    private String buildTopicString(PdfAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        if (analysis.getResearchBackground() != null) {
            sb.append(analysis.getResearchBackground()).append(" ");
        }
        if (analysis.getCoreContent() != null) {
            sb.append(analysis.getCoreContent()).append(" ");
        }
        if (analysis.getExperimentResults() != null) {
            sb.append(analysis.getExperimentResults()).append(" ");
        }
        if (analysis.getAdditionalInfo() != null) {
            sb.append(analysis.getAdditionalInfo()).append(" ");
        }
        return sb.toString().trim();
    }
    
    /**
     * 计算候选文件与用户已查看文件的相似度分数
     */
    private BigDecimal calculateSimilarityScore(File candidateFile, Map<Long, String> userFileTopics) {
        Optional<PdfAnalysis> analysisOpt = pdfAnalysisRepository.findByFileId(candidateFile.getId());
        if (!analysisOpt.isPresent()) {
            return BigDecimal.ZERO;
        }
        
        PdfAnalysis candidateAnalysis = analysisOpt.get();
        if (!"COMPLETED".equals(candidateAnalysis.getAnalysisStatus())) {
            return BigDecimal.ZERO;
        }
        
        String candidateTopic = buildTopicString(candidateAnalysis);
        if (candidateTopic == null || candidateTopic.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 计算与用户已查看文件的相似度
        double maxSimilarity = 0.0;
        for (String userTopic : userFileTopics.values()) {
            double similarity = calculateTextSimilarity(candidateTopic, userTopic);
            maxSimilarity = Math.max(maxSimilarity, similarity);
        }
        
        return BigDecimal.valueOf(maxSimilarity);
    }
    
    /**
     * 计算两个文本的相似度（基于关键词重叠）
     */
    private double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }
        
        // 提取关键词（简单实现：去除停用词，提取重要词汇）
        Set<String> words1 = extractKeywords(text1);
        Set<String> words2 = extractKeywords(text2);
        
        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }
        
        // 计算Jaccard相似度
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * 从文本中提取关键词（简化实现）
     */
    private Set<String> extractKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        
        // 停用词列表（简化版）
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "的", "是", "在", "了", "和", "与", "或", "但", "而", "为", "以", "及",
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by"
        ));
        
        // 简单分词（按空格和标点分割）
        String[] words = text.toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .split("\\s+");
        
        for (String word : words) {
            word = word.trim();
            // 过滤停用词和短词
            if (word.length() > 2 && !stopWords.contains(word)) {
                keywords.add(word);
            }
        }
        
        return keywords;
    }
    
    /**
     * 判断文件是否为PDF文件
     */
    private boolean isPdfFile(File file) {
        if (file == null) {
            return false;
        }
        String fileType = file.getFileType();
        String mimeType = file.getMimeType();
        String fileName = file.getName();
        
        // 检查fileType或mimeType是否为application/pdf
        if (fileType != null && fileType.toLowerCase().contains("pdf")) {
            return true;
        }
        if (mimeType != null && mimeType.toLowerCase().contains("pdf")) {
            return true;
        }
        // 检查文件扩展名
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            return true;
        }
        return false;
    }
}

