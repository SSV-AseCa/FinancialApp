import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCompany } from '@ssv/ui-core';
import type { Company } from '@ssv/ui-core';
import { Search, Building2, ArrowLeft } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Spinner } from '../components/ui/Spinner';

type Status =
  | { kind: 'idle' }
  | { kind: 'loading' }
  | { kind: 'success'; results: Company[] }
  | { kind: 'error'; message: string };

export default function CompanySearchPage() {
  const company = useCompany();
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState<Status>({ kind: 'idle' });

  const handleSearch = async () => {
    const q = query.trim();
    if (!q) return;
    setStatus({ kind: 'loading' });
    try {
      const results = await company.searchCompanies(q);
      setStatus({ kind: 'success', results });
    } catch (err) {
      setStatus({ kind: 'error', message: err instanceof Error ? err.message : 'Search failed.' });
    }
  };

  const handleSelect = (c: Company) => {
    const ticker = c.tickers?.[0] ?? '';
    navigate(`/companies/${encodeURIComponent(c.cik)}?name=${encodeURIComponent(c.name)}&symbol=${encodeURIComponent(ticker)}`);
  };

  return (
    <div
      data-testid="company-search-page"
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
          Company Research
        </div>
      </header>

      <main className="flex-1 relative z-10 mx-auto w-full max-w-2xl px-4 py-12 sm:px-8">
        <div className="mb-8 flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary/15 text-primary">
            <Building2 className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-primary to-ring bg-clip-text text-transparent">
              Search Companies
            </h1>
            <p className="text-sm text-muted-foreground">Find companies by name or ticker</p>
          </div>
        </div>

        <div className="flex gap-3 mb-8">
          <Input
            data-testid="company-search-input"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            placeholder="e.g. Apple or AAPL"
            className="flex-1"
          />
          <Button
            data-testid="company-search-submit"
            onClick={handleSearch}
            disabled={status.kind === 'loading'}
            className="px-5"
          >
            <Search className="h-4 w-4" />
            Search
          </Button>
        </div>

        {status.kind === 'loading' && (
          <div className="flex justify-center py-16">
            <Spinner size="lg" />
          </div>
        )}

        {status.kind === 'error' && (
          <div role="alert" className="rounded-xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
            {status.message}
          </div>
        )}

        {status.kind === 'success' && status.results.length === 0 && (
          <p className="text-center text-muted-foreground py-16" data-testid="no-results">
            No companies found for "{query}".
          </p>
        )}

        {status.kind === 'success' && status.results.length > 0 && (
          <ul data-testid="company-search-results" className="flex flex-col gap-3">
            {status.results.map((c) => (
              <li key={c.cik}>
                <button
                  data-testid={`company-result-${c.cik}`}
                  onClick={() => handleSelect(c)}
                  className="w-full text-left rounded-xl border border-white/10 bg-card/40 backdrop-blur-sm px-5 py-4 hover:border-primary/40 hover:bg-card/60 transition-all group"
                >
                  <p className="font-semibold text-foreground group-hover:text-primary transition-colors">{c.name}</p>
                  <p className="text-xs text-muted-foreground mt-0.5">
                    CIK: {c.cik}{c.tickers?.length > 0 ? ` · ${c.tickers.join(', ')}` : ''}
                  </p>
                </button>
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}
