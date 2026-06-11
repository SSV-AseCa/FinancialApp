import { useEffect, useState } from 'react';
import { usePortfolio } from '@ssv/ui-core';
import type { PortfolioPerformance } from '@ssv/ui-core';
import { TrendingUp, TrendingDown, Activity } from 'lucide-react';
import { Spinner } from './ui/Spinner';

type Status =
  | { kind: 'loading' }
  | { kind: 'success'; data: PortfolioPerformance }
  | { kind: 'error'; message: string };

function formatUsd(value: number): string {
  return value.toLocaleString('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function formatSignedUsd(value: number): string {
  const sign = value > 0 ? '+' : '';
  return `${sign}${formatUsd(value)}`;
}

export function PortfolioPerformancePanel() {
  const portfolio = usePortfolio();
  const [status, setStatus] = useState<Status>({ kind: 'loading' });

  useEffect(() => {
    let ignore = false;
    portfolio
      .getPortfolioPerformance()
      .then((data) => {
        if (!ignore) setStatus({ kind: 'success', data });
      })
      .catch((err: unknown) => {
        if (ignore) return;
        const message =
          err instanceof Error ? err.message : 'Failed to load performance metrics.';
        setStatus({ kind: 'error', message });
      });
    return () => {
      ignore = true;
    };
  }, [portfolio]);

  const pnl = status.kind === 'success' ? status.data.totalPnL : 0;
  const pnlTone =
    pnl > 0 ? 'text-emerald-400' : pnl < 0 ? 'text-destructive' : 'text-foreground';

  return (
    <section
      data-testid="performance-metrics-panel"
      aria-label="Portfolio performance metrics"
      className="mb-8 rounded-xl border border-white/10 bg-card/40 backdrop-blur-sm px-5 py-4"
    >
      <div className="mb-4 flex items-center gap-2">
        <Activity className="h-5 w-5 text-primary" />
        <h2 className="text-lg font-bold text-foreground">Performance</h2>
      </div>

      {status.kind === 'loading' && (
        <div data-testid="performance-loading" className="flex items-center gap-3 py-2">
          <Spinner size="sm" />
          <span className="text-sm text-muted-foreground">Loading performance metrics…</span>
        </div>
      )}

      {status.kind === 'error' && (
        <p
          data-testid="performance-error"
          role="alert"
          className="text-sm text-destructive"
        >
          {status.message}
        </p>
      )}

      {status.kind === 'success' && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-1">
            <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
              Total Value
            </p>
            <p
              data-testid="performance-total-value"
              className="text-2xl font-extrabold text-foreground"
            >
              {formatUsd(status.data.totalValue)}
            </p>
          </div>
          <div className="flex flex-col gap-1">
            <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
              Total P&amp;L
            </p>
            <p
              data-testid="performance-total-pnl"
              className={`flex items-center gap-1.5 text-2xl font-extrabold ${pnlTone}`}
            >
              {pnl < 0 ? (
                <TrendingDown className="h-5 w-5 shrink-0" />
              ) : (
                <TrendingUp className="h-5 w-5 shrink-0" />
              )}
              {formatSignedUsd(status.data.totalPnL)}
            </p>
          </div>
        </div>
      )}
    </section>
  );
}
