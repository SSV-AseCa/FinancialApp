import type { ReactNode } from 'react'
import { AuthContext } from './AuthContext'
import type { AuthPort } from './AuthPort'

interface AuthProviderProps {
  auth: AuthPort
  children: ReactNode
}

export function AuthProvider({ auth, children }: AuthProviderProps) {
  return <AuthContext.Provider value={auth}>{children}</AuthContext.Provider>
}
