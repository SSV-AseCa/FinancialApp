import { createContext } from 'react'
import type { TradingPort } from './TradingPort'

export const TradingContext = createContext<TradingPort | null>(null)
