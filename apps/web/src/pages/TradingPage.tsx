import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTrading } from '@ssv/ui-core';
import type { Transaction } from '@ssv/ui-core';
import { ArrowLeft, Clock, Search } from 'lucide-react';
import { Spinner } from '../components/ui/Spinner';
import { TradeForm } from '../components/TradeForm';
import type { TradeStatus } from '../components/TradeForm';
import { TransactionItem } from '../components/TransactionItem';

type HistoryStatus = { kind: 'loading' } | { kind: 'success'; data: Transaction[] } | { kind: 'error'; message: string };

export default function TradingPage() {
  const trading = useTrading();
  const navigate = useNavigate();

  const [buyStatus, setBuyStatus] = useState<TradeStatus>({ kind: 'idle' });
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

  const handleBuy = async (cik: string, quantity: number): Promise<boolean> => {
    setBuyStatus({ kind: 'loading' });
    try {
      await trading.buyShares({ cik, quantity });
      setBuyStatus({ kind: 'success', message: `Bought ${quantity} shares of CIK ${cik}.` });
      loadHistory();
      return true;
    } catch (err) {
      setBuyStatus({ kind: 'error', message: err instanceof Error ? err.message : 'Buy failed.' });
      return false;
    }
  };

  const handleSell = async (cik: string, quantity: number): Promise<boolean> => {
    setSellStatus({ kind: 'loading' });
    try {
      await trading.sellShares({ cik, quantity });
      setSellStatus({ kind: 'success', message: `Sold ${quantity} shares of CIK ${cik}.` });
      loadHistory();
      return true;
    } catch (err) {
      setSellStatus({ kind: 'error', message: err instanceof Error ? err.message : 'Sell failed.' });
      return false;
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
        <TradeForm
          type="BUY"
          status={buyStatus}
          onSubmit={handleBuy}
          onClearStatus={() => setBuyStatus({ kind: 'idle' })}
        />

        {/* Sell */}
        <TradeForm
          type="SELL"
          status={sellStatus}
          onSubmit={handleSell}
          onClearStatus={() => setSellStatus({ kind: 'idle' })}
        />

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
                <TransactionItem key={tx.id} transaction={tx} />
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  );
}
