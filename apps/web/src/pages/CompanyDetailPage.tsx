import { useEffect, useState } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { useCompany } from '@ssv/ui-core';
import type { CompanyDetails } from '@ssv/ui-core';
import { Building2, ArrowLeft, TrendingUp, FileText } from 'lucide-react';
import { Spinner } from '../components/ui/Spinner';
import { Button } from '../components/ui/button';

type Status =
  | { kind: 'loading' }
  | { kind: 'success'; data: CompanyDetails }
  | { kind: 'error'; message: string };

export default function CompanyDetailPage() {
  const { cik } = useParams<{ cik: string }>();
  const [searchParams] = useSearchParams();
  const company = useCompany();
  const navigate = useNavigate();
  const [status, setStatus] = useState<Status>({ kind: 'loading' });

  useEffect(() => {
    if (!cik) return;
    const name = searchParams.get('name') ?? '';
    const symbol = searchParams.get('symbol') ?? '';
    company
      .getCompanyDetails(cik, name, symbol)
      .then((data) => setStatus({ kind: 'success', data }))
      .catch((err: unknown) => {
        setStatus({ kind: 'error', message: err instanceof Error ? err.message : 'Failed to load company.' });
      });
  }, [cik, company, searchParams]);

  return (
    <div
      data-testid="company-detail-page"
      className="min-h-screen bg-background text-foreground flex flex-col relative overflow-hidden"
    >
      <div className="pointer-events-none absolute top-[-10%] left-[-10%] h-[40%] w-[40%] rounded-full bg-primary/10 blur-[120px]" />

      <header className="w-full border-b border-white/10 bg-card/50 backdrop-blur-md px-6 py-4 flex items-center gap-4 sticky top-0 z-50">
        <button
          onClick={() => navigate(-1)}
          className="text-muted-foreground hover:text-foreground transition-colors"
          aria-label="Back"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div className="text-xl font-bold bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent tracking-wide">
          Company Research
        </div>
      </header>

      <main className="flex-1 relative z-10 mx-auto w-full max-w-3xl px-4 py-12 sm:px-8">
        {status.kind === 'loading' && (
          <div className="flex flex-col items-center justify-center gap-4 py-24">
            <Spinner size="lg" />
            <p className="text-sm text-muted-foreground">Loading company data…</p>
          </div>
        )}

        {status.kind === 'error' && (
          <div role="alert" className="rounded-xl border border-destructive/30 bg-destructive/10 p-6 text-center">
            <p className="text-base font-semibold text-destructive mb-2">Failed to load company</p>
            <p className="text-sm text-muted-foreground mb-4">{status.message}</p>
            <Button onClick={() => navigate(-1)} className="bg-card/80 border border-white/10 text-foreground">
              Go back
            </Button>
          </div>
        )}

        {status.kind === 'success' && (
          <>
            {/* Company Header */}
            <div className="mb-8 flex items-center gap-3">
              <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary/15 text-primary">
                <Building2 className="h-6 w-6" />
              </div>
              <div>
                <h1
                  data-testid="company-name"
                  className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent"
                >
                  {status.data.name}
                </h1>
                <p className="text-sm text-muted-foreground">
                  {status.data.symbol && <span className="font-mono mr-2">{status.data.symbol}</span>}
                  CIK: {status.data.cik}
                </p>
              </div>
            </div>

            {/* Financial Metrics */}
            <section className="mb-8" aria-label="Financial metrics">
              <div className="flex items-center gap-2 mb-4">
                <TrendingUp className="h-5 w-5 text-primary" />
                <h2 className="text-lg font-bold text-foreground">Financial Metrics</h2>
              </div>
              {status.data.financialMetrics.length === 0 ? (
                <p className="text-sm text-muted-foreground" data-testid="no-metrics">
                  No financial metrics available.
                </p>
              ) : (
                <div
                  data-testid="financial-metrics-list"
                  className="grid grid-cols-1 sm:grid-cols-2 gap-3"
                >
                  {status.data.financialMetrics.slice(0, 20).map((m, i) => (
                    <div
                      key={i}
                      className="rounded-xl border border-white/10 bg-card/40 px-4 py-3"
                    >
                      <p className="text-xs font-medium text-muted-foreground truncate">{m.metric}</p>
                      <p className="text-base font-semibold tabular-nums text-foreground">
                        {Number(m.value).toLocaleString('en-US')}
                        <span className="text-xs text-muted-foreground ml-1">{m.unit}</span>
                      </p>
                      {m.periodEnd && (
                        <p className="text-xs text-muted-foreground mt-0.5">{m.periodEnd}</p>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </section>

            {/* SEC Filings */}
            <section aria-label="SEC filings">
              <div className="flex items-center gap-2 mb-4">
                <FileText className="h-5 w-5 text-primary" />
                <h2 className="text-lg font-bold text-foreground">Recent SEC Filings</h2>
              </div>
              {status.data.filings.length === 0 ? (
                <p className="text-sm text-muted-foreground" data-testid="no-filings">
                  No filings available.
                </p>
              ) : (
                <ul
                  data-testid="sec-filings-list"
                  className="flex flex-col gap-2"
                >
                  {status.data.filings.slice(0, 20).map((f, i) => (
                    <li
                      key={i}
                      className="flex items-center justify-between rounded-xl border border-white/10 bg-card/40 px-4 py-3"
                    >
                      <div>
                        <span className="text-sm font-semibold text-foreground">{f.formType}</span>
                        <span className="text-xs text-muted-foreground ml-3">{f.filingDate}</span>
                      </div>
                      {f.url && (
                        <a
                          href={f.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-xs text-primary hover:underline"
                        >
                          View
                        </a>
                      )}
                    </li>
                  ))}
                </ul>
              )}
            </section>
          </>
        )}
      </main>
    </div>
  );
}
