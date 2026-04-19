package com.litmind.service.recommend;

import com.litmind.model.entity.Recommendation;

import java.util.List;

public interface RecommendationService {

    List<Recommendation> getUserRecommendations(Long userId);

    void generateRecommendations(Long userId);

    void recordUserBehavior(Long userId, Long fileId, String behaviorType, String behaviorData);

    void updateRecommendationFeedback(Long userId, Long recommendationId, String feedback);
}
