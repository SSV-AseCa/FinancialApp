import { useContext } from 'react'
import { WatchlistContext } from './WatchlistContext'
import type { WatchlistPort } from './WatchlistPort'

export function useWatchlist(): WatchlistPort {
  const port = useContext(WatchlistContext)
  if (port === null) {
    throw new Error('useWatchlist must be used within a WatchlistProvider')
  }
  return port
}
