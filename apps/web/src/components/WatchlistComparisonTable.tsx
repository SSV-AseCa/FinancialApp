import type { WatchlistComparison } from '@ssv/ui-core';
import { Trophy } from 'lucide-react';
import { Button } from './ui/button';

interface WatchlistComparisonTableProps {
  comparisonData: WatchlistComparison;
  onClose: () => void;
}

export function WatchlistComparisonTable({ comparisonData, onClose }: WatchlistComparisonTableProps) {
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

  const getWinnerCik = (metricName: 'revenue' | 'netIncome' | 'assets' | 'equity') => {
    if (comparisonData.companies.length < 2) return null;
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
          onClick={onClose}
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
  );
}
