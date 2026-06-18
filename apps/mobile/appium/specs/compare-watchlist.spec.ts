import { $, expect } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'
import { appiumBrowser } from '../helpers/appium-browser'

async function loginWithMockedWatchlist() {
    await appiumBrowser.execute(() => {
        const apple = {
            cik: '0000320193',
            name: 'Apple Inc.',
            tickers: ['AAPL'],
            metrics: {
                revenue: 383285000000,
                netIncome: 96995000000,
                assets: 352583000000,
                equity: 62146000000,
            },
        }
        const microsoft = {
            cik: '0000789019',
            name: 'Microsoft Corp.',
            tickers: ['MSFT'],
            metrics: {
                revenue: 211915000000,
                netIncome: 72361000000,
                assets: 411976000000,
                equity: 206223000000,
            },
        }
        const mockWatchlist = [apple, microsoft]
        const mockComparison = { companies: [apple, microsoft] }
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
            if (s.includes('/watchlist/compare')) {
                return Promise.resolve(
                    new Response(JSON.stringify(mockComparison), {
                        status: 200,
                        headers: { 'Content-Type': 'application/json' },
                    }),
                )
            }
            if (s.includes('/watchlist') && (!opts?.method || opts.method === 'GET')) {
                return Promise.resolve(
                    new Response(JSON.stringify(mockWatchlist), {
                        status: 200,
                        headers: { 'Content-Type': 'application/json' },
                    }),
                )
            }
            return orig(url, opts)
        }
    })
    await loginWithMockToken()
}

describe('mobile compare watchlist companies', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginWithMockedWatchlist()
    })

    it('Compare Watchlist — selecting two companies enables comparison and shows their revenues', async () => {
        const navWatchlist = await $('[data-testid="nav-watchlist"]')
        await navWatchlist.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Watchlist nav button was not displayed',
        })
        await navWatchlist.click()

        const watchlistScreen = await $('[data-testid="watchlist-screen"]')
        await watchlistScreen.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Watchlist screen was not displayed after navigation',
        })

        const compareButton = await $('[data-testid="compare-button"]')
        await compareButton.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Compare button was not displayed',
        })
        expect(await compareButton.isEnabled()).toBe(false)

        const selectApple = await $('[data-testid="compare-select-0000320193"]')
        await selectApple.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Compare select for Apple was not displayed',
        })
        await selectApple.click()

        const selectMicrosoft = await $('[data-testid="compare-select-0000789019"]')
        await selectMicrosoft.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Compare select for Microsoft was not displayed',
        })
        await selectMicrosoft.click()

        await compareButton.click()

        const comparisonView = await $('[data-testid="comparison-view"]')
        await comparisonView.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Comparison view was not displayed after comparing',
        })

        const appleRevenue = await $('[data-testid="compare-revenue-0000320193"]')
        await appleRevenue.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Apple revenue was not displayed in comparison view',
        })

        const microsoftRevenue = await $('[data-testid="compare-revenue-0000789019"]')
        await microsoftRevenue.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Microsoft revenue was not displayed in comparison view',
        })
    })
})
