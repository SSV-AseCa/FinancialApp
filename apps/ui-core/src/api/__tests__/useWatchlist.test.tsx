import { renderHook } from '@testing-library/react'
import type { ReactNode } from 'react'
import { describe, expect, it } from 'vitest'
import type { WatchlistPort } from '../WatchlistPort'
import { WatchlistProvider } from '../WatchlistProvider'
import { useWatchlist } from '../useWatchlist'

const fakeWatchlist: WatchlistPort = {
  addToWatchlist: async () => ({ id: 'w1', companyId: 'c1', cik: '0000320193' }),
  getWatchlist: async () => [],
  removeFromWatchlist: async () => {},
}

function wrapper({ children }: { children: ReactNode }) {
  return <WatchlistProvider port={fakeWatchlist}>{children}</WatchlistProvider>
}

describe('useWatchlist', () => {
  it('returns the WatchlistPort provided by WatchlistProvider', () => {
    const { result } = renderHook(() => useWatchlist(), { wrapper })

    expect(result.current).toBe(fakeWatchlist)
  })

  it('throws when used outside a WatchlistProvider', () => {
    expect(() => renderHook(() => useWatchlist())).toThrow(
      'useWatchlist must be used within a WatchlistProvider',
    )
  })
})
