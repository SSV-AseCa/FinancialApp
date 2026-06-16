import type { WatchlistCompany } from './WatchlistCompany'
import type { WatchlistEntry } from './WatchlistEntry'

export interface WatchlistPort {
  addToWatchlist(cik: string): Promise<WatchlistEntry>
  getWatchlist(): Promise<WatchlistCompany[]>
  removeFromWatchlist(cik: string): Promise<void>
}
