package com.eventmanagement.EventManagement.security;

import com.eventmanagement.EventManagement.configuration.CacheRepository;
import com.eventmanagement.EventManagement.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Component
@NoArgsConstructor
@Slf4j
public class TokenFilter extends OncePerRequestFilter {

    private TokenProvider tokenProvider;

    private CacheRepository cacheRepository;

    @Autowired
    public TokenFilter(TokenProvider tokenProvider, CacheRepository cacheRepository) {
        this.tokenProvider = tokenProvider;
        this.cacheRepository = cacheRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String token = tokenProvider.resolveToken(httpServletRequest);
        if(token == null || httpServletRequest.getRequestURI().equals("/api/signup") || httpServletRequest.getRequestURI().startsWith("/api/login") || httpServletRequest.getRequestURI().startsWith("/api/refreshToken")){
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }else {
            try{
                if (token != null && tokenProvider.validateToken(token)) {
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }else {
                    log.warn("Token is either null or not valid. Request URI: {}", httpServletRequest.getRequestURI());
                    throw new CustomException("Token is invalid or has expired", HttpStatus.UNAUTHORIZED);
                }
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            } catch (CustomException e) {
                log.error("Custom exception occurred during token validation: {}", e.getMessage(), e);
                handleErrorResponse(httpServletResponse, e.getMessage(), e.getHttpStatus());
            } catch (Exception e) {
                log.error("An unexpected error occurred during token validation: {}", e.getMessage(), e);
                handleErrorResponse(httpServletResponse, "An unexpected error occurred while processing the request", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    }


    private void handleErrorResponse(HttpServletResponse httpServletResponse, String errorMessage, HttpStatus status) throws IOException {
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setStatus(status.value());

        // Construct a custom error response body
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", errorMessage);
        errorResponse.put("status", status.value());

        // Write the error response as JSON
        try (PrintWriter writer = httpServletResponse.getWriter()) {
            writer.write(new ObjectMapper().writeValueAsString(errorResponse));
            writer.flush();
        }
    }
}

