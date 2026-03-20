package com.lld.practice.component;

import com.lld.practice.service.RateLimitService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {
    private final RateLimitService rateLimitService;
    public RateLimitFilter(final RateLimitService rateLimitService){
        this.rateLimitService = rateLimitService;
    }
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String userId = request.getHeader("user-id");
        String uri = request.getRequestURI();
        if (uri.contains("hello") && userId != null && rateLimitService.isNotAllowed(userId)) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return;
        }
        chain.doFilter(req, res);
    }

}
