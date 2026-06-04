import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "@ssv/ui-core";

export default function AuthGuard({ children }: { children: ReactNode }) {
  const auth = useAuth();
  const location = useLocation();

  if (!auth.isAuthenticated()) {
    // Saves the attempted location so a future "return to" redirect can be
    // implemented in CallbackPage without changing this guard.
    // Currently CallbackPage always navigates to /portfolio unconditionally.
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
}
