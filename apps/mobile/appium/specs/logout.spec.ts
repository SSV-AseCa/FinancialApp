import { $ } from '@wdio/globals'
import { appiumBrowser } from '../helpers/appium-browser'
import { switchToWebViewContext, loginWithMockToken, waitForDocumentReady } from '../helpers/spec-helpers'

describe('mobile logout flow', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await loginWithMockToken()
    })

    it('happy path: authenticated investor clicks logout → login screen shown', async () => {
        const logoutButton = await $('[data-testid="logout-button"]')

        await logoutButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Logout button was not displayed',
        })

        await logoutButton.click()

        await waitForDocumentReady('Document did not finish loading after logout click')

        const loginScreen = await $('[data-testid="login-screen"]')

        await loginScreen.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Login screen was not shown after logout',
        })
    })

    it('post-logout: session cleared from localStorage', async () => {
        const logoutButton = await $('[data-testid="logout-button"]')

        await logoutButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Logout button was not displayed',
        })

        await logoutButton.click()

        await waitForDocumentReady('Document did not finish loading after logout click')

        const token = await appiumBrowser.execute(() =>
            window.localStorage.getItem('ssv_mock_access_token'),
        )

        if (token !== null) {
            throw new Error(`Expected token to be cleared, but got: ${token}`)
        }
    })
})
