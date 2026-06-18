import { createContext } from 'react'
import type { WatchlistPort } from './WatchlistPort'

export const WatchlistContext = createContext<WatchlistPort | null>(null)
