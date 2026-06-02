import { $ } from '@wdio/globals'
import { appiumBrowser } from '../helpers/appium-browser'
import { describe, it, beforeEach } from 'mocha'


async function waitForDocumentReady(timeoutMsg: string) {
    await appiumBrowser.waitUntil(
        async () => {
            const readyState = await appiumBrowser.execute(() => document.readyState)
            return readyState === 'complete'
        },
        {
            timeout: 30000,
            timeoutMsg,
        },
    )
}

async function switchToWebViewContext() {
    await appiumBrowser.waitUntil(
        async () => {
            const contexts = await appiumBrowser.getContexts()
            return contexts.some((context) => context.includes('WEBVIEW'))
        },
        {
            timeout: 30000,
            timeoutMsg: 'WebView context was not available',
        },
    )

    const contexts = await appiumBrowser.getContexts()
    const webviewContext = contexts.find((context) => context.includes('WEBVIEW'))

    if (!webviewContext) {
        throw new Error(`Could not find WebView context. Contexts: ${contexts.join(', ')}`)
    }

    await appiumBrowser.switchContext(webviewContext)

    await waitForDocumentReady('Document did not finish loading after switching to WebView')
}

describe('mobile registration flow', () => {
    beforeEach(async () => {
        await switchToWebViewContext()

        await appiumBrowser.execute(() => {
            window.localStorage.removeItem('ssv_mock_access_token')
            window.localStorage.removeItem('ssv_access_token')
            window.location.reload()
        })

        await waitForDocumentReady('Document did not finish loading after reload')
    })

    it('happy path: register with mock auth and reach portfolio screen', async () => {
        const createAccountButton = await $('[data-testid="create-account-button"]')

        await createAccountButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Create account button was not displayed',
        })

        await createAccountButton.click()

        const continueButton = await $('[data-testid="continue-secure-signup-button"]')

        await continueButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Continue secure signup button was not displayed',
        })

        await continueButton.click()

        const portfolioTitle = await $('[data-testid="portfolio-screen-title"]')

        await portfolioTitle.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Portfolio screen title was not displayed',
        })
    })

    it('restores a mock authenticated session from localStorage', async () => {
        // This test intentionally bypasses the registration UI to verify that
        // the app restores an authenticated mock session from localStorage.
        await appiumBrowser.execute(() => {
            window.localStorage.setItem('ssv_mock_access_token', 'mock-token')
            window.location.reload()
        })

        await waitForDocumentReady('Document did not finish loading after setting auth token')

        const protectedContent = await $('[data-testid="protected-screen-content"]')

        await protectedContent.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Protected screen content was not displayed',
        })
    })
})