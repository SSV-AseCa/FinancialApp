import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@ssv/ui-core';

export default function CallbackPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const handled = useRef(false);

  useEffect(() => {
    if (handled.current) return;
    handled.current = true;

    auth
      .handleCallback(window.location.href)
      .then(() => navigate('/portfolio', { replace: true }))
      .catch((err) => {
        console.error('Auth callback failed', err);
        navigate('/register', { replace: true });
      });
  }, [auth, navigate]);

  return (
    <div className="min-h-screen bg-background flex items-center justify-center">
      <p className="text-muted-foreground text-lg">Completing sign-in…</p>
    </div>
  );
}
