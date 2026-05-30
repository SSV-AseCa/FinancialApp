import type { BuySharesInput } from './BuySharesInput'
import type { Transaction } from './Transaction'

export interface TradingPort {
  buyShares(input: BuySharesInput): Promise<Transaction>
}
