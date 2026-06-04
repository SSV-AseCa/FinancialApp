import { $ } from '@wdio/globals'
import { appiumBrowser } from '../helpers/appium-browser'
import { describe, it, beforeEach } from 'mocha'

async function waitForDocumentReady(timeoutMsg: string) {
    await appiumBrowser.waitUntil(
        async () => {
            const readyState = await appiumBrowser.execute(() => document.readyState)
            return readyState === 'complete'
        },
        { timeout: 30000, timeoutMsg },
    )
}

async function switchToWebViewContext() {
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

describe('mobile login flow', () => {
    beforeEach(async () => {
        await switchToWebViewContext()

        await appiumBrowser.execute(() => {
            window.localStorage.removeItem('ssv_mock_access_token')
            window.localStorage.removeItem('ssv_access_token')
            window.location.reload()
        })

        await waitForDocumentReady('Document did not finish loading after reload')
    })

    it('happy path: mock login via login button → portfolio screen shown', async () => {
        const loginButton = await $('[data-testid="login-button"]')

        await loginButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Login button was not displayed',
        })

        await loginButton.click()

        await waitForDocumentReady('Document did not finish loading after login click')

        const portfolioTitle = await $('[data-testid="portfolio-screen-title"]')

        await portfolioTitle.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Portfolio screen title was not displayed after login',
        })
    })

    it('restores mock session from localStorage and shows portfolio screen', async () => {
        await appiumBrowser.execute(() => {
            window.localStorage.setItem('ssv_mock_access_token', 'mock-token')
            window.location.reload()
        })

        await waitForDocumentReady('Document did not finish loading after setting auth token')

        const portfolioTitle = await $('[data-testid="portfolio-screen-title"]')

        await portfolioTitle.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Portfolio screen title was not displayed after session restore',
        })
    })
})
