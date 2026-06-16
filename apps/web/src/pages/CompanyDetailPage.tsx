import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useWatchlist, useCompany } from '@ssv/ui-core';
import type { Company } from '@ssv/ui-core';
import { ArrowLeft, Building2, Check, AlertCircle, Plus, Loader2 } from 'lucide-react';
import { Button } from '../components/ui/button';

export default function CompanyDetailPage() {
  const { cik } = useParams<{ cik: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const watchlist = useWatchlist();
  const companyService = useCompany();

  const [companyInfo, setCompanyInfo] = useState<Partial<Company>>(() => {
    const state = location.state as { name?: string; tickers?: string[] } | null;
    return {
      cik,
      name: state?.name,
      tickers: state?.tickers,
    };
  });

  const [isWatchlistLoading, setIsWatchlistLoading] = useState(true);
  const [isAdding, setIsAdding] = useState(false);
  const [isWatched, setIsWatched] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load watchlist to see if this company is already watched
  const checkWatchStatus = useCallback(async () => {
    if (!cik) return;
    setIsWatchlistLoading(true);
    setError(null);
    try {
      const items = await watchlist.getWatchlist();
      setIsWatched(items.some((item) => item.cik === cik));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load watchlist status.');
    } finally {
      setIsWatchlistLoading(false);
    }
  }, [cik, watchlist]);

  // Fetch company details and check watchlist status
  useEffect(() => {
    let active = true;

    if (cik) {
      watchlist.getWatchlist()
        .then((items) => {
          if (active) {
            setIsWatched(items.some((item) => item.cik === cik));
          }
        })
        .catch((err) => {
          if (active) {
            setError(err instanceof Error ? err.message : 'Failed to load watchlist status.');
          }
        })
        .finally(() => {
          if (active) {
            setIsWatchlistLoading(false);
          }
        });

      if (!companyInfo.name) {
        companyService
          .searchCompanies(cik)
          .then((results) => {
            const matched = results.find((c) => c.cik === cik);
            if (matched && active) {
              setCompanyInfo(matched);
            }
          })
          .catch(() => {
            // Fallback gracefully
          });
      }
    }

    return () => {
      active = false;
    };
  }, [cik, watchlist, companyInfo.name, companyService]);

  const handleAddToWatchlist = async () => {
    if (!cik) return;
    setIsAdding(true);
    setError(null);
    try {
      await watchlist.addToWatchlist(cik);
      setIsWatched(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to add company to watchlist.');
    } finally {
      setIsAdding(false);
    }
  };

  return (
    <div
      data-testid="company-detail-page"
      className="min-h-screen bg-background text-foreground flex flex-col relative overflow-hidden"
    >
      {/* Background radial ambient glows */}
      <div className="pointer-events-none absolute top-[-10%] left-[-10%] h-[40%] w-[40%] rounded-full bg-primary/10 blur-[120px]" />
      <div className="pointer-events-none absolute bottom-[-10%] right-[-10%] h-[40%] w-[40%] rounded-full bg-ring/10 blur-[120px]" />

      <header className="w-full border-b border-white/10 bg-card/50 backdrop-blur-md px-6 py-4 flex items-center gap-4 sticky top-0 z-50">
        <button
          onClick={() => navigate('/companies')}
          className="text-muted-foreground hover:text-foreground transition-colors"
          aria-label="Back to company search"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div className="text-xl font-bold bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent tracking-wide">
          Company Details
        </div>
      </header>

      <main className="flex-1 relative z-10 mx-auto w-full max-w-2xl px-4 py-12 sm:px-8">
        <div className="rounded-2xl border border-white/10 bg-card/30 backdrop-blur-md p-6 sm:p-8 flex flex-col gap-6 shadow-xl">
          {/* Header Info */}
          <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
            <div className="flex items-start gap-4">
              <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-primary/15 text-primary border border-primary/20">
                <Building2 className="h-6 w-6" />
              </div>
              <div>
                <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-foreground">
                  {companyInfo.name || `CIK ${cik}`}
                </h1>
                <div className="flex flex-wrap gap-x-3 gap-y-1 mt-1 text-sm text-muted-foreground">
                  <span>CIK: {cik}</span>
                  {companyInfo.tickers && companyInfo.tickers.length > 0 && (
                    <>
                      <span className="text-white/20">•</span>
                      <span>Tickers: {companyInfo.tickers.join(', ')}</span>
                    </>
                  )}
                </div>
              </div>
            </div>

            {/* Action State / Button */}
            <div className="flex items-center shrink-0">
              {isWatchlistLoading ? (
                <div data-testid="watchlist-loading" className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Loader2 className="h-4 w-4 animate-spin text-primary" />
                  <span>Checking status...</span>
                </div>
              ) : isWatched ? (
                <div
                  data-testid="watching-badge"
                  className="flex items-center gap-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 px-4 py-2 text-sm font-semibold text-emerald-400 select-none cursor-default"
                >
                  <Check className="h-4 w-4" />
                  <span>Watching</span>
                </div>
              ) : (
                <Button
                  data-testid="add-watchlist-button"
                  onClick={handleAddToWatchlist}
                  disabled={isAdding}
                  className="bg-primary hover:bg-primary/90 text-primary-foreground font-semibold px-5 py-2.5 rounded-xl flex items-center gap-2 transition-all shadow-lg shadow-primary/25 hover:shadow-primary/35 active:scale-95 disabled:pointer-events-none"
                >
                  {isAdding ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" />
                      <span>Adding...</span>
                    </>
                  ) : (
                    <>
                      <Plus className="h-4 w-4" />
                      <span>Add to Watchlist</span>
                    </>
                  )}
                </Button>
              )}
            </div>
          </div>

          {/* Inline Error State */}
          {error && (
            <div
              data-testid="watchlist-error"
              className="rounded-xl border border-destructive/30 bg-destructive/10 p-4 flex items-start gap-3 text-sm text-destructive"
            >
              <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
              <div className="flex-1">
                <p className="font-semibold">Error updating watchlist</p>
                <p className="mt-0.5 opacity-90">{error}</p>
                <button
                  onClick={checkWatchStatus}
                  className="mt-2 text-xs font-semibold underline hover:no-underline opacity-80 hover:opacity-100"
                >
                  Retry
                </button>
              </div>
            </div>
          )}

          {/* Quick Metrics Placeholder / Context */}
          <div className="border-t border-white/5 pt-6 mt-2">
            <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider mb-4">
              Company Overview
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="bg-white/5 rounded-xl p-4 border border-white/5">
                <p className="text-xs text-muted-foreground">Entity Name</p>
                <p className="text-base font-semibold mt-1">{companyInfo.name || 'N/A'}</p>
              </div>
              <div className="bg-white/5 rounded-xl p-4 border border-white/5">
                <p className="text-xs text-muted-foreground">Central Index Key (CIK)</p>
                <p className="text-base font-semibold mt-1">{cik}</p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
