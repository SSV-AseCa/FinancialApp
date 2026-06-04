import { useCallback, useEffect, useState } from 'react';
import { usePortfolio, useAuth } from '@ssv/ui-core';
import type { Portfolio } from '@ssv/ui-core';
import { BarChart3, RefreshCw, Inbox, LogOut } from 'lucide-react';
import { Spinner } from '../components/ui/Spinner';
import { PositionRow } from '../components/PositionRow';
import { Button } from '../components/ui/button';

type Status =
  | { kind: 'loading' }
  | { kind: 'success'; data: Portfolio }
  | { kind: 'error'; message: string };

export default function PortfolioPage() {
  const portfolio = usePortfolio();
  const auth = useAuth();
  const [status, setStatus] = useState<Status>({ kind: 'loading' });
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const doFetch = useCallback(() => {
    portfolio
      .fetchPortfolio()
      .then((data) => setStatus({ kind: 'success', data }))
      .catch((err: unknown) => {
        const message =
          err instanceof Error ? err.message : 'An unexpected error occurred.';
        setStatus({ kind: 'error', message });
      });
  }, [portfolio]);

  const load = useCallback(() => {
    setStatus({ kind: 'loading' });
    doFetch();
  }, [doFetch]);

  useEffect(() => {
    doFetch();
  }, [doFetch]);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await auth.logout();
    } catch (err) {
      console.error('Logout failed', err);
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <div
      data-testid="portfolio-page"
      className="min-h-screen bg-background text-foreground flex flex-col relative overflow-hidden"
    >
      {/* Ambient glow */}
      <div className="pointer-events-none absolute top-[-10%] left-[-10%] h-[40%] w-[40%] rounded-full bg-primary/10 blur-[120px]" />
      <div className="pointer-events-none absolute bottom-[-10%] right-[-10%] h-[40%] w-[40%] rounded-full bg-ring/10 blur-[120px]" />

      {/* Navigation Header */}
      <header className="w-full border-b border-white/10 bg-card/50 backdrop-blur-md px-6 py-4 flex justify-between items-center sticky top-0 z-50">
        <div className="text-xl font-bold bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent tracking-wide">
          SSV Financial
        </div>
        <Button
          onClick={handleLogout}
          disabled={isLoggingOut}
          className="bg-card/80 text-foreground hover:bg-destructive/90 hover:text-destructive-foreground border border-white/10 transition-colors py-2 px-4"
        >
          <LogOut className="w-4 h-4 mr-2" />
          <span>{isLoggingOut ? 'Logging Out...' : 'Log Out'}</span>
        </Button>
      </header>

      {/* Main Content */}
      <main className="flex-1 relative z-10 mx-auto w-full max-w-3xl px-4 py-12 sm:px-8">
        {/* Header with Title and Refresh */}
        <div className="mb-8 flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary/15 text-primary">
              <BarChart3 className="h-6 w-6" />
            </div>
            <div>
              <h1 className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent">
                Portfolio
              </h1>
              <p className="text-sm text-muted-foreground">Your current positions</p>
            </div>
          </div>

          {status.kind !== 'loading' && (
            <Button
              id="portfolio-refresh-button"
              onClick={load}
              aria-label="Refresh portfolio"
              className="self-start sm:self-auto bg-card/80 hover:bg-card border border-white/10 hover:border-primary/30 text-foreground"
            >
              <RefreshCw className="h-4 w-4" />
              <span>Refresh</span>
            </Button>
          )}
        </div>

        {/* Content states */}
        {status.kind === 'loading' && (
          <div
            data-testid="portfolio-loading"
            className="flex flex-col items-center justify-center gap-4 py-24"
          >
            <Spinner size="lg" />
            <p className="text-sm text-muted-foreground">Fetching your positions…</p>
          </div>
        )}

        {status.kind === 'error' && (
          <div
            data-testid="portfolio-error"
            role="alert"
            className="rounded-xl border border-destructive/30 bg-destructive/10 p-6 text-center"
          >
            <p className="mb-1 text-base font-semibold text-destructive">Failed to load portfolio</p>
            <p className="text-sm text-muted-foreground">{status.message}</p>
            <Button
              id="portfolio-retry-button"
              onClick={load}
              className="mt-5 bg-destructive/10 hover:bg-destructive/20 text-destructive border border-destructive/30 hover:border-destructive/50"
            >
              Try again
            </Button>
          </div>
        )}

        {status.kind === 'success' && status.data.positions.length === 0 && (
          <div
            data-testid="portfolio-empty"
            className="flex flex-col items-center justify-center gap-4 rounded-xl border border-white/10 bg-card/30 py-24 text-center"
          >
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-muted/20 text-muted-foreground">
              <Inbox className="h-8 w-8" />
            </div>
            <div>
              <p className="text-base font-semibold text-foreground">No positions yet</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Your portfolio is empty. Start investing to track your positions here.
              </p>
            </div>
          </div>
        )}

        {status.kind === 'success' && status.data.positions.length > 0 && (
          <section aria-label="Positions list">
            <div className="mb-3 flex items-center justify-between">
              <p className="text-sm font-medium text-muted-foreground">
                {status.data.positions.length}{' '}
                {status.data.positions.length === 1 ? 'position' : 'positions'}
              </p>
            </div>
            <div
              data-testid="portfolio-positions"
              className="flex flex-col gap-3"
            >
              {status.data.positions.map((position) => (
                <PositionRow key={position.id} position={position} />
              ))}
            </div>
          </section>
        )}
      </main>
    </div>
  );
}
