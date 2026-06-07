import { createContext } from 'react'
import type { AuthPort } from './AuthPort'

export const AuthContext = createContext<AuthPort | null>(null)
