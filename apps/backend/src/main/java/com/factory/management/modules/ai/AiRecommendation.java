package com.factory.management.modules.ai;

public record AiRecommendation(
        String priority,
        String title,
        String action,
        String evidence,
        String expectedImpact,
        boolean estimated
) {
}
