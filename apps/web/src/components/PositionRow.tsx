import { useState } from 'react';
import type { Position, ModifyPositionInput } from '@ssv/ui-core';
import { TrendingUp, TrendingDown, Minus, DollarSign, Calendar, Hash, Pencil, Trash2, Check, X } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { cn } from '../lib/utils';

interface PositionRowProps {
  position: Position;
  onModify: (positionId: string, input: ModifyPositionInput) => Promise<void>;
  onRemove: (positionId: string) => Promise<void>;
}

function formatDate(iso: string): string {
  const [year, month, day] = iso.split('-').map(Number)
  const utcDate = new Date(Date.UTC(year, month - 1, day))
  return new Intl.DateTimeFormat('en-US', {
    timeZone: 'UTC',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(utcDate)
}

const pnlUsdFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  signDisplay: 'exceptZero',
});

// `pnlPercent` arrives already in percent units (e.g. 12.5 means 12.5%),
// so it is divided by 100 before being handed to the percent formatter.
const pnlPercentFormatter = new Intl.NumberFormat('en-US', {
  style: 'percent',
  signDisplay: 'exceptZero',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

function formatPnl(value: number): string {
  return pnlUsdFormatter.format(value);
}

function formatPnlPercent(value: number): string {
  return pnlPercentFormatter.format(value / 100);
}

export function PositionRow({ position, onModify, onRemove }: PositionRowProps) {
  const [editing, setEditing] = useState(false);
  const [ticker, setTicker] = useState(position.ticker);
  const [quantity, setQuantity] = useState(String(position.quantity));
  const [operationDate, setOperationDate] = useState(position.operationDate);
  const [saving, setSaving] = useState(false);
  const [removing, setRemoving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function startEdit() {
    setTicker(position.ticker);
    setQuantity(String(position.quantity));
    setOperationDate(position.operationDate);
    setError(null);
    setEditing(true);
  }

  function cancelEdit() {
    setEditing(false);
    setError(null);
  }

  async function handleSave() {
    const qty = parseInt(quantity, 10);
    if (!ticker.trim() || isNaN(qty) || qty <= 0 || !operationDate) {
      setError('All fields are required and quantity must be positive.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await onModify(position.id, { ticker: ticker.trim(), quantity: qty, operationDate });
      setEditing(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed.');
    } finally {
      setSaving(false);
    }
  }

  async function handleRemove() {
    setRemoving(true);
    try {
      await onRemove(position.id);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed.');
      setRemoving(false);
    }
  }

  if (editing) {
    return (
      <div
        data-testid={`position-row-${position.id}`}
        className="rounded-xl border border-primary/40 bg-card/60 backdrop-blur-sm px-5 py-4 flex flex-col gap-3"
      >
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">Ticker</label>
            <Input
              data-testid="edit-ticker-input"
              value={ticker}
              onChange={(e) => setTicker(e.target.value)}
              placeholder="e.g. AAPL"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">Quantity</label>
            <Input
              data-testid="edit-quantity-input"
              type="number"
              min={1}
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium uppercase tracking-widest text-muted-foreground">Date</label>
            <Input
              data-testid="edit-date-input"
              type="date"
              value={operationDate}
              onChange={(e) => setOperationDate(e.target.value)}
            />
          </div>
        </div>
        {error && <p className="text-xs text-destructive">{error}</p>}
        <div className="flex gap-2">
          <Button
            data-testid="save-position-button"
            onClick={handleSave}
            disabled={saving}
            className="bg-primary/90 text-primary-foreground py-2 px-4 text-sm"
          >
            <Check className="h-3.5 w-3.5" />
            {saving ? 'Saving…' : 'Save'}
          </Button>
          <Button
            data-testid="cancel-edit-button"
            onClick={cancelEdit}
            className="bg-card/80 text-foreground hover:bg-card border border-white/10 py-2 px-4 text-sm"
          >
            <X className="h-3.5 w-3.5" />
            Cancel
          </Button>
        </div>
      </div>
    );
  }

  const pnlDirection = position.pnl > 0 ? 'gain' : position.pnl < 0 ? 'loss' : 'flat';
  const pnlColor =
    pnlDirection === 'gain'
      ? 'text-emerald-400'
      : pnlDirection === 'loss'
        ? 'text-destructive'
        : 'text-muted-foreground';
  const PnlIcon = pnlDirection === 'gain' ? TrendingUp : pnlDirection === 'loss' ? TrendingDown : Minus;

  return (
    <div
      data-testid={`position-row-${position.id}`}
      className="group grid grid-cols-1 sm:grid-cols-4 items-center gap-4 rounded-xl border border-white/10 bg-card/40 backdrop-blur-sm px-5 py-4 transition-all duration-200 hover:border-primary/40 hover:bg-card/60 hover:shadow-lg hover:shadow-primary/5"
    >
      {/* Ticker */}
      <div className="flex items-center gap-3 min-w-0">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary transition-colors group-hover:bg-primary/20">
          <TrendingUp className="h-5 w-5" />
        </div>
        <div className="min-w-0">
          <p className="text-xs font-medium uppercase tracking-widest text-muted-foreground">Ticker</p>
          <p className="truncate text-lg font-bold text-foreground">{position.ticker}</p>
        </div>
      </div>

      {/* Quantity */}
      <div className="flex items-center gap-2 sm:flex-col sm:items-center sm:justify-self-center">
        <div className="flex items-center gap-1.5 text-muted-foreground">
          <Hash className="h-3.5 w-3.5" />
          <span className="text-xs font-medium uppercase tracking-widest">Quantity</span>
        </div>
        <p className="text-base font-semibold tabular-nums text-foreground">
          {position.quantity.toLocaleString('en-US')}
        </p>
      </div>

      {/* Profit and Loss */}
      <div className="flex items-center gap-2 sm:flex-col sm:items-center sm:justify-self-center">
        <div className="flex items-center gap-1.5 text-muted-foreground">
          <DollarSign className="h-3.5 w-3.5" />
          <span className="text-xs font-medium uppercase tracking-widest">P&amp;L</span>
        </div>
        <p
          data-testid={`position-pnl-${position.id}`}
          data-pnl-direction={pnlDirection}
          aria-label={`Profit and loss ${formatPnl(position.pnl)}, ${formatPnlPercent(position.pnlPercent)}`}
          className={cn('flex items-center gap-1 text-base font-semibold tabular-nums', pnlColor)}
        >
          <PnlIcon className="h-3.5 w-3.5" aria-hidden="true" />
          <span>{formatPnl(position.pnl)}</span>
          <span className="text-xs font-medium opacity-80">({formatPnlPercent(position.pnlPercent)})</span>
        </p>
      </div>

      {/* Date + actions */}
      <div className="flex items-center justify-between sm:justify-end gap-4">
        <div className="flex items-center gap-2 sm:flex-col sm:items-end">
          <div className="flex items-center gap-1.5 text-muted-foreground sm:justify-end">
            <Calendar className="h-3.5 w-3.5" />
            <span className="text-xs font-medium uppercase tracking-widest">Date</span>
          </div>
          <p className="text-base font-semibold text-foreground">{formatDate(position.operationDate)}</p>
        </div>
        <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <button
            data-testid={`edit-position-${position.id}`}
            onClick={startEdit}
            aria-label="Edit position"
            className="p-1.5 rounded-lg hover:bg-primary/15 text-muted-foreground hover:text-primary transition-colors"
          >
            <Pencil className="h-4 w-4" />
          </button>
          <button
            data-testid={`remove-position-${position.id}`}
            onClick={handleRemove}
            disabled={removing}
            aria-label="Remove position"
            className="p-1.5 rounded-lg hover:bg-destructive/15 text-muted-foreground hover:text-destructive transition-colors"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      </div>
      {error && (
        <p className="col-span-full text-xs text-destructive mt-1">{error}</p>
      )}
    </div>
  );
}
