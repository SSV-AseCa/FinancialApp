import { type ReactNode, useEffect, useState } from 'react'
import { useAuth } from '@ssv/ui-core'

type AuthCallbackHandlerProps = {
    children: ReactNode
    onCallbackHandled?: () => void
}

export function AuthCallbackHandler({
                                        children,
                                        onCallbackHandled,
                                    }: AuthCallbackHandlerProps) {
    const auth = useAuth()
    const [checkingCallback, setCheckingCallback] = useState(true)

    useEffect(() => {
        async function handleAuthCallback() {
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

                onCallbackHandled?.()
            } catch (error) {
                console.error('Failed to handle auth callback', error)
            } finally {
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