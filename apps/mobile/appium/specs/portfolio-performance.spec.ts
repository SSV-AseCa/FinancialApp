import { $, expect } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'
import { appiumBrowser } from '../helpers/appium-browser'

async function loginWithMockedPerformance() {
    await appiumBrowser.execute(() => {
        const mockPerformance = { totalValue: 12345.67, totalPnL: 234.5 }
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
            if (s.includes('/portfolio/performance')) {
                return Promise.resolve(
                    new Response(JSON.stringify(mockPerformance), {
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

describe('mobile portfolio performance metrics', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginWithMockedPerformance()
    })

    it('Performance — performance screen shows total value and total PnL', async () => {
        const navPerformance = await $('[data-testid="nav-performance"]')
        await navPerformance.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Performance nav button was not displayed',
        })
        await navPerformance.click()

        const performanceScreen = await $('[data-testid="performance-screen"]')
        await performanceScreen.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Performance screen was not displayed after navigation',
        })

        const totalValue = await $('[data-testid="performance-total-value"]')
        await totalValue.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Performance total value was not displayed',
        })
        const totalValueText = await totalValue.getText()
        expect(totalValueText.trim().length).toBeGreaterThan(0)

        const totalPnL = await $('[data-testid="performance-total-pnl"]')
        await totalPnL.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Performance total PnL was not displayed',
        })
        const totalPnLText = await totalPnL.getText()
        expect(totalPnLText.trim().length).toBeGreaterThan(0)
    })
})
