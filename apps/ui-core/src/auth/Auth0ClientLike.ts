export type OpenUrl = (url: string) => Promise<void> | void

export interface Auth0ClientLike {
  loginWithRedirect(options?: {
    authorizationParams?: { screen_hint?: string }
    openUrl?: OpenUrl
  }): Promise<void>
  logout(options?: { logoutParams?: { returnTo?: string }; openUrl?: OpenUrl }): Promise<void>
  handleRedirectCallback(url?: string): Promise<unknown>
  getTokenSilently(): Promise<string>
}
