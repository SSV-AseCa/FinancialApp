import type { WatchlistCompany } from "@ssv/ui-core";

export const AAPL_CIK = "0000320193";
export const MSFT_CIK = "0000789019";

export const AAPL_COMPANY: WatchlistCompany = {
  companyId: "mock-co-id-" + AAPL_CIK,
  cik: AAPL_CIK,
  symbol: "AAPL",
  name: "Apple Inc.",
  metrics: {
    revenue: 391000000000,
    netIncome: 93000000000,
    assets: 365000000000,
    equity: 62000000000
  }
};

export const MSFT_COMPANY: WatchlistCompany = {
  companyId: "mock-co-id-" + MSFT_CIK,
  cik: MSFT_CIK,
  symbol: "MSFT",
  name: "Microsoft Corporation",
  metrics: {
    revenue: 245000000000,
    netIncome: -12000000000,
    assets: 410000000000,
    equity: 110000000000
  }
};
