package com.factory.management.ai;

import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class AiRateLimitService {
    private static final long WINDOW_SECONDS = 60;

    private final AiProperties properties;
    private final ConcurrentHashMap<Long, Window> windows = new ConcurrentHashMap<>();

    public void check(Long userId) {
        int limit = Math.max(1, properties.getRequestsPerMinute());
        long now = Instant.now().getEpochSecond();
        AtomicBoolean allowed = new AtomicBoolean(true);
        windows.compute(userId, (ignored, current) -> {
            if (current == null || now - current.startedAt >= WINDOW_SECONDS) {
                return new Window(now, 1);
            }
            if (current.count >= limit) {
                allowed.set(false);
                return current;
            }
            return new Window(current.startedAt, current.count + 1);
        });
        if (!allowed.get()) throw new AppException(ErrorCode.AI_RATE_LIMIT_EXCEEDED);
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt >= WINDOW_SECONDS);
        }
    }

    private record Window(long startedAt, int count) {
    }
}
