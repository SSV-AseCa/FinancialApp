import type { TokenStore } from './TokenStore'

const TOKEN_KEY = 'ssv_access_token'

export class LocalStorageTokenStore implements TokenStore {
  save(token: string): void {
    localStorage.setItem(TOKEN_KEY, token)
  }

  load(): string | null {
    return localStorage.getItem(TOKEN_KEY)
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY)
  }
}
