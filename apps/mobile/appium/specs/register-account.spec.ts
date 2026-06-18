import { $ } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'

describe('mobile registration flow', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
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
        await loginWithMockToken()

        const protectedContent = await $('[data-testid="protected-screen-content"]')

        await protectedContent.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Protected screen content was not displayed',
        })
    })
})