import { browser, $ } from '@wdio/globals'
import { describe, it, beforeEach } from 'mocha'

type AppiumBrowser = {
    waitUntil: (
        condition: () => Promise<boolean>,
        options: {
            timeout: number
            timeoutMsg: string
        },
    ) => Promise<boolean>

    pause: (milliseconds: number) => Promise<void>

    getContexts: () => Promise<string[]>
    switchContext: (context: string) => Promise<void>

    getUrl: () => Promise<string>
    getPageSource: () => Promise<string>

    execute: <TResult>(
        script: () => TResult,
    ) => Promise<TResult>
}

const appiumBrowser = browser as unknown as AppiumBrowser

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
    await appiumBrowser.pause(2000)

    const contexts = await appiumBrowser.getContexts()
    const webviewContext = contexts.find((context) => context.includes('WEBVIEW'))

    if (!webviewContext) {
        throw new Error(`Could not find WebView context. Contexts: ${contexts.join(', ')}`)
    }

    await appiumBrowser.switchContext(webviewContext)
}

describe('mobile registration flow', () => {
    beforeEach(async () => {
        await switchToWebViewContext()

        await appiumBrowser.execute(() => {
            window.localStorage.removeItem('ssv_mock_access_token')
            window.localStorage.removeItem('ssv_access_token')
            window.location.reload()
        })
        await appiumBrowser.pause(2000)

        await appiumBrowser.waitUntil(
            async () => {
                const readyState = await appiumBrowser.execute(() => document.readyState)
                return readyState === 'complete'
            },
            {
                timeout: 30000,
                timeoutMsg: 'Document did not finish loading after reload',
            },
        )
        await appiumBrowser.pause(2000)
    })

    it('happy path: register with mock auth and reach portfolio screen', async () => {
        const createAccountButton = await $('[data-testid="create-account-button"]')

        console.log('CURRENT URL:', await appiumBrowser.getUrl())
        console.log('PAGE SOURCE:', await appiumBrowser.getPageSource())

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

    it('post-registration access: authenticated investor can reach a protected screen', async () => {
        await appiumBrowser.execute(() => {
            window.localStorage.setItem('ssv_mock_access_token', 'mock-token')
            window.location.reload()
        })

        await appiumBrowser.waitUntil(
            async () => {
                const readyState = await appiumBrowser.execute(() => document.readyState)
                return readyState === 'complete'
            },
            {
                timeout: 30000,
                timeoutMsg: 'Document did not finish loading after setting auth token',
            },
        )
        await appiumBrowser.pause(2000)


        console.log('CURRENT URL:', await appiumBrowser.getUrl())
        console.log('PAGE SOURCE:', await appiumBrowser.getPageSource())

        const protectedContent = await $('[data-testid="protected-screen-content"]')
        await protectedContent.waitForDisplayed({
            timeout: 30000,
            timeoutMsg: 'Protected screen content was not displayed',
        })
    })
})