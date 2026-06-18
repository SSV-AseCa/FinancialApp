import { $, expect } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'
import { appiumBrowser } from '../helpers/appium-browser'

async function loginNavigateSearchAndStub() {
    await appiumBrowser.execute(() => {
        const mockResults = [{ name: 'Apple Inc.', cik: '0000320193', tickers: ['AAPL'] }]
        const mockFilings = [
            { id: 'f-0', formType: '10-K', filingDate: '2024-11-01', documentUrl: 'https://sec.gov/f0' },
            { id: 'f-1', formType: '10-Q', filingDate: '2024-08-01', documentUrl: 'https://sec.gov/f1' },
        ]
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
            if (s.includes('/companies/0000320193/filings')) {
                return Promise.resolve(
                    new Response(JSON.stringify(mockFilings), {
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
    await loginWithMockToken()

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

describe('mobile company SEC filings', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginNavigateSearchAndStub()
    })

    it('SEC Filings — viewing filings shows a filings list with form type and date', async () => {
        const searchInput = await $('[data-testid="company-search-input"]')
        await searchInput.waitForDisplayed({ timeout: 10000, timeoutMsg: 'Search input not visible' })
        await searchInput.setValue('Apple')

        const submitButton = await $('[data-testid="company-search-submit"]')
        await submitButton.click()

        const viewFilings = await $('[data-testid="view-filings-0000320193"]')
        await viewFilings.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'View filings button was not displayed for Apple',
        })
        await viewFilings.click()

        const filingsScreen = await $('[data-testid="company-filings-screen"]')
        await filingsScreen.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Company filings screen was not displayed',
        })

        const filingsList = await $('[data-testid="filings-list"]')
        await filingsList.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Filings list was not displayed',
        })

        const filingRow = await $('[data-testid="filing-row-0"]')
        await filingRow.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'First filing row was not displayed',
        })

        const formType = await $('[data-testid="filing-form-type"]')
        await formType.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Filing form type was not displayed',
        })
        const formTypeText = await formType.getText()
        expect(formTypeText.trim().length).toBeGreaterThan(0)

        const filingDate = await $('[data-testid="filing-date"]')
        await filingDate.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Filing date was not displayed',
        })
        const filingDateText = await filingDate.getText()
        expect(filingDateText.trim().length).toBeGreaterThan(0)
    })
})
