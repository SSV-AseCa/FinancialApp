import { $, expect } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'
import { appiumBrowser } from '../helpers/appium-browser'

async function loginWithMockedWatchlist() {
    await loginWithMockToken()
    await appiumBrowser.execute(() => {
        const mockWatchlist = [
            {
                cik: '0000320193',
                name: 'Apple Inc.',
                tickers: ['AAPL'],
                metrics: {
                    revenue: 383285000000,
                    netIncome: 96995000000,
                    assets: 352583000000,
                    equity: 62146000000,
                },
            },
        ]
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
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
}

async function loginWithEmptyWatchlist() {
    await loginWithMockToken()
    await appiumBrowser.execute(() => {
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
            if (s.includes('/watchlist') && (!opts?.method || opts.method === 'GET')) {
                return Promise.resolve(
                    new Response(JSON.stringify([]), {
                        status: 200,
                        headers: { 'Content-Type': 'application/json' },
                    }),
                )
            }
            return orig(url, opts)
        }
    })
}

describe('mobile view watchlist financial metrics', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
    })

    it('View Watchlist — watchlist screen shows a company with its revenue metric', async () => {
        await loginWithMockedWatchlist()

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

        const watchlistItem = await $('[data-testid="watchlist-item-0000320193"]')
        await watchlistItem.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Watchlist item was not displayed',
        })

        const revenueMetric = await $('[data-testid="watchlist-metric-revenue-0000320193"]')
        await revenueMetric.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Watchlist revenue metric was not displayed',
        })
        const revenueText = await revenueMetric.getText()
        expect(revenueText.trim().length).toBeGreaterThan(0)
    })

    it('View Watchlist — empty watchlist shows the empty state', async () => {
        await loginWithEmptyWatchlist()

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

        const emptyState = await $('[data-testid="watchlist-empty"]')
        await emptyState.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Watchlist empty state was not displayed',
        })
    })
})
