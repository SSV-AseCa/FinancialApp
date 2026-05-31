import { renderHook } from '@testing-library/react'
import type { ReactNode } from 'react'
import { describe, expect, it } from 'vitest'
import type { PortfolioPort } from '../PortfolioPort'
import { PortfolioProvider } from '../PortfolioProvider'
import { usePortfolio } from '../usePortfolio'

const fakePortfolio: PortfolioPort = {
  fetchPortfolio: async () => ({ id: 'p1', positions: [] }),
  addPosition: async () => ({ id: 'pos1', ticker: 'AAPL', quantity: 1, operationDate: '2024-01-01' }),
  modifyPosition: async () => ({ id: 'pos1', ticker: 'AAPL', quantity: 2, operationDate: '2024-01-01' }),
}

function wrapper({ children }: { children: ReactNode }) {
  return <PortfolioProvider port={fakePortfolio}>{children}</PortfolioProvider>
}

describe('usePortfolio', () => {
  it('returns the PortfolioPort provided by PortfolioProvider', () => {
    const { result } = renderHook(() => usePortfolio(), { wrapper })

    expect(result.current).toBe(fakePortfolio)
  })

  it('throws when used outside a PortfolioProvider', () => {
    expect(() => renderHook(() => usePortfolio())).toThrow(
      'usePortfolio must be used within a PortfolioProvider',
    )
  })
})
