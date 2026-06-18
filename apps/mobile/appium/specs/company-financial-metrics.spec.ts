import { $, expect } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'
import { appiumBrowser } from '../helpers/appium-browser'

async function loginNavigateSearchAndStub() {
    await loginWithMockToken()
    await appiumBrowser.execute(() => {
        const mockResults = [{ name: 'Apple Inc.', cik: '0000320193', tickers: ['AAPL'] }]
        const mockMetrics = [
            { metric: 'Revenue', value: 383285000000, unit: 'USD', periodEnd: '2024-09-30' },
            { metric: 'Net Income', value: 96995000000, unit: 'USD', periodEnd: '2024-09-30' },
        ]
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
            if (s.includes('/companies/0000320193/metrics')) {
                return Promise.resolve(
                    new Response(JSON.stringify(mockMetrics), {
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

describe('mobile company financial metrics', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginNavigateSearchAndStub()
    })

    it('Financial Metrics — viewing metrics shows a metrics list with name and value', async () => {
        const searchInput = await $('[data-testid="company-search-input"]')
        await searchInput.waitForDisplayed({ timeout: 10000, timeoutMsg: 'Search input not visible' })
        await searchInput.setValue('Apple')

        const submitButton = await $('[data-testid="company-search-submit"]')
        await submitButton.click()

        const viewMetrics = await $('[data-testid="view-metrics-0000320193"]')
        await viewMetrics.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'View metrics button was not displayed for Apple',
        })
        await viewMetrics.click()

        const metricsScreen = await $('[data-testid="company-metrics-screen"]')
        await metricsScreen.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Company metrics screen was not displayed',
        })

        const metricsList = await $('[data-testid="metrics-list"]')
        await metricsList.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Metrics list was not displayed',
        })

        const metricCard = await $('[data-testid="metric-card-0"]')
        await metricCard.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'First metric card was not displayed',
        })

        const metricName = await $('[data-testid="metric-name"]')
        await metricName.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Metric name was not displayed',
        })
        const metricNameText = await metricName.getText()
        expect(metricNameText.trim().length).toBeGreaterThan(0)

        const metricValue = await $('[data-testid="metric-value"]')
        await metricValue.waitForDisplayed({
            timeout: 15000,
            timeoutMsg: 'Metric value was not displayed',
        })
        const metricValueText = await metricValue.getText()
        expect(metricValueText.trim().length).toBeGreaterThan(0)
    })
})
