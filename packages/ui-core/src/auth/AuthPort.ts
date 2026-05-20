export interface AuthPort {
  register(): Promise<void>
  login(): Promise<void>
  handleCallback(url?: string): Promise<void>
  getAccessToken(): string | null
  isAuthenticated(): boolean
}
