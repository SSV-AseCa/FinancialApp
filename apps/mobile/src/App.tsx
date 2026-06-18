import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '@ssv/ui-core'
import { App as CapacitorApp } from '@capacitor/app'
import { Browser } from '@capacitor/browser'
import './App.css'
import { AuthCallbackHandler } from './auth/AuthCallbackHandler.tsx'
import { RegisterAccountScreen } from './screens/RegisterAccountScreen.tsx'
import { HomeScreen } from './screens/HomeScreen.tsx'
import { LoginScreen } from './screens/LoginScreen.tsx'

type UnauthenticatedScreen = 'login' | 'register'

function App() {
    const auth = useAuth()

    const e2eAuthBootstrapEnabled =
        import.meta.env.VITE_ENABLE_E2E_AUTH_BOOTSTRAP === 'true'

    const hasStoredRealToken = useCallback(
        () =>
            e2eAuthBootstrapEnabled &&
            window.localStorage.getItem('ssv_access_token') !== null,
        [e2eAuthBootstrapEnabled],
    )

    const [isAuthenticated, setIsAuthenticated] = useState(
        auth.isAuthenticated() || hasStoredRealToken(),
    )

    const [unauthenticatedScreen, setUnauthenticatedScreen] =
        useState<UnauthenticatedScreen>('login')
    const [authError, setAuthError] = useState<string | null>(null)

    const authDebugText = `e2e=${String(e2eAuthBootstrapEnabled)} token=${String(hasStoredRealToken())} auth=${String(auth.isAuthenticated())}`

    // Native deep-link callback: when Auth0 redirects to the app's custom-scheme
    // URI, the in-app browser tab hands control back here. We finish the token
    // exchange and dismiss the tab. The web callback (same-tab redirect) is
    // handled separately by AuthCallbackHandler reading window.location.
    useEffect(() => {
        const listener = CapacitorApp.addListener('appUrlOpen', async ({ url }) => {
            const isAuthCallback =
                url.includes('state=') && (url.includes('code=') || url.includes('error='))

            if (isAuthCallback) {
                try {
                    await auth.handleCallback(url)
                    setAuthError(null)
                    setIsAuthenticated(auth.isAuthenticated())
                } catch {
                    setIsAuthenticated(false)
                    setUnauthenticatedScreen('login')
                    setAuthError('Authentication failed. Please try logging in again.')
                }
            }

            await Browser.close().catch(() => {
                // Tab may already be dismissed on some platforms; ignore.
            })
        })

        return () => {
            void listener.then((handle) => handle.remove())
        }
    }, [auth])

    const handleCallbackHandled = useCallback((error?: Error) => {
        if (error) {
            setIsAuthenticated(false)
            setUnauthenticatedScreen('login')
            setAuthError('Authentication failed. Please try logging in again.')
            return
        }

        setAuthError(null)
        setIsAuthenticated(auth.isAuthenticated() || hasStoredRealToken())
        setUnauthenticatedScreen('login')
    }, [auth, hasStoredRealToken])

    function handleAuthenticated() {
        setIsAuthenticated(true)
    }

    function handleLogout() {
        setIsAuthenticated(false)
        setUnauthenticatedScreen('login')
    }

    return (
        <>
            <div data-testid="auth-debug" style={{display: 'none'}}>
                {authDebugText}
            </div>

            <AuthCallbackHandler onCallbackHandled={handleCallbackHandled}>
                {isAuthenticated ? (
                    <HomeScreen onLogout={handleLogout}/>
                ) : unauthenticatedScreen === 'register' ? (
                    <RegisterAccountScreen
                        onAuthenticated={handleAuthenticated}
                        onLogin={() => {
                            setAuthError(null)
                            setUnauthenticatedScreen('login')
                        }}
                    />
                ) : (
                    <LoginScreen
                        errorMessage={authError}
                        onAuthenticated={handleAuthenticated}
                        onCreateAccount={() => {
                            setAuthError(null)
                            setUnauthenticatedScreen('register')
                        }}
                    />
                )}
            </AuthCallbackHandler>
        </>
    )
}

export default App