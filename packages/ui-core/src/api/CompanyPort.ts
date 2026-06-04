import type { Company } from './Company'
import type { CompanyDetails } from './CompanyDetails'

export interface CompanyPort {
  searchCompanies(query: string): Promise<Company[]>
  getCompanyDetails(cik: string, name: string, symbol: string): Promise<CompanyDetails>
}
