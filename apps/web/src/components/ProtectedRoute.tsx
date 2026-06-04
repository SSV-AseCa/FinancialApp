import { Navigate } from "react-router-dom";
import { useAuth } from "@ssv/ui-core";
import type { ReactNode } from "react";

interface ProtectedRouteProps {
  children: ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const auth = useAuth();
  if (!auth.isAuthenticated()) {
    return <Navigate to="/register" replace />;
  }
  return <>{children}</>;
}
