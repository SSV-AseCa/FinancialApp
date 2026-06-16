import type { Company } from './Company'
import type { CompanyFinancialMetrics } from './CompanyFinancialMetrics'
import type { HistoricalDataPoint } from './HistoricalDataPoint'

export interface CompanyPort {
  searchCompanies(query: string): Promise<Company[]>
  getCompanyFinancialMetrics(cik: string): Promise<CompanyFinancialMetrics[]>
  getCompanyHistoricalData(cik: string): Promise<HistoricalDataPoint[]>
}
