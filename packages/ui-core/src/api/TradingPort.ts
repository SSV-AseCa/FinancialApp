import type { BuySharesInput } from './BuySharesInput'
import type { SellSharesInput } from './SellSharesInput'
import type { Transaction } from './Transaction'

export interface TradingPort {
  buyShares(input: BuySharesInput): Promise<Transaction>
  sellShares(input: SellSharesInput): Promise<Transaction>
  fetchTransactionHistory(): Promise<Transaction[]>
}
