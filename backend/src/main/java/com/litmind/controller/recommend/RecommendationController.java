package com.litmind.controller.recommend;

import com.litmind.common.response.ApiResponse;
import com.litmind.common.util.SecurityUtil;
import com.litmind.model.entity.Recommendation;
import com.litmind.service.recommend.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ApiResponse<List<Recommendation>> getRecommendations(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<Recommendation> recommendations = recommendationService.getUserRecommendations(userId);
        return ApiResponse.success(recommendations);
    }

    @PostMapping("/generate")
    public ApiResponse<Void> generateRecommendations(Authentication authentication) {
        Long userId = getUserId(authentication);
        recommendationService.generateRecommendations(userId);
        return ApiResponse.success("推荐生成成功", null);
    }

    @PutMapping("/{id}/feedback")
    public ApiResponse<Void> updateFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        String feedback = request.get("feedback");
        recommendationService.updateRecommendationFeedback(userId, id, feedback);
        return ApiResponse.success("反馈更新成功", null);
    }

    private Long getUserId(Authentication authentication) {
        Long userId = securityUtil.getUserId(authentication);
        if (userId == null) {
            throw new RuntimeException("无法获取用户ID");
        }
        return userId;
    }
}

