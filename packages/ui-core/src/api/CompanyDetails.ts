import type { FinancialMetric } from './FinancialMetric'
import type { SecFilingItem } from './SecFilingItem'

export interface CompanyDetails {
  cik: string
  symbol: string
  name: string
  financialMetrics: FinancialMetric[]
  filings: SecFilingItem[]
}
