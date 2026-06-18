import {
    createAuth0Adapter,
    LocalStorageTokenStore,
    type AuthPort,
} from '@ssv/ui-core'
import { Capacitor } from '@capacitor/core'
import { Browser } from '@capacitor/browser'

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

/**
 * Opens the Auth0 authorize/logout URL in an in-app browser tab (Chrome Custom
 * Tabs on Android, SFSafariViewController on iOS) instead of switching to the
 * system browser. The flow returns to the app via the custom-scheme deep link
 * configured as the redirect URI; App.tsx listens for appUrlOpen to finish the
 * callback and dismiss the tab.
 */
async function openInAppBrowser(url: string): Promise<void> {
    await Browser.open({ url })
}

export function createMobileAuth(): AuthPort {
    const useMockAuth = import.meta.env.VITE_USE_MOCK_AUTH === 'true'

    if (useMockAuth) {
        return new MockAuthAdapter()
    }

    // Native shells keep login in-app via Custom Tabs. On the web (pnpm dev) we
    // leave openUrl undefined so auth0-spa-js does its standard same-tab redirect.
    const openUrl = Capacitor.isNativePlatform() ? openInAppBrowser : undefined

    return createAuth0Adapter(
        {
            domain: import.meta.env.VITE_AUTH0_DOMAIN,
            clientId: import.meta.env.VITE_AUTH0_CLIENT_ID,
            audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            redirectUri,
            logoutReturnTo: redirectUri,
            openUrl,
        },
        new LocalStorageTokenStore(),
    )
}