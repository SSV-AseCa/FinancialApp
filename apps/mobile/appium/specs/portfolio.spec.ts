import { $ } from '@wdio/globals'
import { appiumBrowser } from '../helpers/appium-browser'
import { describe, it, beforeEach } from 'mocha'
import { switchToWebViewContext, clearSession, loginWithMockToken } from '../helpers/spec-helpers'

async function loginWithMockedPortfolio() {
    await loginWithMockToken()
    await appiumBrowser.execute(() => {
        const mockPortfolio = {
            id: 'portfolio-1',
            positions: [{ id: 'pos-1', ticker: 'AAPL', quantity: 10, operationDate: '2024-01-01' }],
        }
        const orig = window.fetch.bind(window)
        window.fetch = (url: RequestInfo | URL, opts?: RequestInit) => {
            const s = typeof url === 'string' ? url : url.toString()
            if (s.endsWith('/portfolio') && (!opts?.method || opts.method === 'GET')) {
                return Promise.resolve(
                    new Response(JSON.stringify(mockPortfolio), {
                        status: 200,
                        headers: { 'Content-Type': 'application/json' },
                    }),
                )
            }
            if (s.includes('/portfolio/positions/')) {
                return Promise.resolve(
                    new Response('{}', {
                        status: 200,
                        headers: { 'Content-Type': 'application/json' },
                    }),
                )
            }
            return orig(url, opts)
        }
    })
}

describe('mobile portfolio management', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginWithMockToken()
    })

    it('View Portfolio — shows portfolio screen title when authenticated', async () => {
        const portfolioTitle = await $('[data-testid="portfolio-screen-title"]')

        await portfolioTitle.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Portfolio screen title was not displayed',
        })
    })

    it('View Portfolio — portfolio screen content is visible', async () => {
        const content = await $('[data-testid="protected-screen-content"]')

        await content.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Portfolio screen content was not displayed',
        })
    })

    it('Add Position — add position button is visible on portfolio screen', async () => {
        const addButton = await $('[data-testid="add-position-button"]')

        await addButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Add Position button was not displayed',
        })
    })

    it('Add Position — clicking add position opens the add position screen', async () => {
        const addButton = await $('[data-testid="add-position-button"]')

        await addButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Add Position button was not displayed',
        })

        await addButton.click()

        const addScreen = await $('[data-testid="add-position-screen"]')

        await addScreen.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Add Position screen was not displayed',
        })

        const cikInput = await $('[data-testid="add-cik-input"]')
        await cikInput.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'CIK input was not displayed',
        })
    })
})

describe('mobile modify position', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginWithMockedPortfolio()
    })

    it('Modify Position — edit button is visible next to a position', async () => {
        const editButton = await $('[data-testid="edit-position-pos-1"]')

        await editButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Edit button was not displayed for position pos-1',
        })
    })

    it('Modify Position — clicking edit opens the edit-position screen with form fields', async () => {
        const editButton = await $('[data-testid="edit-position-pos-1"]')

        await editButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Edit button was not displayed',
        })

        await editButton.click()

        const editScreen = await $('[data-testid="edit-position-screen"]')

        await editScreen.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Edit Position screen was not displayed',
        })

        const companyDisplay = await $('[data-testid="edit-ticker-display"]')
        await companyDisplay.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Company display was not shown on edit screen',
        })

        const saveButton = await $('[data-testid="save-position-button"]')
        await saveButton.waitForDisplayed({
            timeout: 10000,
            timeoutMsg: 'Save button was not displayed on edit screen',
        })
    })
})

describe('mobile remove position', () => {
    beforeEach(async () => {
        await switchToWebViewContext()
        await clearSession()
        await loginWithMockedPortfolio()
    })

    it('Remove Position — remove button is visible next to a position', async () => {
        const removeButton = await $('[data-testid="remove-position-pos-1"]')

        await removeButton.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Remove button was not displayed for position pos-1',
        })
    })
})
