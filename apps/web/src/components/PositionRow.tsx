import type { Position } from '@ssv/ui-core';
import { TrendingUp, Calendar, Hash } from 'lucide-react';

interface PositionRowProps {
  position: Position;
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

export function PositionRow({ position }: PositionRowProps) {
  return (
    <div
      data-testid={`position-row-${position.id}`}
      className="group grid grid-cols-1 sm:grid-cols-3 items-center gap-4 rounded-xl border border-white/10 bg-card/40 backdrop-blur-sm px-5 py-4 transition-all duration-200 hover:border-primary/40 hover:bg-card/60 hover:shadow-lg hover:shadow-primary/5"
    >
      {/* Ticker */}
      <div className="flex items-center gap-3 min-w-0">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary transition-colors group-hover:bg-primary/20">
          <TrendingUp className="h-5 w-5" />
        </div>
        <div className="min-w-0">
          <p className="text-xs font-medium uppercase tracking-widest text-muted-foreground">
            Ticker
          </p>
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

      {/* Operation Date */}
      <div className="flex items-center gap-2 sm:flex-col sm:items-end sm:justify-self-end">
        <div className="flex items-center gap-1.5 text-muted-foreground sm:justify-end">
          <Calendar className="h-3.5 w-3.5" />
          <span className="text-xs font-medium uppercase tracking-widest">Date</span>
        </div>
        <p className="text-base font-semibold text-foreground">{formatDate(position.operationDate)}</p>
      </div>
    </div>
  );
}
