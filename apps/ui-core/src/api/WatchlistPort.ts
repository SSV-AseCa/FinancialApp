import type { WatchlistEntry } from './WatchlistEntry'

export interface WatchlistPort {
  addToWatchlist(cik: string): Promise<WatchlistEntry>
}
