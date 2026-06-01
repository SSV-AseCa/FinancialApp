import { browser, expect } from '@wdio/globals'
import { describe, it } from 'mocha'

type AppiumBrowser = {
  waitUntil: (
      condition: () => Promise<boolean>,
      options: {
        timeout: number
        timeoutMsg: string
      },
  ) => Promise<boolean>
  getContexts: () => Promise<string[]>
}

const appiumBrowser = browser as unknown as AppiumBrowser

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