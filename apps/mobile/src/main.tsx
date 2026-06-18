import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { App as KonstaApp } from 'konsta/react'
import {
    AuthProvider,
    PortfolioProvider,
    CompanyProvider,
    TradingProvider,
    WatchlistProvider,
    HttpApiAdapter,
} from '@ssv/ui-core'
import './index.css'
import App from './App.tsx'
import { createMobileAuth } from './auth/CreateMobileAuth.tsx'

const auth = createMobileAuth()
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const api = new HttpApiAdapter(auth, apiBaseUrl)

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <KonstaApp theme="ios" dark safeAreas>
            <AuthProvider auth={auth}>
                <PortfolioProvider port={api}>
                    <CompanyProvider port={api}>
                        <TradingProvider port={api}>
                            <WatchlistProvider port={api}>
                                <App />
                            </WatchlistProvider>
                        </TradingProvider>
                    </CompanyProvider>
                </PortfolioProvider>
            </AuthProvider>
        </KonstaApp>
    </StrictMode>,
)
