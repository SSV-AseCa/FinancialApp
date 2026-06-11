import type { Company } from './Company'
import type { CompanyFinancialMetrics } from './CompanyFinancialMetrics'

export interface CompanyPort {
  searchCompanies(query: string): Promise<Company[]>
  getCompanyFinancialMetrics(cik: string): Promise<CompanyFinancialMetrics[]>
}
