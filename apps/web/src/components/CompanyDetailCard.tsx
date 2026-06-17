import { Building2, Check, AlertCircle, Plus, Loader2, Trash2 } from 'lucide-react';
import { Button } from './ui/button';

interface CompanyDetailCardProps {
  company: Partial<Company>;
  cik: string;
  isWatchlistLoading: boolean;
  isWatched: boolean;
  isAdding: boolean;
  isRemoving?: boolean;
  error: string | null;
  onAdd: () => void;
  onRemove?: () => void;
  onRetryCheck: () => void;
}

export function CompanyDetailCard({
  company,
  cik,
  isWatchlistLoading,
  isWatched,
  isAdding,
  isRemoving,
  error,
  onAdd,
  onRemove,
  onRetryCheck,
}: CompanyDetailCardProps) {
  const renderWatchlistAction = () => {
    if (isWatchlistLoading) {
      return (
        <div data-testid="watchlist-loading" className="flex items-center gap-2 text-sm text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin text-primary" />
          <span>Checking status...</span>
        </div>
      );
    }

    if (isWatched) {
      return (
        <Button
          data-testid="watching-badge"
          onClick={onRemove}
          disabled={isRemoving}
          className="bg-emerald-500/10 hover:bg-destructive/10 border border-emerald-500/30 hover:border-destructive/30 text-emerald-400 hover:text-destructive font-semibold px-5 py-2.5 rounded-xl flex items-center gap-2 transition-all active:scale-95 disabled:pointer-events-none group"
        >
          {isRemoving ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              <span>Removing...</span>
            </>
          ) : (
            <>
              <Check className="h-4 w-4 group-hover:hidden" />
              <Trash2 className="h-4 w-4 hidden group-hover:block" />
              <span className="grid grid-cols-1 grid-rows-1 text-left">
                <span className="col-start-1 row-start-1 transition-opacity duration-200 group-hover:opacity-0">
                  Watching
                </span>
                <span className="col-start-1 row-start-1 transition-opacity duration-200 opacity-0 group-hover:opacity-100">
                  Unwatch
                </span>
              </span>
            </>
          )}
        </Button>
      );
    }

    return (
      <Button
        data-testid="add-watchlist-button"
        onClick={onAdd}
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
    );
  };

  return (
    <div className="rounded-2xl border border-white/10 bg-card/30 backdrop-blur-md p-6 sm:p-8 flex flex-col gap-6 shadow-xl">
      {/* Header Info */}
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div className="flex items-start gap-4">
          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-primary/15 text-primary border border-primary/20">
            <Building2 className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-foreground">
              {company.name || `CIK ${cik}`}
            </h1>
            <div className="flex flex-wrap gap-x-3 gap-y-1 mt-1 text-sm text-muted-foreground">
              <span>CIK: {cik}</span>
              {company.tickers && company.tickers.length > 0 && (
                <>
                  <span className="text-white/20">•</span>
                  <span>Tickers: {company.tickers.join(', ')}</span>
                </>
              )}
            </div>
          </div>
        </div>

        {/* Action State / Button */}
        <div className="flex items-center shrink-0">
          {renderWatchlistAction()}
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
              onClick={onRetryCheck}
              className="mt-2 text-xs font-semibold underline hover:no-underline opacity-80 hover:opacity-100"
            >
              Retry
            </button>
          </div>
        </div>
      )}

      {/* Quick Metrics */}
      <div className="border-t border-white/5 pt-6 mt-2">
        <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider mb-4">
          Company Overview
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="bg-white/5 rounded-xl p-4 border border-white/5">
            <p className="text-xs text-muted-foreground">Entity Name</p>
            <p className="text-base font-semibold mt-1">{company.name || 'N/A'}</p>
          </div>
          <div className="bg-white/5 rounded-xl p-4 border border-white/5">
            <p className="text-xs text-muted-foreground">Central Index Key (CIK)</p>
            <p className="text-base font-semibold mt-1">{cik}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
