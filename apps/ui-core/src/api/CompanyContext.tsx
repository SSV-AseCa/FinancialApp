import { createContext } from 'react'
import type { CompanyPort } from './CompanyPort'

export const CompanyContext = createContext<CompanyPort | null>(null)
