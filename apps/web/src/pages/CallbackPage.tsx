import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@ssv/ui-core';
import { Spinner } from '../components/ui/Spinner';

export default function CallbackPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const handled = useRef(false);

  useEffect(() => {
    if (handled.current) return;
    handled.current = true;

    const params = new URLSearchParams(window.location.search);
    if (!params.get('code')) {
      // No Auth0 code present — mock/dev mode, skip callback handling
      navigate('/login', { replace: true });
      return;
    }

    auth
      .handleCallback(window.location.href)
      .then(() => navigate('/portfolio', { replace: true }))
      .catch((err) => {
        console.error('Auth callback failed', err);
        navigate('/login', { 
          replace: true, 
          state: { error: err instanceof Error ? err.message : 'Callback processing failed' } 
        });
      });
  }, [auth, navigate]);

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4 sm:p-8 relative overflow-hidden">
      {/* Background radial ambient glows */}
      <div className="pointer-events-none absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary/20 rounded-full blur-[120px]" />
      <div className="pointer-events-none absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-ring/20 rounded-full blur-[120px]" />

      <div className="w-full max-w-md relative z-10 flex flex-col items-center gap-6 text-center">
        <Spinner size="lg" />
        <p className="text-muted-foreground text-lg animate-pulse">
          Completing sign-in…
        </p>
      </div>
    </div>
  );
}

