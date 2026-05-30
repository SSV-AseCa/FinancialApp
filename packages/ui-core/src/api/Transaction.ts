export interface Transaction {
  type: 'BUY' | 'SELL'
  company: string
  quantity: number
  date: string
}
