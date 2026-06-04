import {
    createAuth0Adapter,
    LocalStorageTokenStore,
    type AuthPort,
} from '@ssv/ui-core'

class MockAuthAdapter implements AuthPort {
    async register(): Promise<void> {
        localStorage.setItem('ssv_mock_access_token', 'mock-token')
    }

    async login(): Promise<void> {
        localStorage.setItem('ssv_mock_access_token', 'mock-token')
    }

    async logout(): Promise<void> {
        localStorage.removeItem('ssv_mock_access_token')
    }

    async handleCallback(): Promise<void> {
        // No-op for mock auth.
    }

    getAccessToken(): string | null {
        return localStorage.getItem('ssv_mock_access_token')
    }

    isAuthenticated(): boolean {
        return this.getAccessToken() !== null
    }
}

const redirectUri = import.meta.env.VITE_REDIRECT_URI ?? window.location.origin

export function createMobileAuth(): AuthPort {
    const useMockAuth = import.meta.env.VITE_USE_MOCK_AUTH === 'true'

    if (useMockAuth) {
        return new MockAuthAdapter()
    }

    return createAuth0Adapter(
        {
            domain: import.meta.env.VITE_AUTH0_DOMAIN,
            clientId: import.meta.env.VITE_AUTH0_CLIENT_ID,
            redirectUri,
            logoutReturnTo: redirectUri,
        },
        new LocalStorageTokenStore(),
    )
}