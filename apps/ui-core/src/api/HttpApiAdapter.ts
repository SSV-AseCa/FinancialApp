import type { AuthPort } from '../auth/AuthPort'
import { ApiError } from './ApiError'
import type { AddPositionInput } from './AddPositionInput'
import type { BuySharesInput } from './BuySharesInput'
import type { Company } from './Company'
import type { CompanyFinancialMetrics } from './CompanyFinancialMetrics'
import type { CompanyPort } from './CompanyPort'
import type { HistoricalDataPoint } from './HistoricalDataPoint'
import type { ModifyPositionInput } from './ModifyPositionInput'
import type { Portfolio } from './Portfolio'
import type { PortfolioPerformance } from './PortfolioPerformance'
import type { PortfolioPort } from './PortfolioPort'
import type { PortfolioValue } from './PortfolioValue'
import type { Position } from './Position'
import type { SecFiling } from './SecFiling'
import type { SellSharesInput } from './SellSharesInput'
import type { Transaction } from './Transaction'
import type { TradingPort } from './TradingPort'
import type { WatchlistCompany } from './WatchlistCompany'
import type { WatchlistComparison } from './WatchlistComparison'
import type { WatchlistEntry } from './WatchlistEntry'
import type { WatchlistPort } from './WatchlistPort'

export class HttpApiAdapter implements PortfolioPort, CompanyPort, TradingPort, WatchlistPort {
  constructor(
    private readonly auth: AuthPort,
    private readonly baseUrl: string,
  ) {}

  private headers(): Record<string, string> {
    return {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${this.auth.getAccessToken()}`,
    }
  }

  private async request<T>(path: string, init?: RequestInit): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      ...init,
      headers: { ...this.headers(), ...(init?.headers as Record<string, string> | undefined) },
    })
    if (!response.ok) {
      const body = await response.json().catch(() => ({})) as { message?: string }
      throw new ApiError(body.message ?? response.statusText, response.status)
    }
    if (response.status === 204) return undefined as T
    return response.json() as Promise<T>
  }

  fetchPortfolio(): Promise<Portfolio> {
    return this.request<Portfolio>('/portfolio')
  }

  getPortfolioTotalValue(): Promise<PortfolioValue> {
    return this.request<PortfolioValue>('/portfolio/value')
  }

  getPortfolioPerformance(): Promise<PortfolioPerformance> {
    return this.request<PortfolioPerformance>('/portfolio/performance')
  }

  addPosition(input: AddPositionInput): Promise<Position> {
    return this.request<Position>('/portfolio/positions', {
      method: 'POST',
      body: JSON.stringify(input),
    })
  }

  modifyPosition(positionId: string, input: ModifyPositionInput): Promise<Position> {
    return this.request<Position>(`/portfolio/positions/${positionId}`, {
      method: 'PUT',
      body: JSON.stringify(input),
    })
  }

  removePosition(positionId: string): Promise<void> {
    return this.request<void>(`/portfolio/positions/${positionId}`, {
      method: 'DELETE',
    })
  }

  searchCompanies(query: string): Promise<Company[]> {
    return this.request<Company[]>(`/companies/search?q=${encodeURIComponent(query)}`)
  }

  getCompanySecFilings(cik: string): Promise<SecFiling[]> {
    return this.request<SecFiling[]>(`/companies/${encodeURIComponent(cik)}/filings`)
  }

  getCompanyFinancialMetrics(cik: string): Promise<CompanyFinancialMetrics[]> {
    return this.request<CompanyFinancialMetrics[]>(`/companies/${encodeURIComponent(cik)}/metrics`)
  }

  getCompanyHistoricalData(cik: string): Promise<HistoricalDataPoint[]> {
    return this.request<HistoricalDataPoint[]>(`/companies/${encodeURIComponent(cik)}/history`)
  }

  buyShares(input: BuySharesInput): Promise<Transaction> {
    return this.request<Transaction>('/portfolio/transactions/buy', {
      method: 'POST',
      body: JSON.stringify(input),
    })
  }

  sellShares(input: SellSharesInput): Promise<Transaction> {
    return this.request<Transaction>('/portfolio/transactions/sell', {
      method: 'POST',
      body: JSON.stringify(input),
    })
  }

  getTransactionHistory(): Promise<Transaction[]> {
    return this.request<Transaction[]>('/portfolio/transactions')
  }

  addToWatchlist(cik: string): Promise<WatchlistEntry> {
    return this.request<WatchlistEntry>('/watchlist', {
      method: 'POST',
      body: JSON.stringify({ cik }),
    })
  }

  getWatchlist(): Promise<WatchlistCompany[]> {
    return this.request<WatchlistCompany[]>('/watchlist')
  }

  removeFromWatchlist(cik: string): Promise<void> {
    return this.request<void>(`/watchlist/${cik}`, {
      method: 'DELETE',
    })
  }

  compareWatchlistCompanies(ciks: string[]): Promise<WatchlistComparison> {
    return this.request<WatchlistComparison>(
      `/watchlist/compare?ciks=${encodeURIComponent(ciks.join(','))}`,
    )
  }
}
