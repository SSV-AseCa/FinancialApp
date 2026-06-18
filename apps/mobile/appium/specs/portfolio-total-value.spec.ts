import { $, expect } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { appiumBrowser } from '../helpers/appium-browser'
import {
    switchToWebViewContext,
    clearSession,
    loginWithAuth0Token,
    waitForDocumentReady,
} from '../helpers/spec-helpers'
import {
    clearPortfolio,
    fetchPortfolioValue,
    seedPortfolioWithPosition,
} from '../helpers/api'

function formatUsd(value: number): string {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
    }).format(value)
}

async function reloadApp() {
    await appiumBrowser.execute(() => {
        window.location.reload()
    })

    await waitForDocumentReady('Document did not finish loading after reload')
}

async function waitForTotalValue() {
    // temporary debug
    const bodyText = await appiumBrowser.execute(() => document.body.innerText)
    console.log('Page content:', bodyText.substring(0, 500))

    const totalValue = await $('[data-testid="portfolio-total-value"]')

    await totalValue.waitForDisplayed({
        timeout: 60000,
        timeoutMsg: 'Portfolio total value was not displayed',
    })

    return totalValue
}

describe('mobile portfolio total value', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
    })

    it('Investor sees total value that matches the backend', async () => {
        const token = await loginWithAuth0Token()

        await seedPortfolioWithPosition(token)
        await reloadApp()

        const expectedValue = await fetchPortfolioValue(token)

        console.log('Expected portfolio value:', JSON.stringify(expectedValue, null, 2))

        const totalValue = await waitForTotalValue()

        await expect(totalValue).toHaveText(formatUsd(expectedValue.totalValue))
    })

    it('Investor with no positions sees zero total value and a meaningful empty state', async () => {
        const token = await loginWithAuth0Token()

        await clearPortfolio(token)
        await reloadApp()

        const expectedValue = await fetchPortfolioValue(token)

        if (expectedValue.totalValue !== 0) {
            throw new Error(
                `Expected empty portfolio total value to be 0, but backend returned ${expectedValue.totalValue}.`,
            )
        }

        const totalValue = await waitForTotalValue()

        await expect(totalValue).toHaveText(formatUsd(0))

        const emptyState = await $('[data-testid="portfolio-empty"]')

        await emptyState.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Portfolio empty state was not displayed',
        })
    })
})