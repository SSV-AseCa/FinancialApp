import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@ssv/ui-core';

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const auth = useAuth();
  const location = useLocation();

  if (!auth.isAuthenticated()) {
    // Redirect them to the /login page and preserve the current location in
    // router state. The login flow currently does not read this state, so
    // users will continue to land on the default post-login page.
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
}
