export interface Auth0ClientLike {
  loginWithRedirect(options?: { authorizationParams?: { screen_hint?: string } }): Promise<void>
  logout(options?: { logoutParams?: { returnTo?: string } }): Promise<void>
  handleRedirectCallback(url?: string): Promise<unknown>
  getTokenSilently(): Promise<string>
}
