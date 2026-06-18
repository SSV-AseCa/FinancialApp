import type { Company } from './Company'
import type { CompanyFinancialMetrics } from './CompanyFinancialMetrics'
import type { HistoricalDataPoint } from './HistoricalDataPoint'
import type { SecFiling } from './SecFiling'

export interface CompanyPort {
  searchCompanies(query: string): Promise<Company[]>
  getCompanySecFilings(cik: string): Promise<SecFiling[]>
  getCompanyFinancialMetrics(cik: string): Promise<CompanyFinancialMetrics[]>
  getCompanyHistoricalData(cik: string): Promise<HistoricalDataPoint[]>
}
