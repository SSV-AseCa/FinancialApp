import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useWatchlist, useCompany } from '@ssv/ui-core';
import type { Company } from '@ssv/ui-core';
import { ArrowLeft } from 'lucide-react';
import { CompanyDetailCard } from '../components/CompanyDetailCard';

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
        {cik && (
          <CompanyDetailCard
            company={companyInfo}
            cik={cik}
            isWatchlistLoading={isWatchlistLoading}
            isWatched={isWatched}
            isAdding={isAdding}
            error={error}
            onAdd={handleAddToWatchlist}
            onRetryCheck={checkWatchStatus}
          />
        )}
      </main>
    </div>
  );
}
