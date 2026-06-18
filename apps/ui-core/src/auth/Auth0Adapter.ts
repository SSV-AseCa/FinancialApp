import { Auth0Client } from '@auth0/auth0-spa-js'
import type { AuthConfig } from './AuthConfig'
import type { Auth0ClientLike, OpenUrl } from './Auth0ClientLike'
import type { AuthPort } from './AuthPort'
import type { TokenStore } from './TokenStore'

export class Auth0Adapter implements AuthPort {
  constructor(
    private readonly client: Auth0ClientLike,
    private readonly store: TokenStore,
    private readonly logoutReturnTo: string,
    private readonly openUrl?: OpenUrl,
  ) {}

  private get openUrlOption(): { openUrl?: OpenUrl } {
    return this.openUrl ? { openUrl: this.openUrl } : {}
  }

  async register(): Promise<void> {
    await this.client.loginWithRedirect({
      authorizationParams: { screen_hint: 'signup' },
      ...this.openUrlOption,
    })
  }

  async login(): Promise<void> {
    await this.client.loginWithRedirect({
      authorizationParams: { screen_hint: 'login' },
      ...this.openUrlOption,
    })
  }

  async logout(): Promise<void> {
    this.store.clear()
    await this.client.logout({
      logoutParams: { returnTo: this.logoutReturnTo },
      ...this.openUrlOption,
    })
  }

  async handleCallback(url?: string): Promise<void> {
    await this.client.handleRedirectCallback(url)
    const token = await this.client.getTokenSilently()
    this.store.save(token)
  }

  getAccessToken(): string | null {
    return this.store.load()
  }

  isAuthenticated(): boolean {
    return this.store.load() !== null
  }
}

export function createAuth0Adapter(config: AuthConfig, store: TokenStore): Auth0Adapter {
  return new Auth0Adapter(
    new Auth0Client({
      domain: config.domain,
      clientId: config.clientId,
      authorizationParams: { redirect_uri: config.redirectUri, audience: config.audience },
    }),
    store,
    config.logoutReturnTo,
    config.openUrl,
  )
}
