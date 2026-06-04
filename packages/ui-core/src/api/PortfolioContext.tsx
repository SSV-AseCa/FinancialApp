import { createContext } from 'react'
import type { PortfolioPort } from './PortfolioPort'

export const PortfolioContext = createContext<PortfolioPort | null>(null)
