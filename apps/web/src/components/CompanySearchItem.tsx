import type { Company } from '@ssv/ui-core';

interface CompanySearchItemProps {
  company: Company;
  onClick: () => void;
}

export function CompanySearchItem({ company, onClick }: CompanySearchItemProps) {
  return (
    <li>
      <button
        data-testid={`company-result-${company.cik}`}
        onClick={onClick}
        className="w-full text-left rounded-xl border border-white/10 bg-card/40 backdrop-blur-sm px-5 py-4 cursor-pointer hover:bg-card/60 hover:border-primary/30 transition-all focus:outline-none focus:ring-1 focus:ring-primary/40"
      >
        <p className="font-semibold text-foreground">{company.name}</p>
        <p className="text-xs text-muted-foreground mt-0.5">
          CIK: {company.cik}
          {company.tickers && company.tickers.length > 0 ? ` · ${company.tickers.join(', ')}` : ''}
        </p>
      </button>
    </li>
  );
}
