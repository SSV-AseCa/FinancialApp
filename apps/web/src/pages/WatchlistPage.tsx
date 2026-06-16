import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useWatchlist } from '@ssv/ui-core';
import type { WatchlistCompany } from '@ssv/ui-core';
import { ArrowLeft, Trash2, Star, TrendingUp, DollarSign, Loader2, AlertCircle } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Spinner } from '../components/ui/Spinner';

type PageStatus =
  | { kind: 'loading' }
  | { kind: 'success'; data: WatchlistCompany[] }
  | { kind: 'error'; message: string };

export default function WatchlistPage() {
  const watchlist = useWatchlist();
  const navigate = useNavigate();
  const [status, setStatus] = useState<PageStatus>({ kind: 'loading' });
  const [removingCik, setRemovingCik] = useState<string | null>(null);

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
    try {
      await watchlist.removeFromWatchlist(cik);
      // Refresh local state without a full reload
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
      alert(err instanceof Error ? err.message : 'Failed to remove from watchlist.');
    } finally {
      setRemovingCik(null);
    }
  };

  const formatCurrency = (val: number | undefined | null) => {
    if (val === undefined || val === null) return 'N/A';
    // Format to billions or millions
    const absVal = Math.abs(val);
    if (absVal >= 1e9) {
      return `$${(val / 1e9).toFixed(2)}B`;
    }
    if (absVal >= 1e6) {
      return `$${(val / 1e6).toFixed(2)}M`;
    }
    return `$${val.toLocaleString()}`;
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
              <div
                key={c.cik}
                data-testid={`watchlist-item-${c.cik}`}
                className="rounded-2xl border border-white/10 bg-card/30 backdrop-blur-sm p-5 sm:p-6 hover:border-white/15 transition-all flex flex-col gap-4 shadow-md"
              >
                {/* Item Top Section */}
                <div className="flex items-start justify-between gap-4">
                  <div
                    onClick={() =>
                      navigate(`/companies/${c.cik}`, {
                        state: { name: c.name, tickers: [c.symbol].filter(Boolean) },
                      })
                    }
                    className="cursor-pointer group flex-1"
                  >
                    <h2 className="text-xl font-bold text-foreground group-hover:text-primary transition-colors flex items-center gap-2">
                      {c.name}
                      {c.symbol && (
                        <span className="text-xs font-semibold px-2 py-0.5 rounded bg-primary/10 text-primary border border-primary/20 uppercase">
                          {c.symbol}
                        </span>
                      )}
                    </h2>
                    <p className="text-xs text-muted-foreground mt-0.5">CIK: {c.cik}</p>
                  </div>

                  <Button
                    data-testid={`remove-watchlist-${c.cik}`}
                    onClick={() => handleRemove(c.cik)}
                    disabled={removingCik === c.cik}
                    className="h-9 w-9 p-0 shrink-0 border border-white/5 hover:border-destructive/30 bg-white/5 hover:bg-destructive/10 text-muted-foreground hover:text-destructive transition-colors rounded-lg flex items-center justify-center"
                    aria-label={`Remove ${c.name} from watchlist`}
                  >
                    {removingCik === c.cik ? (
                      <Loader2 className="h-4 w-4 animate-spin text-destructive" />
                    ) : (
                      <Trash2 className="h-4 w-4" />
                    )}
                  </Button>
                </div>

                {/* Metrics Section */}
                {c.metrics ? (
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-3 pt-3 border-t border-white/5">
                    <div className="bg-white/5 rounded-xl p-3 border border-white/5">
                      <p className="text-xs text-muted-foreground font-medium flex items-center gap-1">
                        <DollarSign className="w-3.5 h-3.5 text-primary" />
                        <span>Revenue</span>
                      </p>
                      <p className="text-base font-bold mt-1 text-foreground">
                        {formatCurrency(c.metrics.revenue)}
                      </p>
                    </div>

                    <div className="bg-white/5 rounded-xl p-3 border border-white/5">
                      <p className="text-xs text-muted-foreground font-medium flex items-center gap-1">
                        <TrendingUp className="w-3.5 h-3.5 text-emerald-400" />
                        <span>Net Income</span>
                      </p>
                      <p
                        className={`text-base font-bold mt-1 ${
                          c.metrics.netIncome >= 0 ? 'text-emerald-400' : 'text-rose-400'
                        }`}
                      >
                        {formatCurrency(c.metrics.netIncome)}
                      </p>
                    </div>

                    <div className="bg-white/5 rounded-xl p-3 border border-white/5">
                      <p className="text-xs text-muted-foreground font-medium">Assets</p>
                      <p className="text-base font-bold mt-1 text-foreground">
                        {formatCurrency(c.metrics.assets)}
                      </p>
                    </div>

                    <div className="bg-white/5 rounded-xl p-3 border border-white/5">
                      <p className="text-xs text-muted-foreground font-medium">Equity</p>
                      <p className="text-base font-bold mt-1 text-foreground">
                        {formatCurrency(c.metrics.equity)}
                      </p>
                    </div>
                  </div>
                ) : (
                  <p className="text-xs text-muted-foreground italic pt-2">No financial metrics available.</p>
                )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
