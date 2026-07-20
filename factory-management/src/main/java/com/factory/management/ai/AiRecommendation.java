package com.factory.management.ai;

public record AiRecommendation(
        String priority,
        String title,
        String action,
        String evidence,
        String expectedImpact,
        boolean estimated
) {
}
