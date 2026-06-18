import type { OpenUrl } from './Auth0ClientLike'

export interface AuthConfig {
  domain: string
  clientId: string
  audience: string
  redirectUri: string
  logoutReturnTo: string
  /**
   * Optional hook controlling how authorize/logout URLs are opened.
   *
   * Web leaves this undefined, so auth0-spa-js performs its default
   * full-page redirect. Native shells (e.g. Capacitor) inject a function
   * that opens the URL in an in-app browser tab, keeping the user inside
   * the app instead of switching to the system browser.
   */
  openUrl?: OpenUrl
}
