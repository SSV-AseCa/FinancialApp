import type { TokenStore } from './TokenStore'

export const SSV_TOKEN_STORAGE_KEY = 'ssv_access_token'

export class LocalStorageTokenStore implements TokenStore {
  save(token: string): void {
    localStorage.setItem(SSV_TOKEN_STORAGE_KEY, token)
  }

  load(): string | null {
    return localStorage.getItem(SSV_TOKEN_STORAGE_KEY)
  }

  clear(): void {
    localStorage.removeItem(SSV_TOKEN_STORAGE_KEY)
  }
}
