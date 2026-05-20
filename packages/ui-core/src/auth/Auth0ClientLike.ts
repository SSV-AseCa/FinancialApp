export interface Auth0ClientLike {
  loginWithRedirect(options?: { authorizationParams?: { screen_hint?: string } }): Promise<void>
  handleRedirectCallback(url?: string): Promise<unknown>
  getTokenSilently(): Promise<string>
}
