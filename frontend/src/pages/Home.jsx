import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient';
import { tokenManager } from '../utils/tokenManager';

export function Home() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    apiClient.get('/auth/me')
      .then((res) => setUser(res.data.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleLogout = async () => {
    try {
      await apiClient.post('/auth/logout');
    } catch {
      // 서버 오류 여부와 무관하게 클라이언트 토큰은 삭제
    } finally {
      tokenManager.clearTokens();
      navigate('/login');
    }
  };

  if (loading) return <p>로딩 중...</p>;

  return (
    <div style={{ maxWidth: '600px', margin: '80px auto', padding: '0 16px' }}>
      <h1>홈</h1>
      {user && (
        <div style={{ marginBottom: '24px' }}>
          <p>환영합니다, <strong>{user.username}</strong>님!</p>
          <p style={{ color: '#666', fontSize: '14px' }}>사용자 ID: {user.id}</p>
        </div>
      )}
      <button onClick={handleLogout} style={{ padding: '8px 16px', cursor: 'pointer' }}>
        로그아웃
      </button>
    </div>
  );
}
