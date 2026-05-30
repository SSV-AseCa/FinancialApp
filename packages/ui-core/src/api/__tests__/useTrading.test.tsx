import { renderHook } from '@testing-library/react'
import type { ReactNode } from 'react'
import { describe, expect, it } from 'vitest'
import type { TradingPort } from '../TradingPort'
import { TradingProvider } from '../TradingProvider'
import { useTrading } from '../useTrading'

const fakeTrading: TradingPort = {
  buyShares: async () => ({ type: 'BUY', company: 'AAPL', quantity: 1, date: '2024-01-01' }),
}

function wrapper({ children }: { children: ReactNode }) {
  return <TradingProvider port={fakeTrading}>{children}</TradingProvider>
}

describe('useTrading', () => {
  it('returns the TradingPort provided by TradingProvider', () => {
    const { result } = renderHook(() => useTrading(), { wrapper })

    expect(result.current).toBe(fakeTrading)
  })

  it('throws when used outside a TradingProvider', () => {
    expect(() => renderHook(() => useTrading())).toThrow(
      'useTrading must be used within a TradingProvider',
    )
  })
})
