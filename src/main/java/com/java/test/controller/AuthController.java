package com.java.test.controller;

import com.java.test.dto.ApiResponse;
import com.java.test.dto.UserInfo;
import com.java.test.entity.User;
import com.java.test.exception.ErrorCode;
import com.java.test.service.UserService;
import com.java.test.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil, UserService userService, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/loginProc")
    public ResponseEntity<?> loginProc(@RequestBody Map<String, Object> req) {
        String username = req.get("username") != null ? req.get("username").toString() : "";
        String password = req.get("password") != null ? req.get("password").toString() : "";

        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(ErrorCode.MISSING_FIELD.getHttpStatus())
                    .body(ApiResponse.error(ErrorCode.MISSING_FIELD));
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(ErrorCode.USER_NOT_FOUND.getHttpStatus())
                    .body(ApiResponse.error(ErrorCode.USER_NOT_FOUND));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(ErrorCode.INVALID_PASSWORD.getHttpStatus())
                    .body(ApiResponse.error(ErrorCode.INVALID_PASSWORD));
        }

        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);
        userService.updateRefreshToken(user, refreshToken);

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        )));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> req) {
        String refreshToken = req.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(ErrorCode.MISSING_TOKEN.getHttpStatus())
                    .body(ApiResponse.error(ErrorCode.MISSING_TOKEN));
        }

        ErrorCode tokenState = jwtUtil.validateTokenState(refreshToken);
        if (tokenState == ErrorCode.TOKEN_EXPIRED) {
            return ResponseEntity.status(ErrorCode.REFRESH_TOKEN_EXPIRED.getHttpStatus())
                    .body(ApiResponse.error(ErrorCode.REFRESH_TOKEN_EXPIRED));
        }
        if (tokenState == ErrorCode.TOKEN_INVALID) {
            return ResponseEntity.status(ErrorCode.TOKEN_INVALID.getHttpStatus())
                    .body(ApiResponse.error(ErrorCode.TOKEN_INVALID));
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        User user = userService.findById(userId);
        if (user == null || !refreshToken.equals(user.getRefreshToken())) {
            return ResponseEntity.status(ErrorCode.TOKEN_MISMATCH.getHttpStatus())
                    .body(ApiResponse.error(ErrorCode.TOKEN_MISMATCH));
        }

        String newAccessToken = jwtUtil.createAccessToken(user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("accessToken", newAccessToken)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserInfo principal) {
        if (principal == null) {
            return ResponseEntity.status(ErrorCode.NOT_AUTHENTICATED.getHttpStatus())
                    .body(ApiResponse.error(ErrorCode.NOT_AUTHENTICATED));
        }

        User user = userService.findById(principal.getId());
        if (user != null) {
            userService.clearRefreshToken(user);
        }

        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal UserInfo principal) {
        if (principal == null) {
            return ResponseEntity.status(ErrorCode.NOT_AUTHENTICATED.getHttpStatus())
                    .body(ApiResponse.error(ErrorCode.NOT_AUTHENTICATED));
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "id", principal.getId(),
                "username", principal.getUsername()
        )));
    }
}
