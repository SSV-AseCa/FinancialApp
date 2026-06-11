import { useCallback, useEffect, useState } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { useCompany } from '@ssv/ui-core';
import type { CompanyFinancialMetrics } from '@ssv/ui-core';
import { ArrowLeft, Building2, BarChart2, RefreshCw, AlertCircle, Inbox } from 'lucide-react';
import { Spinner } from '../components/ui/Spinner';
import { Button } from '../components/ui/button';
import { MetricCard } from '../components/MetricCard';

interface CompanyState {
  name?: string;
  tickers?: string[];
}

type Status =
  | { kind: 'loading' }
  | { kind: 'success'; data: CompanyFinancialMetrics[] }
  | { kind: 'error'; message: string };


export default function CompanyDetailPage() {
  const { cik } = useParams<{ cik: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const company = useCompany();

  const state = (location.state ?? {}) as CompanyState;
  const companyName = state.name ?? cik ?? '';
  const tickers = state.tickers ?? [];

  const [status, setStatus] = useState<Status>({ kind: 'loading' });

  const doFetch = useCallback(() => {
    if (!cik) return;
    company
      .getCompanyFinancialMetrics(cik)
      .then((data) => setStatus({ kind: 'success', data }))
      .catch((err: unknown) => {
        setStatus({
          kind: 'error',
          message: err instanceof Error ? err.message : 'Failed to load financial metrics.',
        });
      });
  }, [company, cik]);

  const load = useCallback(() => {
    setStatus({ kind: 'loading' });
    doFetch();
  }, [doFetch]);

  useEffect(() => {
    doFetch();
  }, [doFetch]);

  return (
    <div
      data-testid="company-detail-page"
      className="min-h-screen bg-background text-foreground flex flex-col relative overflow-hidden"
    >
      {/* Ambient glow */}
      <div className="pointer-events-none absolute top-[-10%] left-[-10%] h-[40%] w-[40%] rounded-full bg-primary/10 blur-[120px]" />
      <div className="pointer-events-none absolute bottom-[-10%] right-[-10%] h-[35%] w-[35%] rounded-full bg-ring/10 blur-[120px]" />

      {/* Header */}
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

      {/* Main Content */}
      <main className="flex-1 relative z-10 mx-auto w-full max-w-4xl px-4 py-10 sm:px-8">

        {/* Company Identity */}
        <div className="mb-8 flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-start gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-primary/15 text-primary">
              <Building2 className="h-6 w-6" />
            </div>
            <div>
              <h1
                data-testid="company-name"
                className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent"
              >
                {companyName}
              </h1>
              <div className="mt-1 flex flex-wrap items-center gap-2">
                <span className="text-xs text-muted-foreground font-mono">CIK: {cik}</span>
                {tickers.length > 0 && (
                  <div className="flex gap-1 flex-wrap">
                    {tickers.map((t) => (
                      <span
                        key={t}
                        className="rounded-md border border-primary/30 bg-primary/10 px-2 py-0.5 text-xs font-semibold text-primary"
                      >
                        {t}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>

          {status.kind !== 'loading' && (
            <Button
              id="metrics-refresh-button"
              onClick={load}
              aria-label="Refresh metrics"
              className="self-start sm:self-auto bg-card/80 hover:bg-card border border-white/10 hover:border-primary/30 text-foreground"
            >
              <RefreshCw className="h-4 w-4" />
              <span>Refresh</span>
            </Button>
          )}
        </div>

        {/* Section heading */}
        <div className="flex items-center gap-2 mb-5">
          <BarChart2 className="h-5 w-5 text-primary" />
          <h2 className="text-lg font-bold text-foreground">Financial Metrics</h2>
        </div>

        {/* Loading */}
        {status.kind === 'loading' && (
          <div
            data-testid="metrics-loading"
            className="flex flex-col items-center justify-center gap-4 py-24"
          >
            <Spinner size="lg" />
            <p className="text-sm text-muted-foreground">Loading financial metrics…</p>
          </div>
        )}

        {/* Error */}
        {status.kind === 'error' && (
          <div
            data-testid="metrics-error"
            role="alert"
            className="rounded-xl border border-destructive/30 bg-destructive/10 p-6 text-center"
          >
            <div className="flex justify-center mb-3">
              <AlertCircle className="h-8 w-8 text-destructive" />
            </div>
            <p className="mb-1 text-base font-semibold text-destructive">Failed to load metrics</p>
            <p className="text-sm text-muted-foreground mb-5">{status.message}</p>
            <Button
              id="metrics-retry-button"
              onClick={load}
              className="bg-destructive/10 hover:bg-destructive/20 text-destructive border border-destructive/30 hover:border-destructive/50"
            >
              Try again
            </Button>
          </div>
        )}

        {/* Empty */}
        {status.kind === 'success' && status.data.length === 0 && (
          <div
            data-testid="metrics-empty"
            className="flex flex-col items-center justify-center gap-4 rounded-xl border border-white/10 bg-card/30 py-24 text-center"
          >
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-muted/20 text-muted-foreground">
              <Inbox className="h-8 w-8" />
            </div>
            <div>
              <p className="text-base font-semibold text-foreground">No metrics available</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Financial metrics for this company are not yet available.
              </p>
            </div>
          </div>
        )}

        {/* Metrics Grid */}
        {status.kind === 'success' && status.data.length > 0 && (
          <section aria-label="Financial metrics">
            <div
              data-testid="metrics-grid"
              className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4"
            >
              {status.data.map((m, idx) => (
                <MetricCard
                  key={`${m.metric}-${idx}`}
                  metric={m}
                  data-testid={`metric-card-${idx}`}
                />
              ))}
            </div>
          </section>
        )}
      </main>
    </div>
  );
}
