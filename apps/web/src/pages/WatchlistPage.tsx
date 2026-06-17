import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useWatchlist } from '@ssv/ui-core';
import type { WatchlistCompany, WatchlistComparison } from '@ssv/ui-core';
import { ArrowLeft, Star, AlertCircle, Trophy } from 'lucide-react';
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

  // Comparison States
  const [selectedCiks, setSelectedCiks] = useState<string[]>([]);
  const [comparisonData, setComparisonData] = useState<WatchlistComparison | null>(null);
  const [isComparing, setIsComparing] = useState(false);
  const [compareError, setCompareError] = useState<string | null>(null);

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
      setSelectedCiks((prev) => prev.filter((c) => c !== cik));
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

  const handleSelectToggle = (cik: string) => {
    setSelectedCiks((prev) =>
      prev.includes(cik) ? prev.filter((c) => c !== cik) : [...prev, cik]
    );
  };

  const handleCompare = async () => {
    if (selectedCiks.length < 2) return;
    setIsComparing(true);
    setCompareError(null);
    setComparisonData(null); // Clear previous selection data while fetching
    try {
      const res = await watchlist.compareWatchlistCompanies(selectedCiks);
      setComparisonData(res);
    } catch (err) {
      setCompareError(err instanceof Error ? err.message : 'Failed to compare companies.');
    } finally {
      setIsComparing(false);
    }
  };

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

  // Winner Highlighting Logic
  const getWinnerCik = (metricName: 'revenue' | 'netIncome' | 'assets' | 'equity') => {
    if (!comparisonData || comparisonData.companies.length < 2) return null;
    let maxVal: number | null = null;
    let winnerCik: string | null = null;

    for (const c of comparisonData.companies) {
      const val = c.metrics?.[metricName];
      if (val !== undefined && val !== null) {
        if (maxVal === null || val > maxVal) {
          maxVal = val;
          winnerCik = c.cik;
        }
      }
    }
    return winnerCik;
  };

  const getMaxAbsValue = (metricName: 'revenue' | 'netIncome' | 'assets' | 'equity') => {
    if (!comparisonData) return 0;
    return Math.max(
      ...comparisonData.companies.map((c) => Math.abs(c.metrics?.[metricName] || 0))
    );
  };

  const renderComparisonBar = (val: number | undefined | null, maxVal: number, isPositive: boolean) => {
    if (val === undefined || val === null || maxVal === 0) return null;
    const pct = Math.min(100, Math.max(0, (Math.abs(val) / maxVal) * 100));
    return (
      <div className="w-full bg-white/5 h-1 rounded-full overflow-hidden mt-1.5">
        <div
          className={`h-full rounded-full transition-all duration-500 ${
            isPositive ? 'bg-primary/40' : 'bg-rose-500/40'
          }`}
          style={{ width: `${pct}%` }}
        />
      </div>
    );
  };

  const winnerRevenueCik = getWinnerCik('revenue');
  const winnerNetIncomeCik = getWinnerCik('netIncome');
  const winnerAssetsCik = getWinnerCik('assets');
  const winnerEquityCik = getWinnerCik('equity');

  const maxRevenue = getMaxAbsValue('revenue');
  const maxNetIncome = getMaxAbsValue('netIncome');
  const maxAssets = getMaxAbsValue('assets');
  const maxEquity = getMaxAbsValue('equity');

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

        {/* Comparison Skeletons */}
        {isComparing && (
          <div
            data-testid="comparison-loading"
            className="mb-8 rounded-2xl border border-primary/20 bg-card/40 backdrop-blur-sm p-6 flex flex-col gap-4 relative shadow-lg animate-pulse"
          >
            <div className="flex justify-between items-start mb-2 gap-4">
              <div>
                <div className="h-5 w-40 bg-white/10 rounded-md mb-2" />
                <div className="h-3 w-64 bg-white/5 rounded-md" />
              </div>
            </div>
            <div className="overflow-x-auto border border-white/5 rounded-xl bg-black/10">
              <table className="w-full border-collapse text-left text-sm min-w-[500px]">
                <thead>
                  <tr className="border-b border-white/5 bg-white/5">
                    <th className="p-3 w-1/4"><div className="h-4 bg-white/10 rounded w-16" /></th>
                    {selectedCiks.map((cik) => (
                      <th key={cik} className="p-3"><div className="h-4 bg-white/10 rounded w-24" /></th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {[1, 2, 3, 4].map((i) => (
                    <tr key={i} className="border-b border-white/5">
                      <td className="p-3"><div className="h-4 bg-white/5 rounded w-16" /></td>
                      {selectedCiks.map((cik) => (
                        <td key={cik} className="p-3"><div className="h-4 bg-white/5 rounded w-20" /></td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Comparison Results Card */}
        {comparisonData && (
          <div
            data-testid="comparison-view"
            className="mb-8 rounded-2xl border border-primary/30 bg-card/60 backdrop-blur-sm p-6 flex flex-col gap-4 relative shadow-lg animate-fadeIn"
          >
            <div className="flex justify-between items-start mb-2 gap-4">
              <div>
                <h2 className="text-xl font-bold bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent">
                  Company Comparison
                </h2>
                <p className="text-xs text-muted-foreground">Side-by-side financial metrics comparison</p>
              </div>
              <Button
                data-testid="close-comparison"
                onClick={() => setComparisonData(null)}
                className="bg-white/5 border border-white/10 hover:bg-white/10 text-foreground py-1.5 px-3 rounded-lg text-xs transition-all"
              >
                Close Comparison
              </Button>
            </div>

            <div className="overflow-x-auto border border-white/10 rounded-xl bg-black/20">
              <table className="w-full border-collapse text-left text-sm min-w-[500px]">
                <thead>
                  <tr className="border-b border-white/10 bg-white/5">
                    <th className="p-3 font-semibold text-muted-foreground w-1/4">Metric</th>
                    {comparisonData.companies.map((c) => (
                      <th key={c.cik} className="p-3 font-bold text-foreground">
                        <div>{c.name}</div>
                        {c.symbol && (
                          <span className="text-xs font-normal text-muted-foreground uppercase bg-white/5 px-2 py-0.5 rounded border border-white/10 mt-1 inline-block">
                            {c.symbol}
                          </span>
                        )}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/5">
                  <tr>
                    <td className="p-3 font-medium text-muted-foreground">Revenue</td>
                    {comparisonData.companies.map((c) => {
                      const val = c.metrics?.revenue;
                      const isWinner = c.cik === winnerRevenueCik;
                      return (
                        <td
                          key={c.cik}
                          className={`p-3 font-semibold transition-all ${isWinner ? 'bg-emerald-500/5' : ''}`}
                          data-testid={`compare-revenue-${c.cik}`}
                        >
                          <div className="flex items-center gap-1.5">
                            <span>{formatCurrency(val)}</span>
                            {isWinner && <Trophy className="w-3.5 h-3.5 text-yellow-500 shrink-0" title="Highest Revenue" />}
                          </div>
                          {renderComparisonBar(val, maxRevenue, true)}
                        </td>
                      );
                    })}
                  </tr>
                  <tr>
                    <td className="p-3 font-medium text-muted-foreground">Net Income</td>
                    {comparisonData.companies.map((c) => {
                      const netIncome = c.metrics?.netIncome;
                      const isPositive = netIncome !== undefined && netIncome >= 0;
                      const isWinner = c.cik === winnerNetIncomeCik;
                      return (
                        <td
                          key={c.cik}
                          className={`p-3 font-semibold transition-all ${isWinner ? 'bg-emerald-500/5' : ''} ${
                            netIncome !== undefined ? (isPositive ? 'text-emerald-400' : 'text-rose-400') : 'text-foreground'
                          }`}
                          data-testid={`compare-net-income-${c.cik}`}
                        >
                          <div className="flex items-center gap-1.5">
                            <span>{formatCurrency(netIncome)}</span>
                            {isWinner && <Trophy className="w-3.5 h-3.5 text-yellow-500 shrink-0" title="Highest Net Income" />}
                          </div>
                          {renderComparisonBar(netIncome, maxNetIncome, isPositive)}
                        </td>
                      );
                    })}
                  </tr>
                  <tr>
                    <td className="p-3 font-medium text-muted-foreground">Assets</td>
                    {comparisonData.companies.map((c) => {
                      const val = c.metrics?.assets;
                      const isWinner = c.cik === winnerAssetsCik;
                      return (
                        <td
                          key={c.cik}
                          className={`p-3 transition-all ${isWinner ? 'bg-emerald-500/5 font-semibold' : 'text-foreground'}`}
                          data-testid={`compare-assets-${c.cik}`}
                        >
                          <div className="flex items-center gap-1.5">
                            <span>{formatCurrency(val)}</span>
                            {isWinner && <Trophy className="w-3.5 h-3.5 text-yellow-500 shrink-0" title="Highest Assets" />}
                          </div>
                          {renderComparisonBar(val, maxAssets, true)}
                        </td>
                      );
                    })}
                  </tr>
                  <tr>
                    <td className="p-3 font-medium text-muted-foreground">Equity</td>
                    {comparisonData.companies.map((c) => {
                      const val = c.metrics?.equity;
                      const isWinner = c.cik === winnerEquityCik;
                      return (
                        <td
                          key={c.cik}
                          className={`p-3 transition-all ${isWinner ? 'bg-emerald-500/5 font-semibold' : 'text-foreground'}`}
                          data-testid={`compare-equity-${c.cik}`}
                        >
                          <div className="flex items-center gap-1.5">
                            <span>{formatCurrency(val)}</span>
                            {isWinner && <Trophy className="w-3.5 h-3.5 text-yellow-500 shrink-0" title="Highest Equity" />}
                          </div>
                          {renderComparisonBar(val, maxEquity, true)}
                        </td>
                      );
                    })}
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        )}

        {compareError && (
          <div
            className="mb-6 rounded-xl border border-destructive/30 bg-destructive/10 p-4 flex items-start gap-3 text-sm text-destructive"
          >
            <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
            <div className="flex-1">
              <p className="font-semibold">Failed to compare companies</p>
              <p className="mt-0.5 opacity-90">{compareError}</p>
            </div>
          </div>
        )}

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
          <>
            <div className="mb-6 flex flex-col sm:flex-row gap-4 items-center justify-between bg-card/45 backdrop-blur-sm border border-white/5 p-4 rounded-2xl">
              <div>
                <p className="text-sm font-semibold text-foreground">Compare Companies</p>
                <p className="text-xs text-muted-foreground">
                  Select 2 or more companies to compare their metrics side-by-side.
                </p>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-xs text-muted-foreground">
                  {selectedCiks.length} selected
                </span>
                {selectedCiks.length > 0 && (
                  <Button
                    onClick={() => setSelectedCiks([])}
                    className="bg-white/5 border border-white/10 hover:bg-white/10 text-muted-foreground hover:text-foreground py-1.5 px-3 rounded-lg text-xs transition-all"
                  >
                    Clear All
                  </Button>
                )}
                <Button
                  data-testid="compare-button"
                  disabled={selectedCiks.length < 2 || isComparing}
                  onClick={handleCompare}
                  className="bg-primary hover:bg-primary/95 text-primary-foreground font-semibold py-2 px-4 rounded-xl disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                >
                  {isComparing ? 'Comparing...' : 'Compare Selected'}
                </Button>
              </div>
            </div>

            <div className="flex flex-col gap-4">
              {status.data.map((c) => (
                <WatchlistItem
                  key={c.cik}
                  company={c}
                  isRemoving={removingCik === c.cik}
                  onRemove={handleRemove}
                  onNavigate={handleNavigateToCompany}
                  isSelected={selectedCiks.includes(c.cik)}
                  onSelectToggle={handleSelectToggle}
                />
              ))}
            </div>
          </>
        )}
      </main>
    </div>
  );
}
