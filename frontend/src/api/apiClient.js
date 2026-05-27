import axios from 'axios';
import { tokenManager } from '../utils/tokenManager';

const apiClient = axios.create({
  timeout: 10000,
});

// 요청 인터셉터: 모든 요청에 accessToken 자동 첨부
apiClient.interceptors.request.use(
  (config) => {
    const token = tokenManager.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

function dispatchApiError(message) {
  window.dispatchEvent(new CustomEvent('api-error', { detail: { message } }));
}

// 응답 인터셉터: 백엔드 에러 코드 기반으로 상황별 분기 처리
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;
    const errorCode = error.response?.data?.code;
    const errorMessage = error.response?.data?.message;

    if (status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      // 위변조 토큰: refresh 없이 즉시 로그아웃 (불필요한 네트워크 요청 방지)
      if (errorCode === 'TOKEN_INVALID') {
        tokenManager.clearTokens();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      // 리프레시 토큰 자체가 만료: 재로그인 필요
      if (errorCode === 'REFRESH_TOKEN_EXPIRED') {
        tokenManager.clearTokens();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      // 액세스 토큰 만료 또는 토큰 없음: refreshToken으로 재발급 시도
      if (errorCode === 'TOKEN_EXPIRED' || errorCode === 'TOKEN_MISSING') {
        const refreshToken = tokenManager.getRefreshToken();

        if (!refreshToken) {
          tokenManager.clearTokens();
          window.location.href = '/login';
          return Promise.reject(error);
        }

        try {
          const res = await axios.post('/auth/refresh', { refreshToken });
          const { accessToken: newAccessToken } = res.data.data;

          tokenManager.setTokens(newAccessToken, refreshToken);
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

          return apiClient(originalRequest);
        } catch {
          tokenManager.clearTokens();
          window.location.href = '/login';
          return Promise.reject(error);
        }
      }
    }

    // 권한 없음: 서버 메시지 표시 후 홈으로 이동
    if (status === 403) {
      dispatchApiError(errorMessage || '접근 권한이 없습니다');
      window.location.href = '/';
      return Promise.reject(error);
    }

    // 서버 오류: 서버 메시지 표시, 현재 페이지 유지
    if (status === 500) {
      dispatchApiError(errorMessage || '서버 오류가 발생했습니다');
      return Promise.reject(error);
    }

    return Promise.reject(error);
  }
);

export default apiClient;
