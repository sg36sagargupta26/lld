package com.lld.practice.component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final int MAX_REQUESTS = 10;

    public RateLimitInterceptor() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(requestCounts::clear, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        String userId = request.getHeader("user-id");

        if (uri.contains("hello") && userId != null) {
            int currentCount = requestCounts.merge(userId, 1, Integer::sum);
            if (currentCount > MAX_REQUESTS) {
                // Return 429 Too Many Requests
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                return false; // Stop the request here
            }
        }
        return true; // Continue to the Controller
    }
}
