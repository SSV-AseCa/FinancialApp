import { useContext } from 'react'
import { TradingContext } from './TradingContext'
import type { TradingPort } from './TradingPort'

export function useTrading(): TradingPort {
  const port = useContext(TradingContext)
  if (port === null) {
    throw new Error('useTrading must be used within a TradingProvider')
  }
  return port
}
