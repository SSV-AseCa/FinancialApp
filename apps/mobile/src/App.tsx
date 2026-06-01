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
    const [isAuthenticated, setIsAuthenticated] = useState(auth.isAuthenticated())
    const [unauthenticatedScreen, setUnauthenticatedScreen] =
        useState<UnauthenticatedScreen>('login')

    const handleCallbackHandled = useCallback(() => {
        setIsAuthenticated(auth.isAuthenticated())
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
        <AuthCallbackHandler onCallbackHandled={handleCallbackHandled}>
            {isAuthenticated ? (
                <HomeScreen onLogout={handleLogout} />
            ) : unauthenticatedScreen === 'register' ? (
                <RegisterAccountScreen
                    onAuthenticated={handleAuthenticated}
                    onLogin={() => {
                        setUnauthenticatedScreen('login')
                    }}
                />
            ) : (
                <LoginScreen
                    onAuthenticated={handleAuthenticated}
                    onCreateAccount={() => {
                        setUnauthenticatedScreen('register')
                    }}
                />
            )}
        </AuthCallbackHandler>
    )
}

export default App