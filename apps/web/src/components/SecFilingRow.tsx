import type { SecFiling } from '@ssv/ui-core';
import { FileText, Calendar } from 'lucide-react';

interface SecFilingRowProps {
  filing: SecFiling;
  'data-testid'?: string;
}

export function SecFilingRow({ filing, 'data-testid': testId }: SecFilingRowProps) {
  return (
    <div
      data-testid={testId}
      className="flex items-start gap-4 rounded-xl border border-white/10 bg-card/40 backdrop-blur-sm px-5 py-4 hover:border-primary/30 hover:bg-card/60 transition-all duration-200"
    >
      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/15 text-primary">
        <FileText className="h-5 w-5" />
      </div>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span
            data-testid="filing-form-type"
            className="rounded-md border border-primary/30 bg-primary/10 px-2 py-0.5 text-sm font-bold text-primary"
          >
            {filing.formType}
          </span>
          <span
            data-testid="filing-date"
            className="flex items-center gap-1 text-xs text-muted-foreground"
          >
            <Calendar className="h-3.5 w-3.5" />
            {filing.filingDate}
          </span>
        </div>
        {filing.description && (
          <p className="mt-1.5 text-sm text-foreground/90">{filing.description}</p>
        )}
      </div>
    </div>
  );
}
