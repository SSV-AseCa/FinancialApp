import type { Company } from './Company'
import type { CompanyFinancialMetrics } from './CompanyFinancialMetrics'
import type { SecFiling } from './SecFiling'

export interface CompanyPort {
  searchCompanies(query: string): Promise<Company[]>
  getCompanySecFilings(cik: string): Promise<SecFiling[]>
  getCompanyFinancialMetrics(cik: string): Promise<CompanyFinancialMetrics[]>
}
