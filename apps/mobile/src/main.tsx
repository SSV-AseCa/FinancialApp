import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { AuthProvider } from '@ssv/ui-core'
import './index.css'
import App from './App.tsx'
import { createMobileAuth } from './auth/CreateMobileAuth.tsx'

const auth = createMobileAuth()

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <AuthProvider auth={auth}>
            <App />
        </AuthProvider>
    </StrictMode>,
)