import { useContext } from 'react'
import { AuthContext } from './AuthContext'
import type { AuthPort } from './AuthPort'

export function useAuth(): AuthPort {
  const auth = useContext(AuthContext)
  if (auth === null) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return auth
}
