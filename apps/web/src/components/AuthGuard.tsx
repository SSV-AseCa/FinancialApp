import { type ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@ssv/ui-core';

export default function AuthGuard({ children }: { children: ReactNode }) {
  const auth = useAuth();

  if (!auth.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
