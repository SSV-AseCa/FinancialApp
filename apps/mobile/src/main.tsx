import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import {
    AuthProvider,
    LocalStorageTokenStore,
    createAuth0Adapter,
} from '@ssv/ui-core'
import './index.css'
import App from './App.tsx'

const redirectUri = import.meta.env.VITE_REDIRECT_URI ?? window.location.origin

const auth = createAuth0Adapter(
    {
        domain: import.meta.env.VITE_AUTH0_DOMAIN,
        clientId: import.meta.env.VITE_AUTH0_CLIENT_ID,
        redirectUri,
        logoutReturnTo: redirectUri,
    },
    new LocalStorageTokenStore(),
)

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <AuthProvider auth={auth}>
            <App />
        </AuthProvider>
    </StrictMode>,
)