import { useEffect, useState } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import axios from 'axios';
import { tokenManager } from '../utils/tokenManager';

export function ProtectedRoute() {
  const location = useLocation();
  // 'checking': 토큰 검증 중, 'ok': 통과, 'redirect': 로그인 페이지로
  const [authState, setAuthState] = useState('checking');

  useEffect(() => {
    const accessToken = tokenManager.getAccessToken();

    // 토큰 자체가 없으면 바로 로그인 페이지로
    if (!accessToken) {
      setAuthState('redirect');
      return;
    }

    // 토큰 형식이 잘못됐으면 바로 로그인 페이지로
    if (tokenManager.isAccessTokenMalformed()) {
      tokenManager.clearTokens();
      setAuthState('redirect');
      return;
    }

    // 아직 유효한 토큰이면 바로 통과
    if (!tokenManager.isAccessTokenExpired()) {
      setAuthState('ok');
      return;
    }

    // 만료된 accessToken: refreshToken으로 silent refresh 시도
    const refreshToken = tokenManager.getRefreshToken();
    if (!refreshToken) {
      tokenManager.clearTokens();
      setAuthState('redirect');
      return;
    }

    axios.post('/auth/refresh', { refreshToken })
      .then((res) => {
        tokenManager.setTokens(res.data.data.accessToken, refreshToken);
        setAuthState('ok');
      })
      .catch(() => {
        tokenManager.clearTokens();
        setAuthState('redirect');
      });
  }, []);

  if (authState === 'checking') return null;
  if (authState === 'redirect') return <Navigate to="/login" state={{ from: location }} replace />;
  return <Outlet />;
}
