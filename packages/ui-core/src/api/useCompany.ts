import { useContext } from 'react'
import { CompanyContext } from './CompanyContext'
import type { CompanyPort } from './CompanyPort'

export function useCompany(): CompanyPort {
  const port = useContext(CompanyContext)
  if (port === null) {
    throw new Error('useCompany must be used within a CompanyProvider')
  }
  return port
}
