package com.java.test.service;

import com.java.test.entity.User;
import com.java.test.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public void updateRefreshToken(User user, String refreshToken) {
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
    }

    @Transactional
    public void clearRefreshToken(User user) {
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    // 카카오 사용자 조회, 없으면 자동 신규 생성 (최초 카카오 로그인 = 자동 회원가입)
    @Transactional
    public User findOrCreateKakaoUser(String kakaoId, String nickname) {
        return userRepository.findByProviderAndProviderId("kakao", kakaoId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername("kakao_" + kakaoId);
                    newUser.setProvider("kakao");
                    newUser.setProviderId(kakaoId);
                    return userRepository.save(newUser);
                });
    }
}
