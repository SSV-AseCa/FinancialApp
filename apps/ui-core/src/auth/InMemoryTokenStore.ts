import type { TokenStore } from './TokenStore'

export class InMemoryTokenStore implements TokenStore {
  private token: string | null = null

  save(token: string): void {
    this.token = token
  }

  load(): string | null {
    return this.token
  }

  clear(): void {
    this.token = null
  }
}
