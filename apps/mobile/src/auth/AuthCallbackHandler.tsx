import {type ReactNode, useEffect, useState } from 'react'
import { useAuth } from '@ssv/ui-core'

type AuthCallbackHandlerProps = {
    children: ReactNode
}

export function AuthCallbackHandler({ children }: AuthCallbackHandlerProps) {
    const auth = useAuth()
    const [checkingCallback, setCheckingCallback] = useState(true)

    useEffect(() => {
        async function handleAuthCallback() {
            try {
                const params = new URLSearchParams(window.location.search)
                const hasAuthCallback = params.has('code') && params.has('state')

                if (hasAuthCallback) {
                    await auth.handleCallback(window.location.href)
                    window.history.replaceState({}, document.title, window.location.pathname)
                }
            } catch (error) {
                console.error('Failed to handle auth callback', error)
            } finally {
                setCheckingCallback(false)
            }
        }

        void handleAuthCallback()
    }, [auth])

    if (checkingCallback) {
        return <main className="register-page">Loading...</main>
    }

    return children
}