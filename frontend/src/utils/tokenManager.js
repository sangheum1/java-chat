export const tokenManager = {
  setTokens: (accessToken, refreshToken) => {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  },

  getAccessToken: () => localStorage.getItem('accessToken'),
  getRefreshToken: () => localStorage.getItem('refreshToken'),

  clearTokens: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  },

  hasTokens: () => !!localStorage.getItem('accessToken'),

  // JWT payload를 서명 검증 없이 디코딩 (만료 여부 사전 판별용)
  parseToken: (token) => {
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch {
      return null;
    }
  },

  // accessToken의 만료 여부를 클라이언트에서 선제적으로 판별
  isAccessTokenExpired: () => {
    const token = localStorage.getItem('accessToken');
    if (!token) return true;
    const payload = tokenManager.parseToken(token);
    if (!payload?.exp) return true;
    // exp는 초 단위, Date.now()는 밀리초 단위
    return Date.now() >= payload.exp * 1000;
  },

  // JWT 구조(3파트) 여부로 형식 유효성을 빠르게 판별
  isAccessTokenMalformed: () => {
    const token = localStorage.getItem('accessToken');
    if (!token) return false;
    return token.split('.').length !== 3;
  },
};
