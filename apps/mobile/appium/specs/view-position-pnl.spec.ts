import { $, expect } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'
import { appiumBrowser } from '../helpers/appium-browser'

async function loginWithMockedPnL() {
    await appiumBrowser.execute(() => {
        const mockPortfolio = {
            id: 'portfolio-1',
            positions: [
                {
                    id: 'pos-gain',
                    ticker: 'AAPL',
                    quantity: 10,
                    operationDate: '2024-01-01',
                    pnl: 1500.25,
                    pnlPercent: 12.5,
                },
                {
                    id: 'pos-loss',
                    ticker: 'MSFT',
                    quantity: 5,
                    operationDate: '2024-02-01',
                    pnl: -320.75,
                    pnlPercent: -4.2,
                },
            ],
        }
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
            if (s.endsWith('/portfolio') && (!opts?.method || opts.method === 'GET')) {
                return Promise.resolve(
                    new Response(JSON.stringify(mockPortfolio), {
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

describe('mobile view position profit and loss', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginWithMockedPnL()
    })

    it('View PnL — gaining position shows currency text and gain direction', async () => {
        const pnl = await $('[data-testid="position-pnl-pos-gain"]')
        await pnl.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'PnL for gaining position was not displayed',
        })

        const text = await pnl.getText()
        expect(text.trim().length).toBeGreaterThan(0)

        const direction = await pnl.getAttribute('data-pnl-direction')
        expect(direction).toBe('gain')
    })

    it('View PnL — losing position shows currency text and loss direction', async () => {
        const pnl = await $('[data-testid="position-pnl-pos-loss"]')
        await pnl.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'PnL for losing position was not displayed',
        })

        const text = await pnl.getText()
        expect(text.trim().length).toBeGreaterThan(0)

        const direction = await pnl.getAttribute('data-pnl-direction')
        expect(direction).toBe('loss')
    })
})
