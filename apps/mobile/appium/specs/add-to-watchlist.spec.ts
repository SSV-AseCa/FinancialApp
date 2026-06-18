import { $ } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'
import { appiumBrowser } from '../helpers/appium-browser'

async function loginNavigateSearchAndStub() {
    await loginWithMockToken()
    await appiumBrowser.execute(() => {
        const mockResults = [{ name: 'Apple Inc.', cik: '0000320193', tickers: ['AAPL'] }]
        const mockWatchlistEntry = { id: 'w1', companyId: 'c1', cik: '0000320193' }
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
            if (s.includes('/watchlist') && opts?.method === 'POST') {
                return Promise.resolve(
                    new Response(JSON.stringify(mockWatchlistEntry), {
                        status: 201,
                        headers: { 'Content-Type': 'application/json' },
                    }),
                )
            }
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

describe('mobile add company to watchlist', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginNavigateSearchAndStub()
    })

    it('Add Watchlist — adding a company shows the watching badge', async () => {
        const searchInput = await $('[data-testid="company-search-input"]')
        await searchInput.waitForDisplayed({ timeout: 10000, timeoutMsg: 'Search input not visible' })
        await searchInput.setValue('Apple')

        const submitButton = await $('[data-testid="company-search-submit"]')
        await submitButton.click()

        const addWatchlist = await $('[data-testid="add-watchlist-0000320193"]')
        await addWatchlist.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Add to watchlist button was not displayed for Apple',
        })
        await addWatchlist.click()

        const watchingBadge = await $('[data-testid="watching-badge-0000320193"]')
        await watchingBadge.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Watching badge was not displayed after adding to watchlist',
        })
    })
})
