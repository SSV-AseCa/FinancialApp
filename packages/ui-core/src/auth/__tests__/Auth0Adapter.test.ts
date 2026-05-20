import { describe, expect, it } from 'vitest'
import { Auth0Adapter } from '../Auth0Adapter'
import type { Auth0ClientLike } from '../Auth0ClientLike'
import { InMemoryTokenStore } from '../InMemoryTokenStore'

class FakeAuth0Client implements Auth0ClientLike {
  loginWithRedirectOptions: { authorizationParams?: { screen_hint?: string } } | undefined
  handleRedirectCallbackUrl: string | undefined
  private readonly token: string

  constructor(token = 'fake-access-token') {
    this.token = token
  }

  async loginWithRedirect(options?: { authorizationParams?: { screen_hint?: string } }): Promise<void> {
    this.loginWithRedirectOptions = options
  }

  async handleRedirectCallback(url?: string): Promise<unknown> {
    this.handleRedirectCallbackUrl = url
    return {}
  }

  async getTokenSilently(): Promise<string> {
    return this.token
  }
}

describe('Auth0Adapter', () => {
  it('calls loginWithRedirect with screen_hint signup on register', async () => {
    const client = new FakeAuth0Client()
    const adapter = new Auth0Adapter(client, new InMemoryTokenStore())

    await adapter.register()

    expect(client.loginWithRedirectOptions).toEqual({
      authorizationParams: { screen_hint: 'signup' },
    })
  })

  it('stores the access token after handling the callback', async () => {
    const store = new InMemoryTokenStore()
    const client = new FakeAuth0Client('received-token')
    const adapter = new Auth0Adapter(client, store)

    await adapter.handleCallback('https://app.example.com/callback?code=abc&state=xyz')

    expect(store.load()).toBe('received-token')
  })

  it('forwards the callback url to the Auth0 client', async () => {
    const client = new FakeAuth0Client()
    const adapter = new Auth0Adapter(client, new InMemoryTokenStore())
    const callbackUrl = 'https://app.example.com/callback?code=abc&state=xyz'

    await adapter.handleCallback(callbackUrl)

    expect(client.handleRedirectCallbackUrl).toBe(callbackUrl)
  })

  it('returns null when no token has been stored', () => {
    const adapter = new Auth0Adapter(new FakeAuth0Client(), new InMemoryTokenStore())
    expect(adapter.getAccessToken()).toBeNull()
  })

  it('returns the stored token', async () => {
    const adapter = new Auth0Adapter(new FakeAuth0Client('my-token'), new InMemoryTokenStore())
    await adapter.handleCallback()
    expect(adapter.getAccessToken()).toBe('my-token')
  })

  it('reports not authenticated before callback', () => {
    const adapter = new Auth0Adapter(new FakeAuth0Client(), new InMemoryTokenStore())
    expect(adapter.isAuthenticated()).toBe(false)
  })

  it('reports authenticated after callback', async () => {
    const adapter = new Auth0Adapter(new FakeAuth0Client(), new InMemoryTokenStore())
    await adapter.handleCallback()
    expect(adapter.isAuthenticated()).toBe(true)
  })
})
