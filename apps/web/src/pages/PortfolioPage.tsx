import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { usePortfolio, useAuth } from '@ssv/ui-core';
import type { Portfolio, AddPositionInput, ModifyPositionInput } from '@ssv/ui-core';
import { BarChart3, RefreshCw, Inbox, LogOut, Plus, X, Building2, TrendingUp, Wallet, Star } from 'lucide-react';
import { Spinner } from '../components/ui/Spinner';
import { PositionRow } from '../components/PositionRow';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { PortfolioPerformancePanel } from '../components/PortfolioPerformancePanel';

type Status =
  | { kind: 'loading' }
  | { kind: 'success'; data: Portfolio }
  | { kind: 'error'; message: string };

type ValueStatus =
  | { kind: 'loading' }
  | { kind: 'success'; value: number }
  | { kind: 'error' };

const usdFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
});

const formatUsd = (value: number): string => usdFormatter.format(value);

type AddFormState = {
  cik: string;
  quantity: string;
  operationDate: string;
  error: string | null;
  saving: boolean;
};

const defaultAddForm = (): AddFormState => ({
  cik: '',
  quantity: '',
  operationDate: new Date().toISOString().slice(0, 10),
  error: null,
  saving: false,
});

export default function PortfolioPage() {
  const portfolio = usePortfolio();
  const auth = useAuth();
  const navigate = useNavigate();
  const [status, setStatus] = useState<Status>({ kind: 'loading' });
  const [valueStatus, setValueStatus] = useState<ValueStatus>({ kind: 'loading' });
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [addForm, setAddForm] = useState<AddFormState>(defaultAddForm());
  const [reloadToken, setReloadToken] = useState(0);

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

  const doFetchValue = useCallback(() => {
    portfolio
      .getPortfolioTotalValue()
      .then(({ totalValue }) => setValueStatus({ kind: 'success', value: totalValue }))
      .catch(() => setValueStatus({ kind: 'error' }));
  }, [portfolio]);

  // Re-fetch every portfolio-derived view, including the self-contained
  // performance panel, which re-runs its effect when reloadToken changes.
  const refreshDerived = useCallback(() => {
    doFetch();
    doFetchValue();
    setReloadToken((token) => token + 1);
  }, [doFetch, doFetchValue]);

  const load = useCallback(() => {
    setStatus({ kind: 'loading' });
    setValueStatus({ kind: 'loading' });
    refreshDerived();
  }, [refreshDerived]);

  useEffect(() => {
    doFetch();
    doFetchValue();
  }, [doFetch, doFetchValue]);

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

  const handleAddPosition = async () => {
    const qty = parseInt(addForm.quantity, 10);
    if (!addForm.cik.trim() || isNaN(qty) || qty <= 0 || !addForm.operationDate) {
      setAddForm((f) => ({ ...f, error: 'All fields are required and quantity must be positive.' }));
      return;
    }
    setAddForm((f) => ({ ...f, saving: true, error: null }));
    const input: AddPositionInput = {
      cik: addForm.cik.trim(),
      quantity: qty,
      operationDate: addForm.operationDate,
    };
    try {
      await portfolio.addPosition(input);
      setAddForm(defaultAddForm());
      setShowAddForm(false);
      load();
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to add position.';
      setAddForm((f) => ({ ...f, error: message, saving: false }));
    }
  };

  const handleModify = async (positionId: string, input: ModifyPositionInput) => {
    await portfolio.modifyPosition(positionId, input);
    refreshDerived();
  };

  const handleRemove = async (positionId: string) => {
    await portfolio.removePosition(positionId);
    refreshDerived();
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
        <div className="flex items-center gap-2">
          <Button
            onClick={() => navigate('/companies')}
            className="bg-card/80 text-foreground hover:bg-card border border-white/10 py-2 px-3"
            aria-label="Company Research"
          >
            <Building2 className="w-4 h-4" />
            <span className="hidden sm:inline">Research</span>
          </Button>
          <Button
            onClick={() => navigate('/trading')}
            className="bg-card/80 text-foreground hover:bg-card border border-white/10 py-2 px-3"
            aria-label="Trading"
          >
            <TrendingUp className="w-4 h-4" />
            <span className="hidden sm:inline">Trade</span>
          </Button>
          <Button
            onClick={() => navigate('/watchlist')}
            className="bg-card/80 text-foreground hover:bg-card border border-white/10 py-2 px-3"
            aria-label="Watchlist"
          >
            <Star className="w-4 h-4" />
            <span className="hidden sm:inline">Watchlist</span>
          </Button>
          <Button
            onClick={handleLogout}
            disabled={isLoggingOut}
            className="bg-card/80 text-foreground hover:bg-destructive/90 hover:text-destructive-foreground border border-white/10 transition-colors py-2 px-4"
          >
            <LogOut className="w-4 h-4 mr-2" />
            <span>{isLoggingOut ? 'Logging Out...' : 'Log Out'}</span>
          </Button>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 relative z-10 mx-auto w-full max-w-3xl px-4 py-12 sm:px-8">
        {/* Header */}
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
            <div className="flex gap-2 self-start sm:self-auto">
              <Button
                id="portfolio-refresh-button"
                onClick={load}
                aria-label="Refresh portfolio"
                className="bg-card/80 hover:bg-card border border-white/10 hover:border-primary/30 text-foreground"
              >
                <RefreshCw className="h-4 w-4" />
                <span>Refresh</span>
              </Button>
              <Button
                data-testid="add-position-button"
                onClick={() => { setShowAddForm(true); setAddForm(defaultAddForm()); }}
                className="bg-primary/90 text-primary-foreground"
              >
                <Plus className="h-4 w-4" />
                <span>Add</span>
              </Button>
            </div>
          )}
        </div>

        {/* Portfolio performance */}
        <PortfolioPerformancePanel reloadToken={reloadToken} />

        {/* Total Value summary */}
        <div
          data-testid="portfolio-summary"
          className="mb-6 flex items-center justify-between gap-4 rounded-xl border border-white/10 bg-card/40 backdrop-blur-sm px-5 py-4"
        >
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/15 text-primary">
              <Wallet className="h-5 w-5" />
            </div>
            <div>
              <p className="text-xs font-medium uppercase tracking-widest text-muted-foreground">
                Total Value
              </p>
              <p className="text-xs text-muted-foreground">Based on latest stored prices</p>
            </div>
          </div>
          <div className="text-right">
            {valueStatus.kind === 'loading' && (
              <span
                data-testid="portfolio-total-value-loading"
                className="inline-flex items-center gap-2 text-muted-foreground"
              >
                <Spinner size="sm" />
                <span className="text-sm">Calculating…</span>
              </span>
            )}
            {valueStatus.kind === 'success' && (
              <span
                data-testid="portfolio-total-value"
                className="text-2xl font-extrabold tracking-tight text-foreground"
              >
                {formatUsd(valueStatus.value)}
              </span>
            )}
            {valueStatus.kind === 'error' && (
              <span data-testid="portfolio-total-value-error" className="text-sm text-destructive">
                Value unavailable
              </span>
            )}
          </div>
        </div>

        {/* Add Position Form */}
        {showAddForm && (
          <div
            data-testid="add-position-form"
            className="mb-6 rounded-xl border border-primary/40 bg-card/60 backdrop-blur-sm px-5 py-4 flex flex-col gap-3"
          >
            <div className="flex items-center justify-between mb-1">
              <h2 className="text-sm font-semibold text-foreground">New Position</h2>
              <button
                data-testid="close-add-form-button"
                onClick={() => setShowAddForm(false)}
                className="text-muted-foreground hover:text-foreground transition-colors"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="flex flex-col gap-1">
                <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">CIK</label>
                <Input
                  data-testid="add-cik-input"
                  value={addForm.cik}
                  onChange={(e) => setAddForm((f) => ({ ...f, cik: e.target.value }))}
                  placeholder="e.g. 0000320193"
                />
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">Quantity</label>
                <Input
                  data-testid="add-quantity-input"
                  type="number"
                  min={1}
                  value={addForm.quantity}
                  onChange={(e) => setAddForm((f) => ({ ...f, quantity: e.target.value }))}
                  placeholder="e.g. 10"
                />
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">Date</label>
                <Input
                  data-testid="add-date-input"
                  type="date"
                  value={addForm.operationDate}
                  onChange={(e) => setAddForm((f) => ({ ...f, operationDate: e.target.value }))}
                />
              </div>
            </div>
            {addForm.error && (
              <p className="text-xs text-destructive" role="alert">{addForm.error}</p>
            )}
            <Button
              data-testid="confirm-add-position-button"
              onClick={handleAddPosition}
              disabled={addForm.saving}
              className="self-start bg-primary text-primary-foreground py-2 px-5"
            >
              {addForm.saving ? 'Adding…' : 'Add Position'}
            </Button>
          </div>
        )}

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
                Your portfolio is empty. Use the Add button above to track your positions here.
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
                <PositionRow
                  key={position.id}
                  position={position}
                  onModify={handleModify}
                  onRemove={handleRemove}
                />
              ))}
            </div>
          </section>
        )}
      </main>
    </div>
  );
}
