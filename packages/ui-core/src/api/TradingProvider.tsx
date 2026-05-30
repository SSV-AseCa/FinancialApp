import type { ReactNode } from 'react'
import { TradingContext } from './TradingContext'
import type { TradingPort } from './TradingPort'

interface TradingProviderProps {
  port: TradingPort
  children: ReactNode
}

export function TradingProvider({ port, children }: TradingProviderProps) {
  return <TradingContext.Provider value={port}>{children}</TradingContext.Provider>
}
