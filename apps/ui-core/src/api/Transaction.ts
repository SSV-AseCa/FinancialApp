export interface Transaction {
  id: string
  portfolioId: string
  cik: string
  quantity: number
  type: 'BUY' | 'SELL'
  transactionDate: string
}
