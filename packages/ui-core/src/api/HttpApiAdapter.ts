import type { AuthPort } from '../auth/AuthPort'
import { ApiError } from './ApiError'
import type { AddPositionInput } from './AddPositionInput'
import type { BuySharesInput } from './BuySharesInput'
import type { Company } from './Company'
import type { CompanyPort } from './CompanyPort'
import type { ModifyPositionInput } from './ModifyPositionInput'
import type { Portfolio } from './Portfolio'
import type { PortfolioPort } from './PortfolioPort'
import type { Position } from './Position'
import type { SellSharesInput } from './SellSharesInput'
import type { Transaction } from './Transaction'
import type { TradingPort } from './TradingPort'

export class HttpApiAdapter implements PortfolioPort, CompanyPort, TradingPort {
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

  fetchTransactionHistory(): Promise<Transaction[]> {
    return this.request<Transaction[]>('/portfolio/transactions')
  }
}
