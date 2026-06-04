import { $ } from '@wdio/globals'
import { appiumBrowser } from '../helpers/appium-browser'
import { describe, it, beforeEach } from 'mocha'

async function waitForDocumentReady(timeoutMsg: string) {
    await appiumBrowser.waitUntil(
        async () => {
            const readyState = await appiumBrowser.execute(() => document.readyState)
            return readyState === 'complete'
        },
        { timeout: 30000, timeoutMsg },
    )
}

async function switchToWebViewContext() {
    await appiumBrowser.waitUntil(
        async () => {
            const contexts = await appiumBrowser.getContexts()
            const webviewContext = contexts.find((context) => context.includes('WEBVIEW'))
            if (!webviewContext) return false
            await appiumBrowser.switchContext(webviewContext)
            return true
        },
        { timeout: 60000, timeoutMsg: 'WebView context was not available' },
    )
    await waitForDocumentReady('Document did not finish loading after switching to WebView')
}

async function loginAndNavigateToTrading() {
    await appiumBrowser.execute(() => {
        window.localStorage.setItem('ssv_mock_access_token', 'mock-token')
        window.location.reload()
    })
    await waitForDocumentReady('Document did not finish loading after reload')

    const tradeButton = await $('[data-testid="trade-button"]')
    await tradeButton.waitForDisplayed({
        timeout: 30000,
        timeoutMsg: 'Trade button was not displayed on portfolio screen',
    })
    await tradeButton.click()

    const tradingScreen = await $('[data-testid="trading-screen"]')
    await tradingScreen.waitForDisplayed({
        timeout: 30000,
        timeoutMsg: 'Trading screen was not displayed after clicking Trade',
    })
}

describe('mobile buy shares', () => {
    beforeEach(async () => {
        await switchToWebViewContext()

        await appiumBrowser.execute(() => {
            window.localStorage.removeItem('ssv_mock_access_token')
            window.localStorage.removeItem('ssv_access_token')
            window.location.reload()
        })

        await waitForDocumentReady('Document did not finish loading after reload')
        await loginAndNavigateToTrading()
    })

    it('Buy Shares — CIK input is visible on the trading screen', async () => {
        const cikInput = await $('[data-testid="buy-cik-input"]')
        await cikInput.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Buy CIK input was not displayed',
        })
    })

    it('Buy Shares — quantity input is visible on the trading screen', async () => {
        const qtyInput = await $('[data-testid="buy-quantity-input"]')
        await qtyInput.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Buy quantity input was not displayed',
        })
    })

    it('Buy Shares — submit button is visible on the trading screen', async () => {
        const buyButton = await $('[data-testid="buy-submit-button"]')
        await buyButton.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Buy submit button was not displayed',
        })
    })
})

describe('mobile sell shares', () => {
    beforeEach(async () => {
        await switchToWebViewContext()

        await appiumBrowser.execute(() => {
            window.localStorage.removeItem('ssv_mock_access_token')
            window.localStorage.removeItem('ssv_access_token')
            window.location.reload()
        })

        await waitForDocumentReady('Document did not finish loading after reload')
        await loginAndNavigateToTrading()
    })

    it('Sell Shares — CIK input is visible on the trading screen', async () => {
        const cikInput = await $('[data-testid="sell-cik-input"]')
        await cikInput.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Sell CIK input was not displayed',
        })
    })

    it('Sell Shares — quantity input is visible on the trading screen', async () => {
        const qtyInput = await $('[data-testid="sell-quantity-input"]')
        await qtyInput.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Sell quantity input was not displayed',
        })
    })

    it('Sell Shares — submit button is visible on the trading screen', async () => {
        const sellButton = await $('[data-testid="sell-submit-button"]')
        await sellButton.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Sell submit button was not displayed',
        })
    })
})

describe('mobile transaction history', () => {
    beforeEach(async () => {
        await switchToWebViewContext()

        await appiumBrowser.execute(() => {
            window.localStorage.removeItem('ssv_mock_access_token')
            window.localStorage.removeItem('ssv_access_token')
            window.location.reload()
        })

        await waitForDocumentReady('Document did not finish loading after reload')
        await loginAndNavigateToTrading()
    })

    it('View Transaction History — empty state message is shown when no transactions exist', async () => {
        const noTx = await $('[data-testid="no-transactions"]')
        await noTx.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'No-transactions message was not displayed',
        })
    })

    it('View Transaction History — transactions list container is present', async () => {
        const list = await $('[data-testid="transactions-list"]')
        await list.waitForExist({
            timeout: 10000,
            timeoutMsg: 'Transactions list container was not found in DOM',
        })
    })
})
