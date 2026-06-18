import { expect } from '@wdio/globals'
import { appiumBrowser } from '../helpers/appium-browser'

describe('SSV App', () => {
    it('should expose the Capacitor WebView context', async () => {
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
        expect(contexts.some((context) => context.includes('WEBVIEW'))).toBe(true)
    })
})