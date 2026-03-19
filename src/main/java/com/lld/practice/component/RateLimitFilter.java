package com.lld.practice.component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Component
public class RateLimitFilter implements Filter {
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(requestCounts::clear, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String userId = request.getHeader("user-id");
        if (request.getRequestURI().contains("hello") && userId != null) {
            int count = requestCounts.merge(userId, 1, Integer::sum);
            if (count > 10) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded");
                return; // Stop the request
            }
        }
        chain.doFilter(req, res); // Continue to the RestController
    }
}
