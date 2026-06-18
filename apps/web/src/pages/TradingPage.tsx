import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTrading } from '@ssv/ui-core';
import type { Transaction } from '@ssv/ui-core';
import { ArrowLeft, TrendingUp, TrendingDown, Clock, Search } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Spinner } from '../components/ui/Spinner';

type TradeStatus = { kind: 'idle' } | { kind: 'loading' } | { kind: 'success'; message: string } | { kind: 'error'; message: string };
type HistoryStatus = { kind: 'loading' } | { kind: 'success'; data: Transaction[] } | { kind: 'error'; message: string };

export default function TradingPage() {
  const trading = useTrading();
  const navigate = useNavigate();

  const [buyCik, setBuyCik] = useState('');
  const [buyQty, setBuyQty] = useState('');
  const [buyStatus, setBuyStatus] = useState<TradeStatus>({ kind: 'idle' });

  const [sellCik, setSellCik] = useState('');
  const [sellQty, setSellQty] = useState('');
  const [sellStatus, setSellStatus] = useState<TradeStatus>({ kind: 'idle' });

  const [historyStatus, setHistoryStatus] = useState<HistoryStatus>({ kind: 'loading' });

  const doFetchHistory = useCallback(() => {
    trading
      .getTransactionHistory()
      .then((data) => setHistoryStatus({ kind: 'success', data }))
      .catch((err: unknown) => {
        setHistoryStatus({ kind: 'error', message: err instanceof Error ? err.message : 'Failed to load history.' });
      });
  }, [trading]);

  const loadHistory = useCallback(() => {
    setHistoryStatus({ kind: 'loading' });
    doFetchHistory();
  }, [doFetchHistory]);

  useEffect(() => { doFetchHistory(); }, [doFetchHistory]);

  const handleBuy = async () => {
    const qty = parseInt(buyQty, 10);
    if (!buyCik.trim() || isNaN(qty) || qty <= 0) {
      setBuyStatus({ kind: 'error', message: 'CIK and a positive quantity are required.' });
      return;
    }
    setBuyStatus({ kind: 'loading' });
    try {
      await trading.buyShares({ cik: buyCik.trim(), quantity: qty });
      setBuyStatus({ kind: 'success', message: `Bought ${qty} shares of CIK ${buyCik.trim()}.` });
      setBuyCik('');
      setBuyQty('');
      loadHistory();
    } catch (err) {
      setBuyStatus({ kind: 'error', message: err instanceof Error ? err.message : 'Buy failed.' });
    }
  };

  const handleSell = async () => {
    const qty = parseInt(sellQty, 10);
    if (!sellCik.trim() || isNaN(qty) || qty <= 0) {
      setSellStatus({ kind: 'error', message: 'CIK and a positive quantity are required.' });
      return;
    }
    setSellStatus({ kind: 'loading' });
    try {
      await trading.sellShares({ cik: sellCik.trim(), quantity: qty });
      setSellStatus({ kind: 'success', message: `Sold ${qty} shares of CIK ${sellCik.trim()}.` });
      setSellCik('');
      setSellQty('');
      loadHistory();
    } catch (err) {
      setSellStatus({ kind: 'error', message: err instanceof Error ? err.message : 'Sell failed.' });
    }
  };

  return (
    <div
      data-testid="trading-page"
      className="min-h-screen bg-background text-foreground flex flex-col relative overflow-hidden"
    >
      <div className="pointer-events-none absolute top-[-10%] left-[-10%] h-[40%] w-[40%] rounded-full bg-primary/10 blur-[120px]" />

      <header className="w-full border-b border-white/10 bg-card/50 backdrop-blur-md px-6 py-4 flex items-center gap-4 sticky top-0 z-50">
        <button
          onClick={() => navigate('/portfolio')}
          className="text-muted-foreground hover:text-foreground transition-colors"
          aria-label="Back to portfolio"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div className="text-xl font-bold bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent tracking-wide">
          Trading
        </div>
        <button
          onClick={() => navigate('/companies')}
          className="ml-auto text-muted-foreground hover:text-foreground transition-colors"
          aria-label="Search companies"
          title="Search companies to find CIKs"
        >
          <Search className="h-5 w-5" />
        </button>
      </header>

      <main className="flex-1 relative z-10 mx-auto w-full max-w-2xl px-4 py-12 sm:px-8 flex flex-col gap-8">
        {/* Buy */}
        <section
          data-testid="buy-section"
          className="rounded-xl border border-white/10 bg-card/40 px-5 py-5"
        >
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp className="h-5 w-5 text-primary" />
            <h2 className="text-lg font-bold text-foreground">Buy Shares</h2>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-3">
            <div className="flex flex-col gap-1">
              <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">Company CIK</label>
              <Input
                data-testid="buy-cik-input"
                value={buyCik}
                onChange={(e) => setBuyCik(e.target.value)}
                placeholder="e.g. 0000320193"
              />
            </div>
            <div className="flex flex-col gap-1">
              <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">Quantity</label>
              <Input
                data-testid="buy-quantity-input"
                type="number"
                min={1}
                value={buyQty}
                onChange={(e) => setBuyQty(e.target.value)}
                placeholder="e.g. 5"
              />
            </div>
          </div>
          {buyStatus.kind === 'error' && (
            <p className="text-xs text-destructive mb-2" role="alert">{buyStatus.message}</p>
          )}
          {buyStatus.kind === 'success' && (
            <p className="text-xs text-green-500 mb-2" data-testid="buy-success">{buyStatus.message}</p>
          )}
          <Button
            data-testid="buy-submit-button"
            onClick={handleBuy}
            disabled={buyStatus.kind === 'loading'}
            className="bg-primary text-primary-foreground py-2 px-5"
          >
            {buyStatus.kind === 'loading' ? <Spinner size="sm" /> : <TrendingUp className="h-4 w-4" />}
            {buyStatus.kind === 'loading' ? 'Buying…' : 'Buy'}
          </Button>
        </section>

        {/* Sell */}
        <section
          data-testid="sell-section"
          className="rounded-xl border border-white/10 bg-card/40 px-5 py-5"
        >
          <div className="flex items-center gap-2 mb-4">
            <TrendingDown className="h-5 w-5 text-destructive" />
            <h2 className="text-lg font-bold text-foreground">Sell Shares</h2>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-3">
            <div className="flex flex-col gap-1">
              <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">Company CIK</label>
              <Input
                data-testid="sell-cik-input"
                value={sellCik}
                onChange={(e) => setSellCik(e.target.value)}
                placeholder="e.g. 0000320193"
              />
            </div>
            <div className="flex flex-col gap-1">
              <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">Quantity</label>
              <Input
                data-testid="sell-quantity-input"
                type="number"
                min={1}
                value={sellQty}
                onChange={(e) => setSellQty(e.target.value)}
                placeholder="e.g. 5"
              />
            </div>
          </div>
          {sellStatus.kind === 'error' && (
            <p className="text-xs text-destructive mb-2" role="alert">{sellStatus.message}</p>
          )}
          {sellStatus.kind === 'success' && (
            <p className="text-xs text-green-500 mb-2" data-testid="sell-success">{sellStatus.message}</p>
          )}
          <Button
            data-testid="sell-submit-button"
            onClick={handleSell}
            disabled={sellStatus.kind === 'loading'}
            className="bg-destructive/80 text-white hover:bg-destructive py-2 px-5"
          >
            {sellStatus.kind === 'loading' ? <Spinner size="sm" /> : <TrendingDown className="h-4 w-4" />}
            {sellStatus.kind === 'loading' ? 'Selling…' : 'Sell'}
          </Button>
        </section>

        {/* Transaction History */}
        <section data-testid="transaction-history-section">
          <div className="flex items-center gap-2 mb-4">
            <Clock className="h-5 w-5 text-primary" />
            <h2 className="text-lg font-bold text-foreground">Transaction History</h2>
          </div>

          {historyStatus.kind === 'loading' && (
            <div className="flex justify-center py-8"><Spinner /></div>
          )}

          {historyStatus.kind === 'error' && (
            <p className="text-sm text-destructive" role="alert">{historyStatus.message}</p>
          )}

          {historyStatus.kind === 'success' && historyStatus.data.length === 0 && (
            <p className="text-sm text-muted-foreground" data-testid="no-transactions">
              No transactions yet.
            </p>
          )}

          {historyStatus.kind === 'success' && historyStatus.data.length > 0 && (
            <ul data-testid="transactions-list" className="flex flex-col gap-2">
              {historyStatus.data.map((tx) => (
                <li
                  key={tx.id}
                  data-testid={`transaction-${tx.id}`}
                  className="flex items-center justify-between rounded-xl border border-white/10 bg-card/40 px-4 py-3"
                >
                  <div className="flex items-center gap-3">
                    {tx.type === 'BUY' ? (
                      <TrendingUp className="h-4 w-4 text-primary" />
                    ) : (
                      <TrendingDown className="h-4 w-4 text-destructive" />
                    )}
                    <div>
                      <p className="text-sm font-semibold text-foreground">
                        {tx.type} · {tx.quantity.toLocaleString()} shares
                      </p>
                      <p className="text-xs text-muted-foreground">CIK: {tx.cik}</p>
                    </div>
                  </div>
                  <p className="text-xs text-muted-foreground">{tx.transactionDate}</p>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  );
}
