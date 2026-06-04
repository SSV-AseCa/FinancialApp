import { $ } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, waitForDocumentReady, loginWithMockToken } from '../helpers/spec-helpers'

describe('mobile login flow', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
    })

    it('happy path: mock login via login button → portfolio screen shown', async () => {
        const loginButton = await $('[data-testid="login-button"]')

        await loginButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Login button was not displayed',
        })

        await loginButton.click()

        await waitForDocumentReady('Document did not finish loading after login click')

        const portfolioTitle = await $('[data-testid="portfolio-screen-title"]')

        await portfolioTitle.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Portfolio screen title was not displayed after login',
        })
    })

    it('restores mock session from localStorage and shows portfolio screen', async () => {
        await loginWithMockToken()

        const portfolioTitle = await $('[data-testid="portfolio-screen-title"]')

        await portfolioTitle.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Portfolio screen title was not displayed after session restore',
        })
    })
})
