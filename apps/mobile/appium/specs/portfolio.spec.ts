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

async function loginWithMockToken() {
    await appiumBrowser.execute(() => {
        window.localStorage.setItem('ssv_mock_access_token', 'mock-token')
        window.location.reload()
    })
    await waitForDocumentReady('Document did not finish loading after setting auth token')
}

describe('mobile portfolio management', () => {
    beforeEach(async () => {
        await switchToWebViewContext()

        await appiumBrowser.execute(() => {
            window.localStorage.removeItem('ssv_mock_access_token')
            window.localStorage.removeItem('ssv_access_token')
            window.location.reload()
        })

        await waitForDocumentReady('Document did not finish loading after reload')

        await loginWithMockToken()
    })

    it('View Portfolio — shows portfolio screen title when authenticated', async () => {
        const portfolioTitle = await $('[data-testid="portfolio-screen-title"]')

        await portfolioTitle.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Portfolio screen title was not displayed',
        })
    })

    it('View Portfolio — portfolio screen content is visible', async () => {
        const content = await $('[data-testid="protected-screen-content"]')

        await content.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Portfolio screen content was not displayed',
        })
    })

    it('Add Position — add position button is visible on portfolio screen', async () => {
        const addButton = await $('[data-testid="add-position-button"]')

        await addButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Add Position button was not displayed',
        })
    })

    it('Add Position — clicking add position opens the add position screen', async () => {
        const addButton = await $('[data-testid="add-position-button"]')

        await addButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Add Position button was not displayed',
        })

        await addButton.click()

        const addScreen = await $('[data-testid="add-position-screen"]')

        await addScreen.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Add Position screen was not displayed',
        })

        const tickerInput = await $('[data-testid="add-ticker-input"]')
        await tickerInput.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Ticker input was not displayed',
        })
    })
})
