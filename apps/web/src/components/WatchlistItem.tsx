import type { WatchlistCompany } from '@ssv/ui-core';
import { Trash2, TrendingUp, DollarSign, Loader2 } from 'lucide-react';
import { Button } from './ui/button';

interface WatchlistItemProps {
  company: WatchlistCompany;
  isRemoving: boolean;
  onRemove: (cik: string) => void;
  onNavigate: (c: WatchlistCompany) => void;
  isSelected?: boolean;
  onSelectToggle?: (cik: string) => void;
}

export function WatchlistItem({
  company,
  isRemoving,
  onRemove,
  onNavigate,
  isSelected,
  onSelectToggle
}: WatchlistItemProps) {
  const formatCurrency = (val: number | undefined | null) => {
    if (val === undefined || val === null) return 'N/A';
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
      data-testid={`watchlist-item-${company.cik}`}
      className={`rounded-2xl border ${
        isSelected ? 'border-primary bg-primary/5' : 'border-white/10 bg-card/30'
      } backdrop-blur-sm p-5 sm:p-6 hover:border-white/15 transition-all flex flex-row items-start gap-4 shadow-md`}
    >
      {onSelectToggle && (
        <div className="pt-1 select-none flex items-center justify-center shrink-0">
          <input
            type="checkbox"
            data-testid={`compare-select-${company.cik}`}
            checked={isSelected || false}
            onChange={() => onSelectToggle(company.cik)}
            className="h-5 w-5 rounded border-white/20 bg-white/5 text-primary focus:ring-primary focus:ring-offset-0 cursor-pointer"
          />
        </div>
      )}

      <div className="flex-1 flex flex-col gap-4">
        {/* Item Top Section */}
        <div className="flex items-start justify-between gap-4">
          <button
            onClick={() => onNavigate(company)}
            className="text-left cursor-pointer group flex-1 focus:outline-none"
          >
            <h2 className="text-xl font-bold text-foreground group-hover:text-primary transition-colors flex items-center gap-2">
              {company.name}
              {company.symbol && (
                <span className="text-xs font-semibold px-2 py-0.5 rounded bg-primary/10 text-primary border border-primary/20 uppercase">
                  {company.symbol}
                </span>
              )}
            </h2>
            <p className="text-xs text-muted-foreground mt-0.5">CIK: {company.cik}</p>
          </button>

          <Button
            data-testid={`remove-watchlist-${company.cik}`}
            onClick={() => onRemove(company.cik)}
            disabled={isRemoving}
            className="h-9 w-9 p-0 shrink-0 border border-white/5 hover:border-destructive/30 bg-white/5 hover:bg-destructive/10 text-muted-foreground hover:text-destructive transition-colors rounded-lg flex items-center justify-center"
            aria-label={`Remove ${company.name} from watchlist`}
          >
            {isRemoving ? (
              <Loader2 className="h-4 w-4 animate-spin text-destructive" />
            ) : (
              <Trash2 className="h-4 w-4" />
            )}
          </Button>
        </div>

        {/* Metrics Section */}
        {company.metrics ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 pt-3 border-t border-white/5">
            <div className="bg-white/5 rounded-xl p-3 border border-white/5">
              <p className="text-xs text-muted-foreground font-medium flex items-center gap-1">
                <DollarSign className="w-3.5 h-3.5 text-primary" />
                <span>Revenue</span>
              </p>
              <p className="text-base font-bold mt-1 text-foreground">
                {formatCurrency(company.metrics.revenue)}
              </p>
            </div>

            <div className="bg-white/5 rounded-xl p-3 border border-white/5">
              <p className="text-xs text-muted-foreground font-medium flex items-center gap-1">
                <TrendingUp className="w-3.5 h-3.5 text-emerald-400" />
                <span>Net Income</span>
              </p>
              <p
                className={`text-base font-bold mt-1 ${
                  company.metrics.netIncome >= 0 ? 'text-emerald-400' : 'text-rose-400'
                }`}
              >
                {formatCurrency(company.metrics.netIncome)}
              </p>
            </div>

            <div className="bg-white/5 rounded-xl p-3 border border-white/5">
              <p className="text-xs text-muted-foreground font-medium">Assets</p>
              <p className="text-base font-bold mt-1 text-foreground">
                {formatCurrency(company.metrics.assets)}
              </p>
            </div>

            <div className="bg-white/5 rounded-xl p-3 border border-white/5">
              <p className="text-xs text-muted-foreground font-medium">Equity</p>
              <p className="text-base font-bold mt-1 text-foreground">
                {formatCurrency(company.metrics.equity)}
              </p>
            </div>
          </div>
        ) : (
          <p className="text-xs text-muted-foreground italic pt-2">No financial metrics available.</p>
        )}
      </div>
    </div>
  );
}

