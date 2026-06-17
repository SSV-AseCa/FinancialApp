import { useState } from 'react';
import { TrendingUp, TrendingDown } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Spinner } from './ui/Spinner';

export type TradeStatus =
  | { kind: 'idle' }
  | { kind: 'loading' }
  | { kind: 'success'; message: string }
  | { kind: 'error'; message: string };

interface TradeFormProps {
  type: 'BUY' | 'SELL';
  status: TradeStatus;
  onSubmit: (cik: string, quantity: number) => Promise<boolean>;
  onClearStatus: () => void;
}

export function TradeForm({ type, status, onSubmit, onClearStatus }: TradeFormProps) {
  const [cik, setCik] = useState('');
  const [qty, setQty] = useState('');
  const [validationError, setValidationError] = useState<string | null>(null);

  const isBuy = type === 'BUY';
  const Icon = isBuy ? TrendingUp : TrendingDown;
  const colorClass = isBuy ? 'text-primary' : 'text-destructive';
  const buttonClass = isBuy
    ? 'bg-primary text-primary-foreground py-2 px-5'
    : 'bg-destructive/80 text-white hover:bg-destructive py-2 px-5';

  const handleInputChange = (field: 'cik' | 'qty', value: string) => {
    if (field === 'cik') {
      setCik(value);
    } else {
      setQty(value);
    }
    // Clear status and validation errors on typing
    if (status.kind !== 'idle') {
      onClearStatus();
    }
    if (validationError) {
      setValidationError(null);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const parsedQty = parseInt(qty, 10);
    if (!cik.trim() || isNaN(parsedQty) || parsedQty <= 0) {
      setValidationError('CIK and a positive quantity are required.');
      return;
    }
    setValidationError(null);
    const success = await onSubmit(cik.trim(), parsedQty);
    if (success) {
      setCik('');
      setQty('');
    }
  };

  return (
    <section
      data-testid={`${type.toLowerCase()}-section`}
      className="rounded-xl border border-white/10 bg-card/40 px-5 py-5"
    >
      <div className="flex items-center gap-2 mb-4">
        <Icon className={`h-5 w-5 ${colorClass}`} />
        <h2 className="text-lg font-bold text-foreground">
          {isBuy ? 'Buy Shares' : 'Sell Shares'}
        </h2>
      </div>
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">
              Company CIK
            </label>
            <Input
              data-testid={`${type.toLowerCase()}-cik-input`}
              value={cik}
              onChange={(e) => handleInputChange('cik', e.target.value)}
              placeholder="e.g. 0000320193"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">
              Quantity
            </label>
            <Input
              data-testid={`${type.toLowerCase()}-quantity-input`}
              type="number"
              min={1}
              value={qty}
              onChange={(e) => handleInputChange('qty', e.target.value)}
              placeholder="e.g. 5"
            />
          </div>
        </div>

        {validationError && (
          <p className="text-xs text-destructive mb-2" role="alert">
            {validationError}
          </p>
        )}
        {status.kind === 'error' && (
          <p className="text-xs text-destructive mb-2" role="alert">
            {status.message}
          </p>
        )}
        {status.kind === 'success' && (
          <p className="text-xs text-green-500 mb-2" data-testid={`${type.toLowerCase()}-success`}>
            {status.message}
          </p>
        )}

        <Button
          data-testid={`${type.toLowerCase()}-submit-button`}
          type="submit"
          disabled={status.kind === 'loading'}
          className={buttonClass}
        >
          {status.kind === 'loading' ? <Spinner size="sm" /> : <Icon className="h-4 w-4" />}
          {status.kind === 'loading' ? (isBuy ? 'Buying…' : 'Selling…') : (isBuy ? 'Buy' : 'Sell')}
        </Button>
      </form>
    </section>
  );
}
