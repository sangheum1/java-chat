import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import apiClient from '../api/apiClient';
import { tokenManager } from '../utils/tokenManager';

export function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  // 카카오 콜백 처리: URL 파라미터에 토큰이 있으면 저장 후 이동
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const accessToken = params.get('accessToken');
    const refreshToken = params.get('refreshToken');
    const kakaoError = params.get('error');

    if (accessToken && refreshToken) {
      tokenManager.setTokens(accessToken, refreshToken);
      navigate(from, { replace: true });
    } else if (kakaoError) {
      setError(kakaoError);
    }
  }, []);

  const handleKakaoLogin = async () => {
    try {
      const res = await apiClient.get('/auth/kakaoAuthUrl');
      // 프록시를 거치지 않고 브라우저가 카카오로 직접 이동
      window.location.href = res.data.data.url;
    } catch {
      setError('카카오 로그인을 시작할 수 없습니다');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await apiClient.post('/auth/loginProc', { username, password });
      const { accessToken, refreshToken } = response.data.data;

      tokenManager.setTokens(accessToken, refreshToken);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || '로그인에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '80px auto', padding: '0 16px' }}>
      <h1>로그인</h1>
      {error && (
        <p style={{ color: '#e53e3e', background: '#fff5f5', padding: '10px', borderRadius: '4px' }}>
          {error}
        </p>
      )}
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        <div>
          <label htmlFor="username" style={{ display: 'block', marginBottom: '4px' }}>아이디</label>
          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
          />
        </div>
        <div>
          <label htmlFor="password" style={{ display: 'block', marginBottom: '4px' }}>비밀번호</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          style={{ padding: '10px', cursor: loading ? 'not-allowed' : 'pointer' }}
        >
          {loading ? '로그인 중...' : '로그인'}
        </button>
      </form>
      <div style={{ display: 'flex', alignItems: 'center', margin: '16px 0', gap: '8px' }}>
        <hr style={{ flex: 1, borderColor: '#e2e8f0' }} />
        <span style={{ color: '#a0aec0', fontSize: '14px' }}>또는</span>
        <hr style={{ flex: 1, borderColor: '#e2e8f0' }} />
      </div>
      <button
        onClick={handleKakaoLogin}
        style={{
          width: '100%',
          padding: '10px',
          backgroundColor: '#FEE500',
          color: '#000',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
          fontWeight: 'bold',
          fontSize: '15px',
        }}
      >
        카카오로 로그인
      </button>
    </div>
  );
}
