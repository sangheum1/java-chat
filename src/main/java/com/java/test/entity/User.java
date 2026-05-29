package com.java.test.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // 👉 DB 테이블명
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String refreshToken;

    // 로그인 제공자: "local"(일반) 또는 "kakao"
    private String provider;

    // 카카오 사용자 고유 ID (카카오 로그인 시 저장)
    private String providerId;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
