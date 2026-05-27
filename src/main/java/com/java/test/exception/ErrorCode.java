package com.java.test.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 토큰 관련
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED,       "TOKEN_MISSING",         "토큰이 없습니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,       "TOKEN_EXPIRED",         "토큰이 만료되었습니다"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED,       "TOKEN_INVALID",         "유효하지 않은 토큰입니다"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다. 다시 로그인하세요"),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED,      "TOKEN_MISMATCH",        "토큰이 일치하지 않습니다"),

    // 인증/인가
    MISSING_FIELD(HttpStatus.BAD_REQUEST,        "MISSING_FIELD",         "아이디/비밀번호 입력 필요"),
    MISSING_TOKEN(HttpStatus.BAD_REQUEST,        "MISSING_TOKEN",         "리프레시 토큰이 필요합니다"),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED,      "USER_NOT_FOUND",        "유저를 찾을 수 없습니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED,    "INVALID_PASSWORD",      "비밀번호가 틀렸습니다"),
    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED,   "NOT_AUTHENTICATED",     "인증되지 않았습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN,              "FORBIDDEN",             "접근 권한이 없습니다"),

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST,        "INVALID_INPUT",         "잘못된 입력입니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",    "서버 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public int getStatus() { return httpStatus.value(); }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
