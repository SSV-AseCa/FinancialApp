import { useCallback, useState } from 'react'
import { useAuth } from '@ssv/ui-core'
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

    const hasStoredRealToken = () =>
        e2eAuthBootstrapEnabled &&
        window.localStorage.getItem('ssv_access_token') !== null

    const [isAuthenticated, setIsAuthenticated] = useState(
        auth.isAuthenticated() || hasStoredRealToken(),
    )

    const [unauthenticatedScreen, setUnauthenticatedScreen] =
        useState<UnauthenticatedScreen>('login')
    const [authError, setAuthError] = useState<string | null>(null)

    const authDebugText = `e2e=${String(e2eAuthBootstrapEnabled)} token=${String(hasStoredRealToken())} auth=${String(auth.isAuthenticated())}`

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
    }, [auth])

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