export interface AuthPort {
  register(): Promise<void>
  login(): Promise<void>
  logout(): Promise<void>
  handleCallback(url?: string): Promise<void>
  getAccessToken(): string | null
  isAuthenticated(): boolean
}
