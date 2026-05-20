export interface TokenStore {
  save(token: string): void
  load(): string | null
  clear(): void
}
