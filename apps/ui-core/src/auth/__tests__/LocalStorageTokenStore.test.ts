import { beforeEach, describe, expect, it } from 'vitest'
import { LocalStorageTokenStore } from '../LocalStorageTokenStore'

describe('LocalStorageTokenStore', () => {
  let store: LocalStorageTokenStore

  beforeEach(() => {
    localStorage.clear()
    store = new LocalStorageTokenStore()
  })

  it('returns null when no token has been stored', () => {
    expect(store.load()).toBeNull()
  })

  it('stores and retrieves a token', () => {
    store.save('token-abc')
    expect(store.load()).toBe('token-abc')
  })

  it('overwrites an existing token', () => {
    store.save('token-old')
    store.save('token-new')
    expect(store.load()).toBe('token-new')
  })

  it('clears a stored token', () => {
    store.save('token-abc')
    store.clear()
    expect(store.load()).toBeNull()
  })
})
