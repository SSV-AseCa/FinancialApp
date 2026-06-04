import { renderHook } from '@testing-library/react'
import type { ReactNode } from 'react'
import { describe, expect, it } from 'vitest'
import type { CompanyPort } from '../CompanyPort'
import { CompanyProvider } from '../CompanyProvider'
import { useCompany } from '../useCompany'

const fakeCompany: CompanyPort = {
  searchCompanies: async () => [],
  getCompanyDetails: async () => ({ cik: '', symbol: '', name: '', financialMetrics: [], filings: [] }),
}

function wrapper({ children }: { children: ReactNode }) {
  return <CompanyProvider port={fakeCompany}>{children}</CompanyProvider>
}

describe('useCompany', () => {
  it('returns the CompanyPort provided by CompanyProvider', () => {
    const { result } = renderHook(() => useCompany(), { wrapper })

    expect(result.current).toBe(fakeCompany)
  })

  it('throws when used outside a CompanyProvider', () => {
    expect(() => renderHook(() => useCompany())).toThrow(
      'useCompany must be used within a CompanyProvider',
    )
  })
})
