export interface HistoricalDataPoint {
  period: string
  revenue: number
  netIncome: number
  assets: number
  equity: number
}

export type CompanyHistoricalData = HistoricalDataPoint
