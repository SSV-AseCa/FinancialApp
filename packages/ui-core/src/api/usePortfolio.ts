import { useContext } from 'react'
import { PortfolioContext } from './PortfolioContext'
import type { PortfolioPort } from './PortfolioPort'

export function usePortfolio(): PortfolioPort {
  const port = useContext(PortfolioContext)
  if (port === null) {
    throw new Error('usePortfolio must be used within a PortfolioProvider')
  }
  return port
}
