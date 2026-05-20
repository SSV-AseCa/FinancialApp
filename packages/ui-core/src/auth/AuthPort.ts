export interface AuthPort {
  register(): Promise<void>
  handleCallback(url?: string): Promise<void>
  getAccessToken(): string | null
  isAuthenticated(): boolean
}
