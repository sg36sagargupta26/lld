package com.lld.practice.component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Component
public class RateLimitFilter implements Filter {
    private final Map<String, UserData> userCache = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SIZE_MS = 60_000;
    static class UserData {
        final ArrayDeque<Long> timestamps = new ArrayDeque<>(MAX_REQUESTS);
    }
    public RateLimitFilter() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long threshold = System.currentTimeMillis() - WINDOW_SIZE_MS;
            userCache.entrySet().removeIf(entry -> {
                UserData data = entry.getValue();
                synchronized (data.timestamps) {
                    while (!data.timestamps.isEmpty() && data.timestamps.peekFirst() < threshold) {
                        data.timestamps.pollFirst();
                    }
                    return data.timestamps.isEmpty();
                }
            });
        }, 5, 5, TimeUnit.MINUTES);
    }
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String userId = request.getHeader("user-id");
        String uri = request.getRequestURI();
        if (uri.contains("hello") && userId != null && isNotAllowed(userId)) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return;
        }
        chain.doFilter(req, res);
    }
    private boolean isNotAllowed(String userId) {
        long now = System.currentTimeMillis();
        UserData data = userCache.computeIfAbsent(userId, _ -> new UserData());
        synchronized (data.timestamps) {
            while (!data.timestamps.isEmpty() && data.timestamps.peekFirst() < (now - WINDOW_SIZE_MS)) {
                data.timestamps.pollFirst();
            }
            if (data.timestamps.size() >= MAX_REQUESTS) {
                return true;
            }
            data.timestamps.addLast(now);
            return false;
        }
    }
}
