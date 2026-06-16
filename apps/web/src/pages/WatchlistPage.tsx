import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useWatchlist } from '@ssv/ui-core';
import type { WatchlistCompany } from '@ssv/ui-core';
import { ArrowLeft, Star, AlertCircle } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Spinner } from '../components/ui/Spinner';
import { WatchlistItem } from '../components/WatchlistItem';

type PageStatus =
  | { kind: 'loading' }
  | { kind: 'success'; data: WatchlistCompany[] }
  | { kind: 'error'; message: string };

export default function WatchlistPage() {
  const watchlist = useWatchlist();
  const navigate = useNavigate();
  const [status, setStatus] = useState<PageStatus>({ kind: 'loading' });
  const [removingCik, setRemovingCik] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleRetry = () => {
    setStatus({ kind: 'loading' });
    watchlist.getWatchlist()
      .then((data) => setStatus({ kind: 'success', data }))
      .catch((err) => {
        setStatus({
          kind: 'error',
          message: err instanceof Error ? err.message : 'Failed to fetch watchlist.',
        });
      });
  };

  useEffect(() => {
    let active = true;
    watchlist.getWatchlist()
      .then((data) => {
        if (active) {
          setStatus({ kind: 'success', data });
        }
      })
      .catch((err) => {
        if (active) {
          setStatus({
            kind: 'error',
            message: err instanceof Error ? err.message : 'Failed to fetch watchlist.',
          });
        }
      });
    return () => {
      active = false;
    };
  }, [watchlist]);

  const handleRemove = async (cik: string) => {
    setRemovingCik(cik);
    setError(null);
    try {
      await watchlist.removeFromWatchlist(cik);
      setStatus((currentStatus) => {
        if (currentStatus.kind === 'success') {
          return {
            ...currentStatus,
            data: currentStatus.data.filter((item) => item.cik !== cik),
          };
        }
        return currentStatus;
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to remove from watchlist.');
    } finally {
      setRemovingCik(null);
    }
  };

  const handleNavigateToCompany = (company: WatchlistCompany) => {
    navigate(`/companies/${company.cik}`, {
      state: { name: company.name, tickers: [company.symbol].filter(Boolean) },
    });
  };

  return (
    <div
      data-testid="watchlist-page"
      className="min-h-screen bg-background text-foreground flex flex-col relative overflow-hidden"
    >
      {/* Background radial ambient glows */}
      <div className="pointer-events-none absolute top-[-10%] left-[-10%] h-[40%] w-[40%] rounded-full bg-primary/10 blur-[120px]" />
      <div className="pointer-events-none absolute bottom-[-10%] right-[-10%] h-[40%] w-[40%] rounded-full bg-ring/10 blur-[120px]" />

      <header className="w-full border-b border-white/10 bg-card/50 backdrop-blur-md px-6 py-4 flex items-center gap-4 sticky top-0 z-50">
        <button
          onClick={() => navigate('/portfolio')}
          className="text-muted-foreground hover:text-foreground transition-colors"
          aria-label="Back to portfolio"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div className="text-xl font-bold bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent tracking-wide">
          My Watchlist
        </div>
      </header>

      <main className="flex-1 relative z-10 mx-auto w-full max-w-4xl px-4 py-12 sm:px-8">
        <div className="mb-8 flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary/15 text-primary">
            <Star className="h-6 w-6 fill-primary/25 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent">
              Watchlist
            </h1>
            <p className="text-sm text-muted-foreground">Monitor key metrics of your saved companies</p>
          </div>
        </div>

        {error && (
          <div
            data-testid="watchlist-error"
            className="mb-6 rounded-xl border border-destructive/30 bg-destructive/10 p-4 flex items-start gap-3 text-sm text-destructive"
          >
            <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
            <div className="flex-1">
              <p className="font-semibold">Error updating watchlist</p>
              <p className="mt-0.5 opacity-90">{error}</p>
              <button
                onClick={() => setError(null)}
                className="mt-2 text-xs font-semibold underline hover:no-underline opacity-80 hover:opacity-100"
              >
                Dismiss
              </button>
            </div>
          </div>
        )}

        {status.kind === 'loading' && (
          <div className="flex justify-center py-20">
            <Spinner size="lg" />
          </div>
        )}

        {status.kind === 'error' && (
          <div
            role="alert"
            className="rounded-xl border border-destructive/30 bg-destructive/10 p-4 flex items-center gap-3 text-sm text-destructive"
          >
            <AlertCircle className="h-5 w-5 shrink-0" />
            <div className="flex-1">
              <p className="font-semibold">Failed to load watchlist</p>
              <p className="mt-0.5 opacity-90">{status.message}</p>
            </div>
            <Button onClick={handleRetry} className="bg-destructive hover:bg-destructive/90 text-white text-xs">
              Retry
            </Button>
          </div>
        )}

        {status.kind === 'success' && status.data.length === 0 && (
          <div
            data-testid="watchlist-empty"
            className="rounded-2xl border border-white/5 bg-card/20 backdrop-blur-sm p-16 text-center flex flex-col items-center justify-center gap-4"
          >
            <Star className="h-12 w-12 text-muted-foreground/30 stroke-[1.5]" />
            <p className="text-muted-foreground max-w-sm">
              Your watchlist is currently empty. Start adding companies to monitor their financial health!
            </p>
            <Button
              onClick={() => navigate('/companies')}
              className="mt-2 bg-primary/20 hover:bg-primary/30 border border-primary/30 text-primary-foreground font-semibold py-2 px-5 rounded-xl transition-all"
            >
              Search Companies
            </Button>
          </div>
        )}

        {status.kind === 'success' && status.data.length > 0 && (
          <div className="flex flex-col gap-4">
            {status.data.map((c) => (
              <WatchlistItem
                key={c.cik}
                company={c}
                isRemoving={removingCik === c.cik}
                onRemove={handleRemove}
                onNavigate={handleNavigateToCompany}
              />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
