import { $ } from '@wdio/globals'
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
            if (s.includes('/watchlist/0000320193') && opts?.method === 'DELETE') {
                return Promise.resolve(new Response(null, { status: 204 }))
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
}

describe('mobile remove company from watchlist', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginWithMockedWatchlist()
    })

    it('Remove Watchlist — removing a company removes its watchlist item', async () => {
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
            timeoutMsg: 'Watchlist item was not displayed before removal',
        })

        const removeButton = await $('[data-testid="remove-watchlist-0000320193"]')
        await removeButton.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Remove from watchlist button was not displayed',
        })
        await removeButton.click()

        const removedItem = await $('[data-testid="watchlist-item-0000320193"]')
        await removedItem.waitForExist({
            reverse: true,
            timeout: 15000,
            timeoutMsg: 'Watchlist item was still present after removal',
        })
    })
})
