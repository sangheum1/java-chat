package com.java.test.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.test.dto.ApiResponse;
import com.java.test.dto.UserInfo;
import com.java.test.exception.ErrorCode;
import com.java.test.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            // 토큰 없음: Spring Security의 AuthenticationEntryPoint에 위임
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        ErrorCode tokenState = jwtUtil.validateTokenState(token);

        if (tokenState != null) {
            // 만료 또는 위변조 토큰: 즉시 JSON 응답 반환 (filterChain 진행 안 함)
            writeErrorResponse(response, tokenState);
            return;
        }

        // 유효한 토큰: SecurityContext에 인증 정보 설정
        UserInfo user = jwtUtil.getUserInfo(token);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponse.error(errorCode))
        );
    }
}
