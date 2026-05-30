import type { ReactNode } from 'react'
import { PortfolioContext } from './PortfolioContext'
import type { PortfolioPort } from './PortfolioPort'

interface PortfolioProviderProps {
  port: PortfolioPort
  children: ReactNode
}

export function PortfolioProvider({ port, children }: PortfolioProviderProps) {
  return <PortfolioContext.Provider value={port}>{children}</PortfolioContext.Provider>
}
