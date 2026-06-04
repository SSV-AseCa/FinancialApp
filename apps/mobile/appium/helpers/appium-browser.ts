import { browser } from '@wdio/globals'

export type AppiumBrowser = {
    waitUntil: (
        condition: () => Promise<boolean>,
        options: {
            timeout: number
            timeoutMsg: string
        },
    ) => Promise<boolean>

    getContexts: () => Promise<string[]>
    switchContext: (context: string) => Promise<void>

    execute: <TResult>(
        script: () => TResult,
    ) => Promise<TResult>
}

export const appiumBrowser = browser as unknown as AppiumBrowser