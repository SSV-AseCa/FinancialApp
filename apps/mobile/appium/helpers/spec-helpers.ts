import { appiumBrowser } from './appium-browser'
import { getAuth0TestAccessToken } from './auth0'
import { $ } from '@wdio/globals'

export async function waitForDocumentReady(timeoutMsg: string) {
        await appiumBrowser.waitUntil(
                async () => {
                        const readyState = await appiumBrowser.execute(() => document.readyState)
                        return readyState === 'complete'
                },
                { timeout: 30000, timeoutMsg },
        )
}

export async function switchToWebViewContext() {
    await appiumBrowser.waitUntil(
        async () => {
            const contexts = await appiumBrowser.getContexts()
            const webviewContext = contexts.find((context) => context === 'WEBVIEW_com.ssv.app')
            if (!webviewContext) return false
            await appiumBrowser.switchContext(webviewContext)
            return true
        },
        { timeout: 60000, timeoutMsg: 'WebView context was not available' },
    )

    await waitForDocumentReady('Document did not finish loading after switching to WebView')

    await appiumBrowser.waitUntil(
        async () => {
            try {
                await appiumBrowser.execute(() => {
                    window.localStorage.getItem('__test__')
                })
                return true
            } catch {
                return false
            }
        },
        { timeout: 60000, timeoutMsg: 'localStorage was not accessible after switching to WebView' },
    )
}

export async function clearSession() {
    await appiumBrowser.execute(() => {
        window.localStorage.removeItem('ssv_mock_access_token')
        window.localStorage.removeItem('ssv_access_token')
        window.location.reload()
    })

    await waitForDocumentReady('Document did not finish loading after reload')
}

export async function loginWithMockToken() {
        await appiumBrowser.execute(() => {
                window.localStorage.setItem('ssv_mock_access_token', 'mock-token')
                window.location.reload()
        })
        await waitForDocumentReady('Document did not finish loading after setting auth token')
}

export async function loginWithAuth0Token(): Promise<string> {
    const token = await getAuth0TestAccessToken()

    await appiumBrowser.execute((accessToken: string) => {
        window.localStorage.removeItem('ssv_mock_access_token')
        window.localStorage.setItem('ssv_access_token', accessToken)
        window.location.reload()
    }, token)

    await waitForDocumentReady('Document did not finish loading after setting Auth0 token')

    const authDebug = await appiumBrowser.execute(() => ({
        text: document.body.innerText,
        authDebug: document.querySelector('[data-testid="auth-debug"]')?.textContent,
        url: window.location.href,
        keys: Object.keys(window.localStorage),
        hasRealToken: window.localStorage.getItem('ssv_access_token') !== null,
    }))

    console.log('Auth debug after token injection:', JSON.stringify(authDebug, null, 2))

    const stored = await appiumBrowser.execute(() => ({
        mock: window.localStorage.getItem('ssv_mock_access_token'),
        real: window.localStorage.getItem('ssv_access_token'),
        allKeys: Object.keys(window.localStorage),
    }))

    console.log('localStorage after login:', JSON.stringify(stored))

    await $('[data-testid="portfolio-screen"]').waitForDisplayed({
        timeout: 60000,
        timeoutMsg: 'Portfolio screen was not displayed after setting Auth0 token',
    })

    return token
}
