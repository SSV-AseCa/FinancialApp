import { appiumBrowser } from './appium-browser'

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
                        const webviewContext = contexts.find((context) => context.includes('WEBVIEW'))
                        if (!webviewContext) return false
                        await appiumBrowser.switchContext(webviewContext)
                        return true
                },
                { timeout: 60000, timeoutMsg: 'WebView context was not available' },
        )
        await waitForDocumentReady('Document did not finish loading after switching to WebView')
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
