import type { CompanyFinancialMetrics } from '@ssv/ui-core';

function formatValue(value: number, unit: string): string {
  const u = unit.trim().toUpperCase();
  if (u === '%' || u === 'PERCENT') {
    return `${value.toFixed(2)}%`;
  }
  if (u === 'USD' || u === '$') {
    const abs = Math.abs(value);
    const sign = value < 0 ? '-' : '';
    if (abs >= 1e12) return `${sign}$${(abs / 1e12).toFixed(2)}T`;
    if (abs >= 1e9) return `${sign}$${(abs / 1e9).toFixed(2)}B`;
    if (abs >= 1e6) return `${sign}$${(abs / 1e6).toFixed(2)}M`;
    if (abs >= 1e3) return `${sign}$${(abs / 1e3).toFixed(2)}K`;
    return `${sign}$${abs.toLocaleString()}`;
  }
  return value.toLocaleString(undefined, { maximumFractionDigits: 4 });
}

function formatUnit(unit: string): string {
  const u = unit.trim().toUpperCase();
  if (u === '%' || u === 'PERCENT' || u === 'USD' || u === '$') return '';
  return unit;
}

interface MetricCardProps {
  metric: CompanyFinancialMetrics;
  'data-testid'?: string;
}

export function MetricCard({ metric, 'data-testid': testId }: MetricCardProps) {
  const displayValue = formatValue(metric.value, metric.unit);
  const displayUnit = formatUnit(metric.unit);

  return (
    <div
      data-testid={testId}
      className="rounded-xl border border-white/10 bg-card/40 backdrop-blur-sm px-5 py-4 flex flex-col gap-2 hover:border-primary/30 hover:bg-card/60 transition-all duration-200"
    >
      <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground truncate">
        {metric.metric}
      </p>
      <div className="flex items-baseline gap-1.5 min-w-0">
        <span className="text-2xl font-extrabold text-foreground truncate">
          {displayValue}
        </span>
        {displayUnit && (
          <span className="text-sm text-muted-foreground shrink-0">{displayUnit}</span>
        )}
      </div>
      <p className="text-xs text-muted-foreground">
        Period ending {metric.periodEnd}
      </p>
    </div>
  );
}
