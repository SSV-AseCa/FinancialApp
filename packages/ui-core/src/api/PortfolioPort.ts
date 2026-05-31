import type { Portfolio } from './Portfolio'

export interface PortfolioPort {
  fetchPortfolio(): Promise<Portfolio>
}
