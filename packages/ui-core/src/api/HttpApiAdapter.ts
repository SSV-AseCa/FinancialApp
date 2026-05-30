import type { AuthPort } from '../auth/AuthPort'
import { ApiError } from './ApiError'
import type { Portfolio } from './Portfolio'
import type { PortfolioPort } from './PortfolioPort'

export class HttpApiAdapter implements PortfolioPort {
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
}
