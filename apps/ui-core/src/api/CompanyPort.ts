import type { Company } from './Company'
import type { CompanyFinancialMetrics } from './CompanyFinancialMetrics'
import type { CompanyQuery } from './CompanyQuery'
import type { HistoricalDataPoint } from './HistoricalDataPoint'
import type { Page } from './Page'
import type { SecFiling } from './SecFiling'

export interface CompanyPort {
  searchCompanies(query: string): Promise<Company[]>
  getCompanySecFilings(cik: string, options?: CompanyQuery): Promise<Page<SecFiling>>
  getCompanyFinancialMetrics(cik: string, options?: CompanyQuery): Promise<Page<CompanyFinancialMetrics>>
  getCompanyHistoricalData(cik: string): Promise<HistoricalDataPoint[]>
}
