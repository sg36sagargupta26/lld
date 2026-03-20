package com.lld.practice.component;

import com.lld.practice.repository.UserRateLimitRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {
    private static final int MAX_REQUESTS = 10;
    private final UserRateLimitRepository userRateLimitRepository;
    public RateLimitFilter(final UserRateLimitRepository userRateLimitRepository){
        this.userRateLimitRepository = userRateLimitRepository;
    }
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String userId = request.getHeader("user-id");
        String uri = request.getRequestURI();
        if (uri.contains("hello") && userId != null && userRateLimitRepository.isNotAllowed(userId,MAX_REQUESTS)) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return;
        }
        chain.doFilter(req, res);
    }

}
