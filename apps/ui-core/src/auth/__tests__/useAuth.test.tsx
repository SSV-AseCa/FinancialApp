import { renderHook } from '@testing-library/react'
import type { ReactNode } from 'react'
import { describe, expect, it } from 'vitest'
import type { AuthPort } from '../AuthPort'
import { AuthProvider } from '../AuthProvider'
import { useAuth } from '../useAuth'

const fakeAuth: AuthPort = {
  register: async () => {},
  login: async () => {},
  logout: async () => {},
  handleCallback: async () => {},
  getAccessToken: () => 'test-token',
  isAuthenticated: () => true,
}

function wrapper({ children }: { children: ReactNode }) {
  return <AuthProvider auth={fakeAuth}>{children}</AuthProvider>
}

describe('useAuth', () => {
  it('returns the AuthPort provided by AuthProvider', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })

    expect(result.current.getAccessToken()).toBe('test-token')
    expect(result.current.isAuthenticated()).toBe(true)
  })

  it('throws when used outside an AuthProvider', () => {
    expect(() => renderHook(() => useAuth())).toThrow(
      'useAuth must be used within an AuthProvider',
    )
  })
})
