import { $, expect } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'
import { appiumBrowser } from '../helpers/appium-browser'

async function loginNavigateSearchAndStub() {
    await loginWithMockToken()
    await appiumBrowser.execute(() => {
        const mockResults = [{ name: 'Apple Inc.', cik: '0000320193', tickers: ['AAPL'] }]
        const mockHistory = [
            { period: '2023', revenue: 383285000000, netIncome: 96995000000, assets: 352583000000, equity: 62146000000 },
            { period: '2022', revenue: 394328000000, netIncome: 99803000000, assets: 352755000000, equity: 50672000000 },
        ]
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
            if (s.includes('/companies/0000320193/history')) {
                return Promise.resolve(
                    new Response(JSON.stringify(mockHistory), {
                        status: 200,
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

describe('mobile company historical financial data', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginNavigateSearchAndStub()
    })

    it('Historical Data — viewing history shows a trend table with period and revenue', async () => {
        const searchInput = await $('[data-testid="company-search-input"]')
        await searchInput.waitForDisplayed({ timeout: 10000, timeoutMsg: 'Search input not visible' })
        await searchInput.setValue('Apple')

        const submitButton = await $('[data-testid="company-search-submit"]')
        await submitButton.click()

        const viewHistory = await $('[data-testid="view-history-0000320193"]')
        await viewHistory.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'View history button was not displayed for Apple',
        })
        await viewHistory.click()

        const historyScreen = await $('[data-testid="company-history-screen"]')
        await historyScreen.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Company history screen was not displayed',
        })

        const trendTable = await $('[data-testid="trend-table"]')
        await trendTable.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Trend table was not displayed',
        })

        const historyRow = await $('[data-testid="history-row-0"]')
        await historyRow.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'First history row was not displayed',
        })

        const periodCell = await $('[data-testid="history-cell-period"]')
        await periodCell.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'History period cell was not displayed',
        })
        const periodText = await periodCell.getText()
        expect(periodText.trim().length).toBeGreaterThan(0)

        const revenueCell = await $('[data-testid="history-cell-revenue"]')
        await revenueCell.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'History revenue cell was not displayed',
        })
        const revenueText = await revenueCell.getText()
        expect(revenueText.trim().length).toBeGreaterThan(0)
    })
})
