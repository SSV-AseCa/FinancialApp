import type { FinancialMetrics } from './FinancialMetrics'

export interface WatchlistCompany {
  companyId: string
  cik: string
  symbol: string
  name: string
  metrics: FinancialMetrics
}
