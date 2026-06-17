import type { Transaction } from '@ssv/ui-core';
import { TrendingUp, TrendingDown } from 'lucide-react';

interface TransactionItemProps {
  transaction: Transaction;
}

export function TransactionItem({ transaction }: TransactionItemProps) {
  const isBuy = transaction.type === 'BUY';
  const Icon = isBuy ? TrendingUp : TrendingDown;
  const iconColorClass = isBuy ? 'text-primary' : 'text-destructive';

  return (
    <li
      data-testid={`transaction-${transaction.id}`}
      className="flex items-center justify-between rounded-xl border border-white/10 bg-card/40 px-4 py-3"
    >
      <div className="flex items-center gap-3">
        <Icon className={`h-4 w-4 ${iconColorClass}`} />
        <div>
          <p className="text-sm font-semibold text-foreground">
            {transaction.type} · {transaction.quantity.toLocaleString()} shares
          </p>
          <p className="text-xs text-muted-foreground">CIK: {transaction.cik}</p>
        </div>
      </div>
      <p className="text-xs text-muted-foreground">{transaction.transactionDate}</p>
    </li>
  );
}
