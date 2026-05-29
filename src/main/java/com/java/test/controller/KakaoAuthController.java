package com.java.test.controller;

import com.java.test.dto.ApiResponse;
import com.java.test.entity.User;
import com.java.test.service.KakaoOAuthService;
import com.java.test.service.UserService;
import com.java.test.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class KakaoAuthController {

    private static final Logger log = LoggerFactory.getLogger(KakaoAuthController.class);

    private final KakaoOAuthService kakaoOAuthService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public KakaoAuthController(KakaoOAuthService kakaoOAuthService,
                               UserService userService,
                               JwtUtil jwtUtil) {
        this.kakaoOAuthService = kakaoOAuthService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // 카카오 인증 URL을 JSON으로 반환 (프론트에서 직접 이동)
    @GetMapping("/kakaoAuthUrl")
    public ResponseEntity<?> getKakaoAuthUrl() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("url", kakaoOAuthService.getAuthorizationUrl())
        ));
    }

    // 카카오 로그인 페이지로 리다이렉트 (직접 URL 입력용으로 유지)
    @GetMapping("/kakaoLogin")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(kakaoOAuthService.getAuthorizationUrl());
    }

    // 카카오 인가코드 콜백 처리 → 내부 JWT 발급 → 프론트엔드로 리다이렉트
    @GetMapping("/kakaoCallback")
    public void kakaoCallback(@RequestParam String code,
                              HttpServletResponse response) throws IOException {
        try {
            // 인가코드 → 카카오 액세스 토큰
            String kakaoAccessToken = kakaoOAuthService.getKakaoAccessToken(code);

            // 카카오 액세스 토큰 → 사용자 정보
            KakaoOAuthService.KakaoUserInfo userInfo =
                    kakaoOAuthService.getKakaoUserInfo(kakaoAccessToken);

            // DB 조회 또는 자동 신규 가입
            User user = userService.findOrCreateKakaoUser(
                    userInfo.kakaoId(), userInfo.nickname());

            // 내부 JWT 발급
            String accessToken = jwtUtil.createAccessToken(user);
            String refreshToken = jwtUtil.createRefreshToken(user);
            userService.updateRefreshToken(user, refreshToken);

            // 프론트엔드 로그인 페이지로 토큰과 함께 리다이렉트
            response.sendRedirect("http://localhost:3000/login"
                    + "?accessToken=" + accessToken
                    + "&refreshToken=" + refreshToken);

        } catch (Exception e) {
            log.error("[카카오 콜백] 처리 중 오류 발생 - message: {}", e.getMessage(), e);
            // 카카오 로그인 실패 시 에러 메시지와 함께 로그인 페이지로 이동
            response.sendRedirect("http://localhost:3000/login?error=카카오 로그인에 실패했습니다");
        }
    }
}
