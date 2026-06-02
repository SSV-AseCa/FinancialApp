import { type ReactNode, useEffect, useState } from 'react'
import { useAuth } from '@ssv/ui-core'

type AuthCallbackHandlerProps = {
    children: ReactNode

    /**
     * Called after the OAuth callback has been processed.
     *
     * This callback must be referentially stable, for example memoized with
     * useCallback, because it is included in this component's useEffect
     * dependency array. Passing an inline function may cause the effect to
     * re-run on every render.
     */
    onCallbackHandled?: (error?: Error) => void
}

function toError(error: unknown): Error {
    return error instanceof Error ? error : new Error(String(error))
}

export function AuthCallbackHandler({
                                        children,
                                        onCallbackHandled,
                                    }: AuthCallbackHandlerProps) {
    const auth = useAuth()
    const [checkingCallback, setCheckingCallback] = useState(true)

    useEffect(() => {
        async function handleAuthCallback() {
            let callbackError: Error | undefined

            try {
                const params = new URLSearchParams(window.location.search)
                const hasAuthCallback = params.has('code') && params.has('state')

                if (hasAuthCallback) {
                    await auth.handleCallback(window.location.href)

                    window.history.replaceState(
                        {},
                        document.title,
                        window.location.pathname,
                    )
                }
            } catch (error) {
                callbackError = toError(error)
            } finally {
                onCallbackHandled?.(callbackError)
                setCheckingCallback(false)
            }
        }

        void handleAuthCallback()
    }, [auth, onCallbackHandled])

    if (checkingCallback) {
        return (
            <main className="register-page" data-testid="auth-callback-loading">
                Loading...
            </main>
        )
    }

    return children
}