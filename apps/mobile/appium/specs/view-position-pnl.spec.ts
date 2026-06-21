import { $, expect } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'
import { appiumBrowser } from '../helpers/appium-browser'

// The portfolio list is fetched on mount, before a JS fetch stub can be
// installed (the mock-login reload wipes any earlier stub). To exercise the
// P&L rendering deterministically we install the stub after login and then
// drive the add-position flow, which calls doFetch() and re-fetches the
// (stubbed) portfolio without a page reload.
async function loginAndStubPortfolio() {
    await loginWithMockToken()
    await appiumBrowser.execute(() => {
        const mockPortfolio = {
            id: 'portfolio-1',
            positions: [
                { id: 'pos-gain', ticker: 'AAPL', quantity: 10, operationDate: '2024-01-01', pnl: 1500.25, pnlPercent: 12.5 },
                { id: 'pos-loss', ticker: 'MSFT', quantity: 5, operationDate: '2024-02-01', pnl: -320.75, pnlPercent: -4.2 },
            ],
        }
        const created = { id: 'pos-gain', ticker: 'AAPL', quantity: 10, operationDate: '2024-01-01', pnl: 1500.25, pnlPercent: 12.5 }
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
            if (s.includes('/portfolio/positions') && opts?.method === 'POST') {
                return Promise.resolve(new Response(JSON.stringify(created), { status: 201, headers: { 'Content-Type': 'application/json' } }))
            }
            if (s.endsWith('/portfolio') && (!opts?.method || opts.method === 'GET')) {
                return Promise.resolve(new Response(JSON.stringify(mockPortfolio), { status: 200, headers: { 'Content-Type': 'application/json' } }))
            }
            return orig(url, opts)
        }
    })
}

async function addPositionToTriggerStubbedRefetch() {
    const addButton = await $('[data-testid="add-position-button"]')
    await addButton.waitForDisplayed({ timeout: 30000, timeoutMsg: 'Add position button was not displayed' })
    await addButton.click()

    const cik = await $('[data-testid="add-cik-input"]')
    await cik.waitForDisplayed({ timeout: 30000, timeoutMsg: 'Add CIK input was not displayed' })
    await cik.setValue('0000320193')
    await (await $('[data-testid="add-quantity-input"]')).setValue('10')
    await (await $('[data-testid="add-date-input"]')).setValue('2024-01-01')
    await (await $('[data-testid="confirm-add-position-button"]')).click()
}

describe('mobile view position profit and loss', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginAndStubPortfolio()
        await addPositionToTriggerStubbedRefetch()
    })

    it('View PnL — gaining position shows currency text and gain direction', async () => {
        const pnl = await $('[data-testid="position-pnl-pos-gain"]')
        await pnl.waitForDisplayed({ timeout: 30000, timeoutMsg: 'PnL for gaining position was not displayed' })

        const text = await pnl.getText()
        expect(text.trim().length).toBeGreaterThan(0)

        const direction = await pnl.getAttribute('data-pnl-direction')
        expect(direction).toBe('gain')
    })

    it('View PnL — losing position shows currency text and loss direction', async () => {
        const pnl = await $('[data-testid="position-pnl-pos-loss"]')
        await pnl.waitForDisplayed({ timeout: 30000, timeoutMsg: 'PnL for losing position was not displayed' })

        const text = await pnl.getText()
        expect(text.trim().length).toBeGreaterThan(0)

        const direction = await pnl.getAttribute('data-pnl-direction')
        expect(direction).toBe('loss')
    })
})
