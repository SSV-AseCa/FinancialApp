import type { ReactNode } from 'react'
import { WatchlistContext } from './WatchlistContext'
import type { WatchlistPort } from './WatchlistPort'

interface WatchlistProviderProps {
  port: WatchlistPort
  children: ReactNode
}

export function WatchlistProvider({ port, children }: WatchlistProviderProps) {
  return <WatchlistContext.Provider value={port}>{children}</WatchlistContext.Provider>
}
