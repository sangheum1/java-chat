import { Navigate, Outlet } from 'react-router-dom';
import { tokenManager } from '../utils/tokenManager';

export function GuestRoute() {
  if (tokenManager.hasTokens()) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}
