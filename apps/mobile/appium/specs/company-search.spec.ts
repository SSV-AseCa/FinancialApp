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

async function loginAndNavigateToCompanySearch() {
    await appiumBrowser.execute(() => {
        window.localStorage.setItem('ssv_mock_access_token', 'mock-token')
        window.location.reload()
    })
    await waitForDocumentReady('Document did not finish loading after reload')

    const researchButton = await $('[data-testid="research-button"]')
    await researchButton.waitForDisplayed({
        timeout: 30000,
        timeoutMsg: 'Research button was not displayed on portfolio screen',
    })
    await researchButton.click()

    const searchScreen = await $('[data-testid="company-search-screen"]')
    await searchScreen.waitForDisplayed({
        timeout: 30000,
        timeoutMsg: 'Company search screen was not displayed after clicking Research',
    })
}

describe('mobile search companies', () => {
    beforeEach(async () => {
        await switchToWebViewContext()

        await appiumBrowser.execute(() => {
            window.localStorage.removeItem('ssv_mock_access_token')
            window.localStorage.removeItem('ssv_access_token')
            window.location.reload()
        })

        await waitForDocumentReady('Document did not finish loading after reload')
        await loginAndNavigateToCompanySearch()
    })

    it('Search Companies — search input is visible on the company search screen', async () => {
        const searchInput = await $('[data-testid="company-search-input"]')
        await searchInput.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Company search input was not displayed',
        })
    })

    it('Search Companies — search submit button is visible', async () => {
        const submitButton = await $('[data-testid="company-search-submit"]')
        await submitButton.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Company search submit button was not displayed',
        })
    })

    it('Search Companies — typing a query and submitting shows results or no-results message', async () => {
        await appiumBrowser.execute(() => {
            const mockResults = [{ name: 'Apple Inc.', cik: '0000320193', tickers: ['AAPL'] }]
            const orig = window.fetch.bind(window)
            window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
                const s = typeof url === 'string' ? url : url.toString()
                if (s.includes('/companies/search')) {
                    return Promise.resolve(
                        new Response(JSON.stringify(mockResults), {
                            status: 200,
                            headers: { 'Content-Type': 'application/json' },
                        }),
                    )
                }
                return orig(url, opts)
            }
        })

        const searchInput = await $('[data-testid="company-search-input"]')
        await searchInput.waitForDisplayed({ timeout: 10000, timeoutMsg: 'Search input not visible' })
        await searchInput.setValue('apple')

        const submitButton = await $('[data-testid="company-search-submit"]')
        await submitButton.click()

        const results = await $('[data-testid="company-search-results"]')
        await results.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Search results were not displayed after submitting query',
        })
    })
})
