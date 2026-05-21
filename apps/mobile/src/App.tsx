import { useState } from 'react'
import { useAuth } from '@ssv/ui-core'
import './App.css'
import { AuthCallbackHandler } from './auth/AuthCallbackHandler.tsx'
import { RegisterAccountScreen } from './screens/RegisterAccountScreen.tsx'
import { HomeScreen } from './screens/HomeScreen.tsx'

function App() {
  const auth = useAuth()
  const [isAuthenticated, setIsAuthenticated] = useState(auth.isAuthenticated())

  function handleAuthenticated() {
    setIsAuthenticated(true)
  }

  function handleLogout() {
    setIsAuthenticated(false)
  }

  return (
      <AuthCallbackHandler
          onCallbackHandled={() => {
            setIsAuthenticated(auth.isAuthenticated())
          }}
      >
        {isAuthenticated ? (
            <HomeScreen onLogout={handleLogout} />
        ) : (
            <RegisterAccountScreen onAuthenticated={handleAuthenticated} />
        )}
      </AuthCallbackHandler>
  )
}

export default App