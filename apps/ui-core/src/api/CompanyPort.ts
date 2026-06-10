import type { Company } from './Company'

export interface CompanyPort {
  searchCompanies(query: string): Promise<Company[]>
}
