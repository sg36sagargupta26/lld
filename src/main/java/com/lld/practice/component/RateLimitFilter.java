package com.lld.practice.component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter implements Filter {
    private final Map<String, List<Long>> userCache = new ConcurrentHashMap<>();
    private final int MAX_REQUESTS = 10;
    private final long WINDOW_SIZE_MS = 60_000; // 1 minute

    public RateLimitFilter() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long threshold = System.currentTimeMillis() - WINDOW_SIZE_MS;
            userCache.entrySet().removeIf(entry -> {
                List<Long> timestamps = entry.getValue();
                timestamps.removeIf(t -> t < threshold);
                return timestamps.isEmpty();
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

        if (uri.contains("hello") && userId != null) {
            long now = System.currentTimeMillis();
            List<Long> timestamps = userCache.computeIfAbsent(userId, _ -> new CopyOnWriteArrayList<>());
            timestamps.removeIf(t -> t < (now - WINDOW_SIZE_MS));
            timestamps.add(now);
            if (timestamps.size() >= MAX_REQUESTS) {
                response.setStatus(429);
                response.getWriter().write("Rate limit exceeded. Try again later.");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
