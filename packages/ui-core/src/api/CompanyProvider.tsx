import type { ReactNode } from 'react'
import { CompanyContext } from './CompanyContext'
import type { CompanyPort } from './CompanyPort'

interface CompanyProviderProps {
  port: CompanyPort
  children: ReactNode
}

export function CompanyProvider({ port, children }: CompanyProviderProps) {
  return <CompanyContext.Provider value={port}>{children}</CompanyContext.Provider>
}
